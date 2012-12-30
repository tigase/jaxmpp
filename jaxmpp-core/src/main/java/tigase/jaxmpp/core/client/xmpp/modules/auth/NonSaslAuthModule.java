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

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Implementaion of <a href='http://xmpp.org/extensions/xep-0078.html'>XEP-0078:
 * Non-SASL Authentication</a>.
 * 
 */
public class NonSaslAuthModule extends AbstractIQModule {

	public static class NonSaslAuthEvent extends AuthModule.AuthEvent {

		private static final long serialVersionUID = 1L;

		private ErrorCondition error;

		private IQ request;

		public NonSaslAuthEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public ErrorCondition getError() {
			return error;
		}

		public IQ getRequest() {
			return request;
		}

		public void setError(ErrorCondition error) {
			this.error = error;
		}

		public void setRequest(IQ iq) {
			this.request = iq;
		}
	}

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("query", new String[] { "xmlns" }, new String[] { "jabber:iq:auth" }));

	public NonSaslAuthModule(Observable parent, SessionObject sessionObject, PacketWriter packetWriter) {
		super(ObservableFactory.instance(parent), sessionObject, packetWriter);
	}

	protected void fireAuthStart(IQ iq) throws JaxmppException {
		NonSaslAuthEvent event = new NonSaslAuthEvent(AuthModule.AuthStart, sessionObject);
		event.setRequest(iq);
		this.observable.fireEvent(event);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public void login() throws JaxmppException {
		log.fine("Try login with Non-SASL");
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		DefaultElement query = new DefaultElement("query", null, "jabber:iq:auth");
		iq.addChild(query);

		CredentialsCallback callback = sessionObject.getProperty(AuthModule.CREDENTIALS_CALLBACK);
		if (callback == null)
			callback = new AuthModule.DefaultCredentialsCallback(sessionObject);
		BareJID userJID = sessionObject.getProperty(SessionObject.USER_BARE_JID);

		query.addChild(new DefaultElement("username", userJID.getLocalpart(), null));
		query.addChild(new DefaultElement("password", callback.getCredential(), null));
		// query.addChild(new DefaultElement("resource", "x", null));

		fireAuthStart(iq);

		writer.write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				NonSaslAuthModule.this.onError(responseStanza, error);

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
				NonSaslAuthModule.this.onSuccess(responseStanza);
			}

			@Override
			public void onTimeout() throws JaxmppException {
				NonSaslAuthModule.this.onTimeout();
			}
		});
	}

	protected void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
		sessionObject.setProperty(AuthModule.AUTHORIZED, Boolean.FALSE);
		log.fine("Failure with condition: " + error);
		NonSaslAuthEvent event = new NonSaslAuthEvent(AuthModule.AuthFailed, sessionObject);
		event.setError(error);
		observable.fireEvent(AuthModule.AuthFailed, event);
	}

	protected void onSuccess(Stanza responseStanza) throws JaxmppException {
		sessionObject.setProperty(AuthModule.AUTHORIZED, Boolean.TRUE);
		log.fine("Authenticated");
		observable.fireEvent(AuthModule.AuthSuccess, new NonSaslAuthEvent(AuthModule.AuthSuccess, sessionObject));
	}

	protected void onTimeout() throws JaxmppException {
		sessionObject.setProperty(AuthModule.AUTHORIZED, Boolean.FALSE);
		log.fine("Failure because of timeout");
		NonSaslAuthEvent event = new NonSaslAuthEvent(AuthModule.AuthFailed, sessionObject);
		observable.fireEvent(AuthModule.AuthFailed, event);
	}

	@Override
	protected void processGet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}