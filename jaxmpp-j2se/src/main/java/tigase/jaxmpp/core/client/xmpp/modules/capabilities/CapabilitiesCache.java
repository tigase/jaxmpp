package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import java.util.Collection;
import java.util.Set;

public interface CapabilitiesCache {

	public Set<String> getFeatures(String node);

	public String getIdentity(String node);

	public boolean isCached(String node);

	public void store(String node, String name, String category, String type, Collection<String> features);

}
