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
package tigase.jaxmpp.core.client.xmpp.modules.registration;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventType;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.NotSupportedErrorHandler.NotSupportedErrorEvent;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.ReceivedErrorHandler.ReceivedErrorEvent;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.ReceivedRequestedFieldsHandler.ReceivedRequestedFieldsEvent;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.ReceivedTimeoutHandler.ReceivedTimeoutEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class InBandRegistrationModule extends AbstractIQModule {

	public interface NotSupportedErrorHandler extends EventHandler {

		public static class NotSupportedErrorEvent extends JaxmppEvent<NotSupportedErrorHandler> {

			public static final EventType<NotSupportedErrorHandler> TYPE = new EventType<NotSupportedErrorHandler>();

			public NotSupportedErrorEvent(SessionObject sessionObject) {
				super(TYPE, sessionObject);
			}

			@Override
			protected void dispatch(NotSupportedErrorHandler handler) {
				handler.onNotSupportedError(sessionObject);
			}

		}

		void onNotSupportedError(SessionObject sessionObject);
	}

	public interface ReceivedErrorHandler extends EventHandler {

		public static class ReceivedErrorEvent extends JaxmppEvent<ReceivedErrorHandler> {

			public static final EventType<ReceivedErrorHandler> TYPE = new EventType<ReceivedErrorHandler>();

			private ErrorCondition errorCondition;

			private IQ responseStanza;

			public ReceivedErrorEvent(SessionObject sessionObject, IQ responseStanza, ErrorCondition error) {
				super(TYPE, sessionObject);
				this.responseStanza = responseStanza;
				this.errorCondition = error;
			}

			@Override
			protected void dispatch(ReceivedErrorHandler handler) {
				handler.onReceivedError(sessionObject, responseStanza, errorCondition);
			}

			public ErrorCondition getErrorCondition() {
				return errorCondition;
			}

			public IQ getResponseStanza() {
				return responseStanza;
			}

			public void setErrorCondition(ErrorCondition errorCondition) {
				this.errorCondition = errorCondition;
			}

			public void setResponseStanza(IQ responseStanza) {
				this.responseStanza = responseStanza;
			}

		}

		void onReceivedError(SessionObject sessionObject, IQ responseStanza, ErrorCondition errorCondition);
	}

	public interface ReceivedRequestedFieldsHandler extends EventHandler {

		public static class ReceivedRequestedFieldsEvent extends JaxmppEvent<ReceivedRequestedFieldsHandler> {

			public static final EventType<ReceivedRequestedFieldsHandler> TYPE = new EventType<ReceivedRequestedFieldsHandler>();
			private IQ responseStanza;

			public ReceivedRequestedFieldsEvent(SessionObject sessionObject, IQ responseStanza) {
				super(TYPE, sessionObject);
				this.responseStanza = responseStanza;
			}

			@Override
			protected void dispatch(ReceivedRequestedFieldsHandler handler) {
				handler.onReceivedRequestedFields(sessionObject, responseStanza);
			}

			public IQ getResponseStanza() {
				return responseStanza;
			}

			public void setResponseStanza(IQ responseStanza) {
				this.responseStanza = responseStanza;
			}

		}

		void onReceivedRequestedFields(SessionObject sessionObject, IQ responseStanza);
	}

	public interface ReceivedTimeoutHandler extends EventHandler {

		public static class ReceivedTimeoutEvent extends JaxmppEvent<ReceivedTimeoutHandler> {

			public static final EventType<ReceivedTimeoutHandler> TYPE = new EventType<ReceivedTimeoutHandler>();

			public ReceivedTimeoutEvent(SessionObject sessionObject) {
				super(TYPE, sessionObject);
			}

			@Override
			protected void dispatch(ReceivedTimeoutHandler handler) {
				handler.onReceivedTimeout(sessionObject);
			}

		}

		void onReceivedTimeout(SessionObject sessionObject);
	}

	public static final String IN_BAND_REGISTRATION_MODE_KEY = "IN_BAND_REGISTRATION_MODE_KEY";

	public static boolean isRegistrationAvailable(Context context) throws JaxmppException {
		return isRegistrationAvailable(context.getSessionObject());
	}

	public static boolean isRegistrationAvailable(SessionObject sessionObject) throws JaxmppException {
		final Element features = sessionObject.getStreamFeatures();

		boolean registrationSupported = features != null
				&& features.getChildrenNS("register", "http://jabber.org/features/iq-register") != null;

		return registrationSupported;
	}

	public InBandRegistrationModule(Context context) {
		super(context);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	protected void processGet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	public void register(String username, String password, String email, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance((String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME)));

		DefaultElement q = new DefaultElement("query", null, "jabber:iq:register");
		iq.addChild(q);
		if (username != null && username.length() > 0)
			q.addChild(new DefaultElement("username", username, null));
		if (password != null && password.length() > 0)
			q.addChild(new DefaultElement("password", password, null));
		if (email != null && email.length() > 0)
			q.addChild(new DefaultElement("email", email, null));

		write(iq, asyncCallback);

	}

	public void removeAccount(AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance((String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME)));

		DefaultElement q = new DefaultElement("query", null, "jabber:iq:register");
		iq.addChild(q);
		q.addChild(new DefaultElement("remove"));

		write(iq, asyncCallback);
	}

	public void start() throws JaxmppException {
		if (!isRegistrationAvailable(context)) {
			fireEvent(new NotSupportedErrorEvent(context.getSessionObject()));
		} else {
			IQ iq = IQ.create();
			iq.setType(StanzaType.get);
			iq.setTo(JID.jidInstance((String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME)));

			iq.addChild(new DefaultElement("query", null, "jabber:iq:register"));

			write(iq, new AsyncCallback() {

				@Override
				public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
					fireEvent(new ReceivedErrorEvent(context.getSessionObject(), (IQ) responseStanza, error));
				}

				@Override
				public void onSuccess(Stanza responseStanza) throws JaxmppException {
					fireEvent(new ReceivedRequestedFieldsEvent(context.getSessionObject(), (IQ) responseStanza));
				}

				@Override
				public void onTimeout() throws JaxmppException {
					fireEvent(new ReceivedTimeoutEvent(context.getSessionObject()));
				}
			});
		}
	}

}