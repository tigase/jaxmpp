/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.xmpp.modules.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventType;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.AnonymousMechanism;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.PlainMechanism;

/**
 * Module for SASL authentication.
 */
public class SaslModule implements XmppModule {

	public interface SaslAuthFailedHandler extends EventHandler {

		public static class SaslAuthFailedEvent extends JaxmppEvent<SaslAuthFailedHandler> {

			public static final EventType<SaslAuthFailedHandler> TYPE = new EventType<SaslAuthFailedHandler>();

			private SaslError error;

			public SaslAuthFailedEvent(SessionObject sessionObject, SaslError error) {
				super(TYPE, sessionObject);
				this.error = error;
			}

			@Override
			protected void dispatch(SaslAuthFailedHandler handler) {
				handler.onAuthFailed(sessionObject, error);
			}

			public SaslError getError() {
				return error;
			}

			public void setError(SaslError error) {
				this.error = error;
			}

		}

		void onAuthFailed(SessionObject sessionObject, SaslError error);
	}

	public interface SaslAuthStartHandler extends EventHandler {

		public static class SaslAuthStartEvent extends JaxmppEvent<SaslAuthStartHandler> {

			public static final EventType<SaslAuthStartHandler> TYPE = new EventType<SaslAuthStartHandler>();
			private String mechanismName;

			public SaslAuthStartEvent(SessionObject sessionObject, String mechanismName) {
				super(TYPE, sessionObject);
				this.mechanismName = mechanismName;
			}

			@Override
			protected void dispatch(SaslAuthStartHandler handler) {
				handler.onAuthStart(sessionObject, mechanismName);
			}

			public String getMechanismName() {
				return mechanismName;
			}

			public void setMechanismName(String mechanismName) {
				this.mechanismName = mechanismName;
			}

		}

		void onAuthStart(SessionObject sessionObject, String mechanismName);
	}

	public interface SaslAuthSuccessHandler extends EventHandler {

		public static class SaslAuthSuccessEvent extends JaxmppEvent<SaslAuthSuccessHandler> {

			public static final EventType<SaslAuthSuccessHandler> TYPE = new EventType<SaslAuthSuccessHandler>();

			public SaslAuthSuccessEvent(SessionObject sessionObject) {
				super(TYPE, sessionObject);
			}

			@Override
			protected void dispatch(SaslAuthSuccessHandler handler) {
				handler.onAuthSuccess(sessionObject);
			}

		}

		void onAuthSuccess(SessionObject sessionObject);
	}

	public static enum SaslError {
		/**
		 * The receiving entity acknowledges an &lt;abort/&gt; element sent by
		 * the initiating entity; sent in reply to the &lt;abort/&gt; element.
		 */
		aborted,
		/**
		 * The data provided by the initiating entity could not be processed
		 * because the BASE64 encoding is incorrect (e.g., because the encoding
		 * does not adhere to the definition in Section 3 of BASE64); sent in
		 * reply to a &lt;response/&gt; element or an &lt;auth/&gt; element with
		 * initial response data.
		 */
		incorrect_encoding,
		/**
		 * The authzid provided by the initiating entity is invalid, either
		 * because it is incorrectly formatted or because the initiating entity
		 * does not have permissions to authorize that ID; sent in reply to a
		 * &lt;response/&gt element or an &lt;auth/&gt element with initial
		 * response data.
		 */
		invalid_authzid,
		/**
		 * The initiating entity did not provide a mechanism or requested a
		 * mechanism that is not supported by the receiving entity; sent in
		 * reply to an &lt;auth/&gt element.
		 */
		invalid_mechanism,
		/**
		 * The mechanism requested by the initiating entity is weaker than
		 * server policy permits for that initiating entity; sent in reply to a
		 * &lt;response/&gt element or an &lt;auth/&gt element with initial
		 * response data.
		 */
		mechanism_too_weak,
		/**
		 * he authentication failed because the initiating entity did not
		 * provide valid credentials (this includes but is not limited to the
		 * case of an unknown username); sent in reply to a &lt;response/&gt
		 * element or an &lt;auth/&gt element with initial response data.
		 */
		not_authorized,
		/**
		 * The authentication failed because of a temporary error condition
		 * within the receiving entity; sent in reply to an &lt;auth/&gt element
		 * or &lt;response/&gt element.
		 */
		temporary_auth_failure,

	}

	public static class UnsupportedSaslMechanisms extends JaxmppException {
		private static final long serialVersionUID = 1L;

		public UnsupportedSaslMechanisms() {
			super("Not found supported SASL mechanisms.");
		}
	}

	private final static Criteria CRIT = new Or(new Criteria[] {
			ElementCriteria.name("success", "urn:ietf:params:xml:ns:xmpp-sasl"),
			ElementCriteria.name("failure", "urn:ietf:params:xml:ns:xmpp-sasl"),
			ElementCriteria.name("challenge", "urn:ietf:params:xml:ns:xmpp-sasl") });

	public static final String SASL_MECHANISM = "jaxmpp#saslMechanism";

	public static List<String> getAllowedSASLMechanisms(SessionObject sessionObject) throws XMLException {
		final Element sf = sessionObject.getStreamFeatures();
		if (sf == null)
			return null;
		Element m = sf.getChildrenNS("mechanisms", "urn:ietf:params:xml:ns:xmpp-sasl");
		if (m == null)
			return null;

		List<Element> ml = m.getChildren("mechanism");
		if (ml == null)
			return null;

		ArrayList<String> result = new ArrayList<String>();
		for (Element element : ml) {
			result.add(element.getValue());
		}

		return result;
	}

