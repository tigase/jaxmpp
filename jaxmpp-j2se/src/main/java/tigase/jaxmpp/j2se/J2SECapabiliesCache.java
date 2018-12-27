/*
 * J2SECapabiliesCache.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesCache;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.Identity;

import java.util.*;

/**
 * @author andrzej
 */
public class J2SECapabiliesCache
		implements CapabilitiesCache {

	Map<String, Set<String>> features = new HashMap<String, Set<String>>();

	@Override
	public Set<String> getFeatures(String node) {
		return features.get(node);
	}

	@Override
	public Identity getIdentity(String node) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Set<String> getNodesWithFeature(String feature) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCached(String node) {
		return features.containsKey(node);
	}

	@Override
	public void store(String node, String name, String category, String type, Collection<String> features) {
		this.features.put(node, new HashSet<String>(features));
	}

}
