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
package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Module for <a href='http://xmpp.org/rfcs/rfc3921.html#session'>Session
 * Establishment</a>.
 */
public class SessionEstablishmentModule implements XmppModule {

	public interface SessionEstablishmentErrorHandler extends EventHandler {

		public static class SessionEstablishmentErrorEvent extends JaxmppEvent<SessionEstablishmentErrorHandler> {

			private ErrorCondition error;

			public SessionEstablishmentErrorEvent(SessionObject sessionObject, ErrorCondition error) {
				super(sessionObject);
				this.error = error;
			}

			@Override
			protected void dispatch(SessionEstablishmentErrorHandler handler) throws JaxmppException {
				handler.onSessionEstablishmentError(sessionObject, error);
			}

			public ErrorCondition getError() {
				return error;
			}

			public void setError(ErrorCondition error) {
				this.error = error;
			}

		}

		void onSessionEstablishmentError(SessionObject sessionObject, ErrorCondition error) throws JaxmppException;
	}

	public interface SessionEstablishmentSuccessHandler extends EventHandler {

		public static class SessionEstablishmentSuccessEvent extends JaxmppEvent<SessionEstablishmentSuccessHandler> {

			public SessionEstablishmentSuccessEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(SessionEstablishmentSuccessHandler handler) throws JaxmppException {
				handler.onSessionEstablishmentSuccess(sessionObject);
			}

		}

		void onSessionEstablishmentSuccess(SessionObject sessionObject) throws JaxmppException;
	}

	public static final String SESSION_ESTABLISHED = "jaxmpp#sessionEstablished";

	public static boolean isSessionEstablishingAvailable(final SessionObject sessionObject) throws XMLException {
		final Element features = sessionObject.getStreamFeatures();

		return features != null && features.getChildrenNS("session", "urn:ietf:params:xml:ns:xmpp-session") != null;
	}

	private final Context context;

	protected final Logger log;

	public SessionEstablishmentModule(Context context) {
		log = Logger.getLogger(this.getClass().getName());
		this.context = context;
	}

	public void addSessionEstablishmentErrorHandler(SessionEstablishmentErrorHandler handler) {
		context.getEventBus().addHandler(SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent.class, handler);
	}

	public void addSessionEstablishmentSuccessHandler(SessionEstablishmentSuccessHandler handler) {
		context.getEventBus().addHandler(SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent.class, handler);
	}

	public void establish() throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = new DefaultElement("session", null, "urn:ietf:params:xml:ns:xmpp-session");
		iq.addChild(bind);

		context.getWriter().write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				context.getSessionObject().setProperty(SESSION_ESTABLISHED, Boolean.FALSE);
				SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent event = new SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent(
						context.getSessionObject(), error);
				context.getEventBus().fire(event, SessionEstablishmentModule.this);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
				context.getSessionObject().setProperty(SESSION_ESTABLISHED, Boolean.TRUE);
				SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent event = new SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent(
						context.getSessionObject());
				context.getEventBus().fire(event, SessionEstablishmentModule.this);
			}

			@Override
			public void onTimeout() throws JaxmppException {
				SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent event = new SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent(
						context.getSessionObject(), null);
				context.getEventBus().fire(event, SessionEstablishmentModule.this);
			}
		});
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
	public void process(Element element) throws XMPPException, XMLException {
	}

	public void removeSessionEstablishmentErrorHandler(SessionEstablishmentErrorHandler handler) {
		context.getEventBus().remove(SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent.class, handler);
	}

	public void removeSessionEstablishmentSuccessHandler(SessionEstablishmentSuccessHandler handler) {
		context.getEventBus().remove(SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent.class, handler);
	}

}