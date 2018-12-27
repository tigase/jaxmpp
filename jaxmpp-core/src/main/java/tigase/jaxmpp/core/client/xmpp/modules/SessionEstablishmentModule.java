/*
 * SessionEstablishmentModule.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.logging.Logger;

/**
 * Module for <a href='http://xmpp.org/rfcs/rfc3921.html#session'>Session
 * Establishment</a>.
 */
public class SessionEstablishmentModule
		implements XmppModule, ContextAware {

	public static final String SESSION_ESTABLISHED = "jaxmpp#sessionEstablished";
	protected final Logger log;
	private Context context;

	public static boolean isSessionEstablishingAvailable(final SessionObject sessionObject) throws XMLException {

		final Element features = StreamFeaturesModule.getStreamFeatures(sessionObject);

		return features != null && features.getChildrenNS("session", "urn:ietf:params:xml:ns:xmpp-session") != null;
	}

	public SessionEstablishmentModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	public void addSessionEstablishmentErrorHandler(SessionEstablishmentErrorHandler handler) {
		context.getEventBus()
				.addHandler(SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent.class, handler);
	}

	public void addSessionEstablishmentSuccessHandler(SessionEstablishmentSuccessHandler handler) {
		context.getEventBus()
				.addHandler(SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent.class, handler);
	}

	public void establish() throws JaxmppException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = ElementFactory.create("session", null, "urn:ietf:params:xml:ns:xmpp-session");
		iq.addChild(bind);

		context.getWriter().write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				context.getSessionObject().setProperty(SESSION_ESTABLISHED, Boolean.FALSE);
				SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent event = new SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent(
						context.getSessionObject(), error);
				context.getEventBus().fire(event);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
				context.getSessionObject().setProperty(SESSION_ESTABLISHED, Boolean.TRUE);
				SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent event = new SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent(
						context.getSessionObject());
				context.getEventBus().fire(event);
			}

			@Override
			public void onTimeout() throws JaxmppException {
				SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent event = new SessionEstablishmentErrorHandler.SessionEstablishmentErrorEvent(
						context.getSessionObject(), null);
				context.getEventBus().fire(event);
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
		context.getEventBus()
				.remove(SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent.class, handler);
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	public interface SessionEstablishmentErrorHandler
			extends EventHandler {

		void onSessionEstablishmentError(SessionObject sessionObject, ErrorCondition error) throws JaxmppException;

		class SessionEstablishmentErrorEvent
				extends JaxmppEvent<SessionEstablishmentErrorHandler> {

			private ErrorCondition error;

			public SessionEstablishmentErrorEvent(SessionObject sessionObject, ErrorCondition error) {
				super(sessionObject);
				this.error = error;
			}

			@Override
			public void dispatch(SessionEstablishmentErrorHandler handler) throws JaxmppException {
				handler.onSessionEstablishmentError(sessionObject, error);
			}

			public ErrorCondition getError() {
				return error;
			}

			public void setError(ErrorCondition error) {
				this.error = error;
			}

		}
	}

	public interface SessionEstablishmentSuccessHandler
			extends EventHandler {

		void onSessionEstablishmentSuccess(SessionObject sessionObject) throws JaxmppException;

		class SessionEstablishmentSuccessEvent
				extends JaxmppEvent<SessionEstablishmentSuccessHandler> {

			public SessionEstablishmentSuccessEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(SessionEstablishmentSuccessHandler handler) throws JaxmppException {
				handler.onSessionEstablishmentSuccess(sessionObject);
			}

		}
	}

}