package tigase.jaxmpp.j2se;

import java.util.concurrent.ConcurrentHashMap;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public class J2SESessionObject extends AbstractSessionObject {

	public J2SESessionObject() {
		presence = new J2SEPresenceStore();
		properties = new ConcurrentHashMap<String, Entry>();
		responseManager = new ThreadSafeResponseManager();
		roster = new RosterStore();
	}

}
