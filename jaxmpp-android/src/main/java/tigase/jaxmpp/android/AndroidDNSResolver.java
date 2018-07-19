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

import android.util.Log;
import org.xbill.DNS.*;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.Entry;

import java.util.*;

public class AndroidDNSResolver
		implements DnsResolver {

	private static String TAG = "AndroidDNSResolver";

	private final HashMap<String, Entry> cache = new HashMap<String, Entry>();
	private long lastAccess = -1;

	private static Entry resolveSRV(String domain) {
		String hostName = null;
		int hostPort = -1;
		int priority = Integer.MAX_VALUE;
		int weight = 0;
		Lookup lookup;

		Log.v(TAG, "Looking for DNS record of domain " + domain);
		try {
			lookup = new Lookup(domain, Type.SRV);
			lookup.setResolver(new ExtendedResolver());
			Record recs[] = lookup.run();
			if (recs == null) {
				Log.v(TAG, "SRV records for " + domain + " not found.");
				return null;
			}

			Log.v(TAG, "SRV records for " + domain + " found: " + Arrays.toString(recs));

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
		} catch (Exception e) {
			Log.e(TAG, "Problem with resolving " + domain, e);
		}
		if (hostName == null) {
			Log.v(TAG, "Returning null");
			return null;
		} else if (hostName.endsWith(".")) {
			hostName = hostName.substring(0, hostName.length() - 1);
		}
		Log.v(TAG, "Returning " + hostName + ":" + hostPort);
		return new Entry(hostName, hostPort);
	}

	public AndroidDNSResolver() {
	}

	@Override
	public List<Entry> resolve(final String hostname) {
		Log.v(TAG, "Resolving domain " + hostname);

		ArrayList<Entry> result = new ArrayList<Entry>();
		synchronized (cache) {
			long now = (new Date()).getTime();
			if (now - lastAccess > 1000 * 60 * 10) {
				Log.v(TAG, "Clearing cache");
				cache.clear();
			}
			lastAccess = now;
			if (cache.containsKey(hostname)) {
				Entry address = cache.get(hostname);
				if (address != null) {
					result.add(address);
					Log.v(TAG, "Found record for domain " + hostname + " in cache: " + result);
					return result;
				}
			}
		}

		try {
			Entry rr = resolveSRV("_xmpp-client._tcp." + hostname);

			if (rr == null) {
				rr = new Entry(hostname, 5222);
				Log.v(TAG, "Nothing found. Using default entry " + rr);
			}

			synchronized (cache) {
				Log.v(TAG, "Adding entry do cache: " + hostname + "->" + rr);
				cache.put(hostname, rr);
			}

			result.add(rr);
		} catch (Exception e) {
			Log.w(TAG, "Something goes wrong during resolving DNS entry for domain " + hostname, e);
			result.add(new Entry(hostname, 5222));
		}
		return result;
	}

}
