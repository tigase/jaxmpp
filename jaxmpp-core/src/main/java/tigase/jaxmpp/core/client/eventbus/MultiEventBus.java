package tigase.jaxmpp.core.client.eventbus;

import java.util.ArrayList;

public class MultiEventBus extends DefaultEventBus {

	private final ArrayList<EventBus> buses = new ArrayList<EventBus>();

	private final EventListener commonListener;

	public MultiEventBus() {
		this.commonListener = new EventListener() {

			@Override
			public void onEvent(Event<? extends EventHandler> event) {
				fire(event, event.getSource());
			}
		};
	}

	public synchronized void addEventBus(EventBus eventBus) {
		this.buses.add(eventBus);
		eventBus.addListener(commonListener);
	}

	public synchronized void removeEventBus(EventBus eventBus) {
		eventBus.remove(commonListener);
		this.buses.remove(eventBus);
	}

}
