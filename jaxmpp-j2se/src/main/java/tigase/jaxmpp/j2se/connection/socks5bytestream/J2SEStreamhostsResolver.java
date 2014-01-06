/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
package tigase.jaxmpp.j2se.connection.socks5bytestream;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Streamhost;

/**
 * 
 * @author andrzej
 */
public class J2SEStreamhostsResolver implements StreamhostsResolver {

	@Override
	public List<Streamhost> getLocalStreamHosts(JID jid, int port) {
		List<Streamhost> hosts = new ArrayList<Streamhost>();
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				if (netint.isLoopback())
					continue;
				if (!netint.isUp())
					continue;

				Enumeration<InetAddress> addrs = netint.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					hosts.add(new Streamhost(jid.toString(), addr.getHostAddress(), port));
				}
			}
		} catch (SocketException ex) {
			Logger.getLogger(J2SEStreamhostsResolver.class.getName()).log(Level.SEVERE, null, ex);
		}
		return hosts;
	}

}