	private final Context context;

	protected final Logger log;

	private final Map<String, SaslMechanism> mechanisms = new HashMap<String, SaslMechanism>();

	private final ArrayList<String> mechanismsOrder = new ArrayList<String>();

	public SaslModule(Context context) {
		this.context = context;
		log = Logger.getLogger(this.getClass().getName());

		this.mechanisms.put("ANONYMOUS", new AnonymousMechanism());
		this.mechanisms.put("PLAIN", new PlainMechanism());

		this.mechanismsOrder.add("PLAIN");
		this.mechanismsOrder.add("ANONYMOUS");
	}

	public void addMechanism(SaslMechanism mechanism) {
		addMechanism(mechanism, false);
	}

	public void addMechanism(SaslMechanism mechanism, boolean atFirstPlace) {
		this.mechanisms.put(mechanism.name(), mechanism);
		if (atFirstPlace) {
			this.mechanismsOrder.add(0, mechanism.name());
		} else {
			this.mechanismsOrder.add(mechanism.name());
		}
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public ArrayList<String> getMechanismsOrder() {
		return mechanismsOrder;
	}

	protected Collection<String> getSupportedMechanisms() throws XMLException {
		ArrayList<String> result = new ArrayList<String>();
		Element x = this.context.getSessionObject().getStreamFeatures().getChildrenNS("mechanisms",
				"urn:ietf:params:xml:ns:xmpp-sasl");
		if (x != null) {
			List<Element> mms = x.getChildren("mechanism");
			if (mms != null)
				for (Element element : mms) {
					String n = element.getValue();
					if (n != null && n.length() != 0)
						result.add(n);
				}
		}

		return result;
	}

	protected SaslMechanism guessSaslMechanism() throws XMLException {
		final Collection<String> supportedMechanisms = getSupportedMechanisms();

		for (final String name : this.mechanismsOrder) {
			final SaslMechanism mechanism = this.mechanisms.get(name);
			if (mechanism == null || !supportedMechanisms.contains(name))
				continue;

			if (mechanism.isAllowedToUse(context.getSessionObject()))
				return mechanism;

		}

		return null;

		// SaslMechanism result;
		// if (sessionObject.getProperty(SessionObject.PASSWORD) == null
		// || sessionObject.getProperty(SessionObject.USER_BARE_JID) == null) {
		// result = new AnonymousMechanism();
		// } else {
		// result = new PlainMechanism();
		// }
		// log.info("Selected SASL mechanism: " + result.name());
		// return result;
	}

	public void login() throws XMLException, JaxmppException {
		log.fine("Try login with SASL");

		SaslMechanism saslM = guessSaslMechanism();
		if (saslM == null) {
			log.fine("Not found supported SASL mechanisms.");
			throw new UnsupportedSaslMechanisms();
		}
		context.getSessionObject().setProperty(Scope.stream, SASL_MECHANISM, saslM);

		SaslMechanism mechanism = context.getSessionObject().getProperty(SASL_MECHANISM);
		Element auth = new DefaultElement("auth");
		auth.setAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-sasl");
		auth.setAttribute("mechanism", mechanism.name());
		auth.setValue(mechanism.evaluateChallenge(null, context.getSessionObject()));

		context.getSessionObject().setProperty(Scope.stream, Connector.DISABLE_KEEPALIVE_KEY, Boolean.TRUE);

		SaslAuthStartHandler.SaslAuthStartEvent event = new SaslAuthStartHandler.SaslAuthStartEvent(context.getSessionObject(),
				mechanism.name());
		context.getEventBus().fire(event);

		context.getWriter().write(auth);
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		if ("success".equals(element.getName())) {
			context.getSessionObject().setProperty(Scope.stream, Connector.DISABLE_KEEPALIVE_KEY, Boolean.FALSE);
			processSuccess(element);
		} else if ("failure".equals(element.getName())) {
			context.getSessionObject().setProperty(Scope.stream, Connector.DISABLE_KEEPALIVE_KEY, Boolean.FALSE);
			processFailure(element);
		} else if ("challenge".equals(element.getName())) {
			processChallenge(element);
		}
	}

	protected void processChallenge(Element element) throws XMPPException, XMLException, JaxmppException {
		SaslMechanism mechanism = context.getSessionObject().getProperty(SASL_MECHANISM);
		String v = element.getValue();
		String r = mechanism.evaluateChallenge(v, context.getSessionObject());
		Element auth = new DefaultElement("response", r, "urn:ietf:params:xml:ns:xmpp-sasl");
		context.getWriter().write(auth);
	}

	protected void processFailure(Element element) throws JaxmppException {
		context.getSessionObject().setProperty(Scope.stream, AuthModule.AUTHORIZED, Boolean.FALSE);
		Element c = element.getFirstChild();
		SaslError error = null;
		if (c != null) {
			String n = c.getName().replace("-", "_");
			error = SaslError.valueOf(n);
		}
		log.fine("Failure with condition: " + error);

		SaslAuthFailedHandler.SaslAuthFailedEvent event = new SaslAuthFailedHandler.SaslAuthFailedEvent(
				context.getSessionObject(), error);
		context.getEventBus().fire(event, this);
	}

	protected void processSuccess(Element element) throws JaxmppException {
		context.getSessionObject().setProperty(Scope.stream, AuthModule.AUTHORIZED, Boolean.TRUE);
		log.fine("Authenticated");
		context.getEventBus().fire(new SaslAuthSuccessHandler.SaslAuthSuccessEvent(context.getSessionObject()), this);
	}

}