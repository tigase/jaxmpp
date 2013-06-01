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
package tigase.jaxmpp.core.client.xmpp.modules.socks5;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionEndpoint;

public class Streamhost implements ConnectionEndpoint {

	private String address;
	private JID jid;
	private Integer port;

	public Streamhost(String jid, String address, Integer port) {
		this.jid = JID.jidInstance(jid);
		this.address = address;
		this.port = port;
	}

	@Override
	public String getHost() {                
                int idx;
                if (address != null && (idx = address.indexOf("%")) > 0) {
                        address = address.substring(0, idx);
                }
		return address;
	}

	@Override
	public JID getJid() {
		return jid;
	}

	@Override
	public Integer getPort() {
		return port;
	}

}