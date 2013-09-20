package tigase.jaxmpp.core.client.eventbus;

import tigase.jaxmpp.core.client.SessionObject;

public abstract class JaxmppEvent<H extends EventHandler> extends Event<H> {

	protected final SessionObject sessionObject;

	protected JaxmppEvent(EventType<H> type, SessionObject sessionObject) {
		super(type);
		this.sessionObject = sessionObject;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

}
