package tigase.jaxmpp.core.client;

import java.util.HashMap;

import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public class MockSessionObject extends AbstractSessionObject {

	public MockSessionObject() {
		presence = new MockPresenceStore();
		properties = new HashMap<String, Entry>();
		responseManager = new ResponseManager();
		roster = new RosterStore();
	}
}
