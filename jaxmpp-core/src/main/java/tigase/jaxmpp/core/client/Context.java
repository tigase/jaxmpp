package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.eventbus.EventBus;

public interface Context {

	SessionObject getSessionObject();

	EventBus getEventBus();

	PacketWriter getWriter();

}
