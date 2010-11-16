package tigase.jaxmpp.core.client.observer;

import java.util.EventListener;

public interface Listener<E extends BaseEvent> extends EventListener {

	public void handleEvent(E be);

}
