package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import java.util.Collection;
import java.util.Set;

import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.Identity;

public interface CapabilitiesCache {

	public Set<String> getFeatures(String node);

	public Identity getIdentity(String node);

	public Set<String> getNodesWithFeature(String feature);

	public boolean isCached(String node);

	public void store(String node, String name, String category, String type, Collection<String> features);

}
