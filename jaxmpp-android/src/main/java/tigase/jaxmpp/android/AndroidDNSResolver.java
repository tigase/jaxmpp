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
import org.minidns.hla.DnssecResolverApi;
import org.minidns.hla.SrvResolverResult;
import org.minidns.hla.SrvType;
import org.minidns.record.SRV;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.Entry;

import java.io.IOException;
import java.util.*;

public class AndroidDNSResolver
		implements DnsResolver {

	private static final String TAG = "AndroidDNSResolver";

	private final HashMap<String, List<Entry>> cache = new HashMap<String, List<Entry>>();

	private long lastAccess = -1;

	@Override
	public List<Entry> resolve(final String hostname) {
		Log.v(TAG, "Resolving domain " + hostname);

		synchronized (cache) {
			final long now = (new Date()).getTime();
			if (now - lastAccess > 1000 * 30) {
				Log.v(TAG, "Clearing cache");
				cache.clear();
			}
			if (cache.containsKey(hostname)) {
				List<Entry> cached = cache.get(hostname);
				if (cached != null) {
					Log.v(TAG, "Found record for domain " + hostname + " in cache: " + cached);
					return cached;
				}
			}
		}
		final ArrayList<Entry> result = new ArrayList<Entry>();

		try {
			ArrayList<Entry> rr = resolveSRV(hostname);

			if (rr.isEmpty()) {
				rr.add(new Entry(hostname, 5222));
				Log.v(TAG, "Nothing found. Using default entry " + rr);
			}

			synchronized (cache) {
				Log.v(TAG, "Adding entry do cache: " + hostname + "->" + rr);
				cache.put(hostname, rr);
				lastAccess = (new Date()).getTime();
			}

			result.addAll(rr);
		} catch (Exception e) {
			Log.w(TAG, "Something goes wrong during resolving DNS entry for domain " + hostname, e);
			result.add(new SocketConnector.Entry(hostname, 5222));
		}

		return result;
	}

	private ArrayList<Entry> resolveSRV(final String domain) throws IOException {
		Log.v(TAG, "Looking for DNS record of domain " + domain);
		final ArrayList<Entry> result = new ArrayList<Entry>();

		SrvResolverResult resolverResult = DnssecResolverApi.INSTANCE.resolveSrv(SrvType.xmpp_client, domain);

		if (!resolverResult.wasSuccessful()) {
			Log.v(TAG, "Requesting DNS not successful. (" + resolverResult.getResponseCode() + ")");
			return result;
		}
//			if (!result.isAuthenticData) {
//				Log.v(TAG, "Response was not secured with DNSSEC.")
//				return null
//			}

		Set<SRV> srvRecords = resolverResult.getAnswersOrEmptySet();

		if (srvRecords.isEmpty()) {
			Log.v(TAG, "DNS response is empty");
			return result;
		}
		Log.v(TAG, "DNS response: " + srvRecords);

		for (SRV srv : srvRecords) {
			String hostName = srv.target.toString();
			int hostPort = srv.port;

			result.add(new Entry(hostName, hostPort));
		}

		Log.v(TAG, "Returning " + result);
		return result;
	}
}
