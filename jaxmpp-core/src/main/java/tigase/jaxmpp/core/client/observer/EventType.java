package tigase.jaxmpp.core.client.observer;

import java.io.Serializable;

/**
 * Base class for all events type.
 * 
 * @author bmalkow
 * 
 */
public class EventType implements Serializable {

	private static int counter = 0;

	private static final long serialVersionUID = 3511154964022649735L;

	private final int id;

	public EventType() {
		this.id = ++counter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof EventType))
			return false;
		return ((EventType) obj).id == id;
	}

	@Override
	public int hashCode() {
		return ("event" + id).hashCode();
	}

}
