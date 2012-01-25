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
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

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

	private final Observable observable;

	public NonSaslAuthModule(Observable parent, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = new Observable(parent);
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
		query.addChild(new DefaultElement("password", callback.getPassword(), null));
		// query.addChild(new DefaultElement("resource", "x", null));

		fireAuthStart(iq);

		sessionObject.registerResponseHandler(iq, new AsyncCallback() {

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
		writer.write(iq);
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
