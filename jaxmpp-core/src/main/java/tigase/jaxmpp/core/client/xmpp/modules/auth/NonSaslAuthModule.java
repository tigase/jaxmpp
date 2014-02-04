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
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
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

	public interface NonSaslAuthFailedHandler extends EventHandler {

		public static class NonSaslAuthFailedEvent extends JaxmppEvent<NonSaslAuthFailedHandler> {

			private ErrorCondition errorCondition;

			public NonSaslAuthFailedEvent(SessionObject sessionObject, ErrorCondition error) {
				super(sessionObject);
				this.errorCondition = error;
			}

			@Override
			protected void dispatch(NonSaslAuthFailedHandler handler) {
				handler.onAuthFailed(sessionObject, errorCondition);
			}

			public ErrorCondition getErrorCondition() {
				return errorCondition;
			}

			public void setErrorCondition(ErrorCondition errorCondition) {
				this.errorCondition = errorCondition;
			}

		}

		void onAuthFailed(SessionObject sessionObject, ErrorCondition errorCondition);
	}

	public interface NonSaslAuthStartHandler extends EventHandler {

		public static class NonSaslAuthStartEvent extends JaxmppEvent<NonSaslAuthStartHandler> {

			private IQ iq;

			public NonSaslAuthStartEvent(SessionObject sessionObject, IQ iq) {
				super(sessionObject);
				this.iq = iq;
			}

			@Override
			protected void dispatch(NonSaslAuthStartHandler handler) {
				handler.onAuthStart(sessionObject, iq);
			}

			public IQ getIq() {
				return iq;
			}

			public void setIq(IQ iq) {
				this.iq = iq;
			}

		}

		void onAuthStart(SessionObject sessionObject, IQ iq);
	}

	public interface NonSaslAuthSuccessHandler extends EventHandler {

		public static class NonSaslAuthSuccessEvent extends JaxmppEvent<NonSaslAuthSuccessHandler> {

			public NonSaslAuthSuccessEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(NonSaslAuthSuccessHandler handler) {
				handler.onAuthSuccess(sessionObject);
			}

		}

		void onAuthSuccess(SessionObject sessionObject);
	}

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("query", new String[] { "xmlns" }, new String[] { "jabber:iq:auth" }));

	public NonSaslAuthModule(Context context) {
		super(context);
	}

	protected void fireAuthStart(IQ iq) throws JaxmppException {
		NonSaslAuthStartHandler.NonSaslAuthStartEvent event = new NonSaslAuthStartHandler.NonSaslAuthStartEvent(
				context.getSessionObject(), iq);
		fireEvent(event);
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
		Element query = ElementFactory.create("query", null, "jabber:iq:auth");
		iq.addChild(query);

		CredentialsCallback callback = context.getSessionObject().getProperty(AuthModule.CREDENTIALS_CALLBACK);
		if (callback == null)
			callback = new AuthModule.DefaultCredentialsCallback(context.getSessionObject());
		BareJID userJID = context.getSessionObject().getProperty(SessionObject.USER_BARE_JID);

		query.addChild(ElementFactory.create("username", userJID.getLocalpart(), null));
		query.addChild(ElementFactory.create("password", callback.getCredential(), null));
		// query.addChild(ElementFactory.create("resource", "x", null));

		fireAuthStart(iq);

		write(iq, new AsyncCallback() {

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
		context.getSessionObject().setProperty(Scope.stream, AuthModule.AUTHORIZED, Boolean.FALSE);
		log.fine("Failure with condition: " + error);
		NonSaslAuthFailedHandler.NonSaslAuthFailedEvent event = new NonSaslAuthFailedHandler.NonSaslAuthFailedEvent(
				context.getSessionObject(), error);
		fireEvent(event);
	}

	protected void onSuccess(Stanza responseStanza) throws JaxmppException {
		context.getSessionObject().setProperty(Scope.stream, AuthModule.AUTHORIZED, Boolean.TRUE);
		log.fine("Authenticated");
		fireEvent(new NonSaslAuthSuccessHandler.NonSaslAuthSuccessEvent(context.getSessionObject()));
	}

	protected void onTimeout() throws JaxmppException {
		context.getSessionObject().setProperty(Scope.stream, AuthModule.AUTHORIZED, Boolean.FALSE);
		log.fine("Failure because of timeout");
		NonSaslAuthFailedHandler.NonSaslAuthFailedEvent event = new NonSaslAuthFailedHandler.NonSaslAuthFailedEvent(
				context.getSessionObject(), null);
		fireEvent(event);
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