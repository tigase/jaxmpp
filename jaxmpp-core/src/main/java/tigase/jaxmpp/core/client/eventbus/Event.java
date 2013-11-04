package tigase.jaxmpp.core.client.eventbus;

public abstract class Event<H extends EventHandler> {

	private Object source;

	protected final EventType<H> type;

	protected Event(EventType<H> type) {
		super();
		this.type = type;
	}

	protected abstract void dispatch(H handler) throws Exception;

	public Object getSource() {
		return source;
	}

	public EventType<H> getType() {
		return type;
	}

	void setSource(Object source) {
		this.source = source;
	};

}
