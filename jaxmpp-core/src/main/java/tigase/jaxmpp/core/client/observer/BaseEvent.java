package tigase.jaxmpp.core.client.observer;

import java.io.Serializable;

public class BaseEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean handled;

	private EventType type;

	public BaseEvent(EventType type) {
		this.type = type;
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

}
