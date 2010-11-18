package tigase.jaxmpp.core.client.observer;

import java.io.Serializable;

import tigase.jaxmpp.core.client.SessionObject;

public class BaseEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean handled;

	private SessionObject sessionObject;

	private EventType type;

	public BaseEvent(EventType type, SessionObject sessionObject) {
		this.type = type;
		this.sessionObject = sessionObject;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public EventType getType() {
		return type;
	}

	public boolean isHandled() {
		return handled;
	}

	void setHandled(boolean b) {
		this.handled = b;

	}

	public void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}
}
