package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.eventbus.EventBus;

public interface EventBusAware {

	void setEventBus(EventBus context);

}
