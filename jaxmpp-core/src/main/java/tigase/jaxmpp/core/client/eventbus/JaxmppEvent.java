package tigase.jaxmpp.core.client.eventbus;

import tigase.jaxmpp.core.client.SessionObject;

public abstract class JaxmppEvent<H extends EventHandler> extends Event<H> {

	protected final SessionObject sessionObject;

	protected JaxmppEvent(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

}
