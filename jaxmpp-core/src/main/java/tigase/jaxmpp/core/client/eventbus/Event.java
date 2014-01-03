package tigase.jaxmpp.core.client.eventbus;

public abstract class Event<H extends EventHandler> {

	private Object source;

	protected Event() {
		super();
	}

	protected abstract void dispatch(H handler) throws Exception;

	public Object getSource() {
		return source;
	}

	void setSource(Object source) {
		this.source = source;
	};

}
