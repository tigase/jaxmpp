package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.eventbus.EventBus;

public interface Context {

	EventBus getEventBus();

	SessionObject getSessionObject();

	PacketWriter getWriter();

}
