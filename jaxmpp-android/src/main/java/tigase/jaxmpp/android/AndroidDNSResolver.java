/*
 * AndroidDNSResolver.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.android;

import org.xbill.DNS.*;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AndroidDNSResolver
		implements DnsResolver {

	private final HashMap<String, Entry> cache = new HashMap<String, Entry>();
	private long lastAccess = -1;

	private static Entry resolveSRV(String domain) {
		String hostName = null;
		int hostPort = -1;
		int priority = Integer.MAX_VALUE;
		int weight = 0;
		Lookup lookup;

		try {
			lookup = new Lookup(domain, Type.SRV);
			Record recs[] = lookup.run();
			if (recs == null) {
				return null;
			}
			for (Record rec : recs) {
				SRVRecord record = (SRVRecord) rec;
				if (record != null && record.getTarget() != null) {
					int _weight = (int) (record.getWeight() * record.getWeight() * Math.random());
					if (record.getPriority() < priority) {
						priority = record.getPriority();
						weight = _weight;
						hostName = record.getTarget().toString();
						hostPort = record.getPort();
					} else if (record.getPriority() == priority) {
						if (_weight > weight) {
							priority = record.getPriority();
							weight = _weight;
							hostName = record.getTarget().toString();
							hostPort = record.getPort();
						}
					}
				}
			}
		} catch (TextParseException e) {
		} catch (NullPointerException e) {
		}
		if (hostName == null) {
			return null;
		} else if (hostName.endsWith(".")) {
			hostName = hostName.substring(0, hostName.length() - 1);
		}
		return new Entry(hostName, hostPort);
	}

	public AndroidDNSResolver() {
	}

	@Override
	public List<Entry> resolve(final String hostname) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		synchronized (cache) {
			long now = (new Date()).getTime();
			if (now - lastAccess > 1000 * 60 * 10) {
				cache.clear();
			}
			lastAccess = now;
			if (cache.containsKey(hostname)) {
				Entry address = cache.get(hostname);
				if (address != null) {
					result.add(address);
					return result;
				}
			}
		}

		try {
			Entry rr = resolveSRV("_xmpp-client._tcp." + hostname);

			if (rr == null) {
				rr = new Entry(hostname, 5222);
			}

			synchronized (cache) {
				cache.put(hostname, rr);
			}

			result.add(rr);
		} catch (Exception e) {
			result.add(new Entry(hostname, 5222));
		}
		return result;
	}

}
