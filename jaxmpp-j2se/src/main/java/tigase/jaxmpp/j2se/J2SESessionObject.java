package tigase.jaxmpp.j2se;

import java.util.concurrent.ConcurrentHashMap;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public class J2SESessionObject extends AbstractSessionObject {

	public J2SESessionObject() {
		properties = new ConcurrentHashMap<String, Entry>();
		responseManager = new ThreadSafeResponseManager();
		roster = new RosterStore();
	}

}
