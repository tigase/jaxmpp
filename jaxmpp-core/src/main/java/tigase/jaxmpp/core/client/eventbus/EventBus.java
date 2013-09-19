package tigase.jaxmpp.core.client.eventbus;

public abstract class EventBus {

	public abstract <H extends EventHandler> void addHandler(EventType<H> type, H handler);

	public abstract <H extends EventHandler> void addHandler(EventType<H> type, Object source, H handler);

	public abstract <H extends EventHandler> void addListener(EventListener listener);

	public abstract <H extends EventHandler> void addListener(EventType<H> type, EventListener listener);

	public abstract <H extends EventHandler> void addListener(EventType<H> type, Object source, EventListener listener);

	public abstract void fire(Event<?> e);

	public abstract void fire(Event<?> e, Object source);

	public abstract void remove(EventHandler handler);

	public abstract void remove(EventType<?> type, EventHandler handler);

	public abstract void remove(EventType<?> type, Object source, EventHandler handler);

	protected void setEventSource(Event<EventHandler> event, Object source) {
		event.setSource(source);
	}
}
