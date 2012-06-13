package tigase.jaxmpp.core.client.observer;

import java.io.Serializable;

import tigase.jaxmpp.core.client.SessionObject;

/**
 * Base class for all events in Jaxmpp.
 * 
 * @author bmalkow
 * 
 */
public class BaseEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean handled;

	private final SessionObject sessionObject;

	private final EventType type;

	public BaseEvent(EventType type, SessionObject sessionObject) {
		this.type = type;
		this.sessionObject = sessionObject;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	/**
	 * Returns the type of event.
	 * 
	 * @return the event type
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Returns <code>true</code> if event was handled by any listener.
	 * 
	 * @return <code>true</code> if event was handled
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * <code>true</code> if event was handled by listener. This method is called
	 * by {@linkplain Observable Observable}
	 * 
	 * @param b
	 */
	public void setHandled(boolean b) {
		this.handled = b;

	}

}
