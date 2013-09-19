package tigase.jaxmpp.core.client.eventbus;

public interface EventListener extends EventHandler {

	void onEvent(Event<? extends EventHandler> event);

}
