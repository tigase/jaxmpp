package tigase.jaxmpp.core.client.xmpp.modules.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.AnonymousMechanism;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.PlainMechanism;

public class SaslModule implements XmppModule {

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

	public static final class SaslEvent extends AuthModule.AuthEvent {

		private static final long serialVersionUID = 1L;

		private SaslError error;

		public SaslEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public SaslError getError() {
			return error;
		}

		public void setError(SaslError error) {
			this.error = error;
		}
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

	protected final Logger log;

	private final Map<String, SaslMechanism> mechanisms = new HashMap<String, SaslMechanism>();

	private final ArrayList<String> mechanismsOrder = new ArrayList<String>();

	private final Observable observable;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public SaslModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		this.observable = ObservableFactory.instance(parentObservable);
		log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.writer = packetWriter;

		this.mechanisms.put("ANONYMOUS", new AnonymousMechanism());
		this.mechanisms.put("PLAIN", new PlainMechanism());

		this.mechanismsOrder.add("PLAIN");
		this.mechanismsOrder.add("ANONYMOUS");
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addMechanism(SaslMechanism mechanism) {
		this.mechanisms.put(mechanism.name(), mechanism);
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
		Element x = this.sessionObject.getStreamFeatures().getChildrenNS("mechanisms", "urn:ietf:params:xml:ns:xmpp-sasl");
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

			if (mechanism.isAllowedToUse(sessionObject))
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
		observable.fireEvent(AuthModule.AuthStart, new SaslEvent(AuthModule.AuthStart, sessionObject));

		SaslMechanism saslM = guessSaslMechanism();
		if (saslM == null) {
			log.fine("Not found supported SASL mechanisms.");
			throw new UnsupportedSaslMechanisms();
		}
		sessionObject.setProperty(SASL_MECHANISM, saslM);

		SaslMechanism mechanism = sessionObject.getProperty(SASL_MECHANISM);
		Element auth = new DefaultElement("auth");
		auth.setAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-sasl");
		auth.setAttribute("mechanism", mechanism.name());
		auth.setValue(mechanism.evaluateChallenge(null, sessionObject));

		sessionObject.setProperty(Connector.DISABLE_KEEPALIVE_KEY, Boolean.TRUE);

		writer.write(auth);
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		if ("success".equals(element.getName())) {
			sessionObject.setProperty(Connector.DISABLE_KEEPALIVE_KEY, Boolean.FALSE);
			processSuccess(element);
		} else if ("failure".equals(element.getName())) {
			sessionObject.setProperty(Connector.DISABLE_KEEPALIVE_KEY, Boolean.FALSE);
			processFailure(element);
		} else if ("challenge".equals(element.getName())) {
			processChallenge(element);
		}
	}

	protected void processChallenge(Element element) throws XMPPException, XMLException, JaxmppException {
		SaslMechanism mechanism = sessionObject.getProperty(SASL_MECHANISM);
		String v = element.getValue();
		String r = mechanism.evaluateChallenge(v, sessionObject);
		Element auth = new DefaultElement("response", r, "urn:ietf:params:xml:ns:xmpp-sasl");
		writer.write(auth);
	}

	protected void processFailure(Element element) throws JaxmppException {
		sessionObject.setProperty(AuthModule.AUTHORIZED, Boolean.FALSE);
		Element c = element.getFirstChild();
		SaslError error = null;
		if (c != null) {
			String n = c.getName().replace("-", "_");
			error = SaslError.valueOf(n);
		}
		log.fine("Failure with condition: " + error);
		SaslEvent event = new SaslEvent(AuthModule.AuthFailed, sessionObject);
		event.setError(error);
		observable.fireEvent(AuthModule.AuthFailed, event);
	}

	protected void processSuccess(Element element) throws JaxmppException {
		sessionObject.setProperty(AuthModule.AUTHORIZED, Boolean.TRUE);
		log.fine("Authenticated");
		observable.fireEvent(AuthModule.AuthSuccess, new SaslEvent(AuthModule.AuthSuccess, sessionObject));
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
