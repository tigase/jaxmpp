package tigase.jaxmpp.core.client.observer;

import java.io.Serializable;

/**
 * Base class for all events in Jaxmpp.
 * 
 * @author bmalkow
 * 
 */
public class BaseEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean handled;

	private EventType type;

	public BaseEvent(EventType type) {
		this.type = type;
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
	void setHandled(boolean b) {
		this.handled = b;

	}

}
