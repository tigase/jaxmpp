package tigase.jaxmpp.j2se;

import java.util.concurrent.ConcurrentHashMap;

import tigase.jaxmpp.core.client.AbstractSessionObject;

public class J2SESessionObject extends AbstractSessionObject {

	public J2SESessionObject() {
		properties = new ConcurrentHashMap<String, Entry>();
	}

}
