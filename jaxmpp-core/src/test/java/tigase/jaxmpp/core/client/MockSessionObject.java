package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.eventbus.EventBus;

import java.util.HashMap;
//import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
//import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
//import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public class MockSessionObject extends AbstractSessionObject {

	public MockSessionObject(EventBus eventBus) {
		super();
		setEventBus(eventBus);
		properties = new HashMap<String, Entry>();

//		PresenceModule.setPresenceStore(this, new MockPresenceStore());
//		RosterModule.setRosterStore(this, new RosterStore());
		ResponseManager.setResponseManager(this, new ResponseManager());
	}

}
