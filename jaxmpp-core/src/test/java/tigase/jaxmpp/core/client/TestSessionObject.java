package tigase.jaxmpp.core.client;

import java.util.HashMap;

import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public class TestSessionObject extends AbstractSessionObject {

	public TestSessionObject() {
		presence = new PresenceStore();
		properties = new HashMap<String, Object>();
		responseManager = new ResponseManager();
		roster = new RosterStore();
		userProperties = new HashMap<String, Object>();
	}
}
