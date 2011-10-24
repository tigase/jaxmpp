package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.Collection;

public interface RosterCacheProvider {

	String getCachedVersion();

	Collection<RosterItem> loadCachedRoster();

	void updateReceivedVersion(String ver);

}
