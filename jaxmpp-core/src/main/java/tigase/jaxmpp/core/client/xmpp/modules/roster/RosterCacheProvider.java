package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.Collection;

import tigase.jaxmpp.core.client.SessionObject;

public interface RosterCacheProvider {

	String getCachedVersion(SessionObject sessionObject);

	Collection<RosterItem> loadCachedRoster(SessionObject sessionObject);

	void updateReceivedVersion(SessionObject sessionObject, String ver);

}
