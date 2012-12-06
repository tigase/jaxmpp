package tigase.jaxmpp.j2se;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tigase.jaxmpp.core.client.ResponseManager;

public class ThreadSafeResponseManager extends ResponseManager {

	private final Map<String, Entry> handlers = new ConcurrentHashMap<String, ResponseManager.Entry>();

	@Override
	protected Map<String, Entry> getHandlers() {
		return this.handlers;
	}

}
