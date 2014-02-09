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
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
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

			public NotSupportedErrorEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(NotSupportedErrorHandler handler) throws JaxmppException {
				handler.onNotSupportedError(sessionObject);
			}

		}

		void onNotSupportedError(SessionObject sessionObject) throws JaxmppException;
	}

	public interface ReceivedErrorHandler extends EventHandler {

		public static class ReceivedErrorEvent extends JaxmppEvent<ReceivedErrorHandler> {

			private ErrorCondition errorCondition;

			private IQ responseStanza;

			public ReceivedErrorEvent(SessionObject sessionObject, IQ responseStanza, ErrorCondition error) {
				super(sessionObject);
				this.responseStanza = responseStanza;
				this.errorCondition = error;
			}

			@Override
			protected void dispatch(ReceivedErrorHandler handler) throws JaxmppException {
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

		void onReceivedError(SessionObject sessionObject, IQ responseStanza, ErrorCondition errorCondition)
				throws JaxmppException;
	}

	public interface ReceivedRequestedFieldsHandler extends EventHandler {

		public static class ReceivedRequestedFieldsEvent extends JaxmppEvent<ReceivedRequestedFieldsHandler> {

			private IQ responseStanza;

			public ReceivedRequestedFieldsEvent(SessionObject sessionObject, IQ responseStanza) {
				super(sessionObject);
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

			public ReceivedTimeoutEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(ReceivedTimeoutHandler handler) throws JaxmppException {
				handler.onReceivedTimeout(sessionObject);
			}

		}

		void onReceivedTimeout(SessionObject sessionObject) throws JaxmppException;
	}

	public static final String IN_BAND_REGISTRATION_MODE_KEY = "IN_BAND_REGISTRATION_MODE_KEY";

	public static boolean isRegistrationAvailable(Context context) throws JaxmppException {
		return isRegistrationAvailable(context.getSessionObject());
	}

	public static boolean isRegistrationAvailable(SessionObject sessionObject) throws JaxmppException {
		final Element features = StreamFeaturesModule.getStreamFeatures(sessionObject);

		boolean registrationSupported = features != null
				&& features.getChildrenNS("register", "http://jabber.org/features/iq-register") != null;

		return registrationSupported;
	}

	public InBandRegistrationModule(Context context) {
		super(context);
	}

	public void addNotSupportedErrorHandler(NotSupportedErrorHandler handler) {
		context.getEventBus().addHandler(NotSupportedErrorHandler.NotSupportedErrorEvent.class, handler);
	}

	public void addReceivedErrorHandler(ReceivedErrorHandler handler) {
		context.getEventBus().addHandler(ReceivedErrorHandler.ReceivedErrorEvent.class, handler);
	}

	public void addReceivedRequestedFieldsHandler(ReceivedRequestedFieldsHandler handler) {
		context.getEventBus().addHandler(ReceivedRequestedFieldsHandler.ReceivedRequestedFieldsEvent.class, handler);
	}

	public void addReceivedTimeoutHandler(ReceivedTimeoutHandler handler) {
		context.getEventBus().addHandler(ReceivedTimeoutHandler.ReceivedTimeoutEvent.class, handler);
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

		Element q = ElementFactory.create("query", null, "jabber:iq:register");
		iq.addChild(q);
		if (username != null && username.length() > 0)
			q.addChild(ElementFactory.create("username", username, null));
		if (password != null && password.length() > 0)
			q.addChild(ElementFactory.create("password", password, null));
		if (email != null && email.length() > 0)
			q.addChild(ElementFactory.create("email", email, null));

		write(iq, asyncCallback);

	}

	public void removeAccount(AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance((String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME)));

		Element q = ElementFactory.create("query", null, "jabber:iq:register");
		iq.addChild(q);
		q.addChild(ElementFactory.create("remove"));

		write(iq, asyncCallback);
	}

	public void removeNotSupportedErrorHandler(NotSupportedErrorHandler handler) {
		context.getEventBus().remove(NotSupportedErrorHandler.NotSupportedErrorEvent.class, handler);
	}

	public void removeReceivedErrorHandler(ReceivedErrorHandler handler) {
		context.getEventBus().remove(ReceivedErrorHandler.ReceivedErrorEvent.class, handler);
	}

	public void removeReceivedRequestedFieldsHandler(ReceivedRequestedFieldsHandler handler) {
		context.getEventBus().remove(ReceivedRequestedFieldsHandler.ReceivedRequestedFieldsEvent.class, handler);
	}

	public void removeReceivedTimeoutHandler(ReceivedTimeoutHandler handler) {
		context.getEventBus().remove(ReceivedTimeoutHandler.ReceivedTimeoutEvent.class, handler);
	}

	public void start() throws JaxmppException {
		if (!isRegistrationAvailable(context)) {
			fireEvent(new NotSupportedErrorEvent(context.getSessionObject()));
		} else {
			IQ iq = IQ.create();
			iq.setType(StanzaType.get);
			iq.setTo(JID.jidInstance((String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME)));

			iq.addChild(ElementFactory.create("query", null, "jabber:iq:register"));

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