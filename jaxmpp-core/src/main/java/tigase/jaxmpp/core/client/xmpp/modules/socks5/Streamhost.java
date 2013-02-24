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

public class Streamhost {

	private String address;
	private String jid;
	private Integer port;

	public Streamhost(String jid, String address, Integer port) {
		this.jid = jid;
		this.address = address;
		this.port = port;
	}

	public String getAddress() {                
                int idx;
                if (address != null && (idx = address.indexOf("%")) > 0) {
                        address = address.substring(0, idx);
                }
		return address;
	}

	public String getJid() {
		return jid;
	}

	public Integer getPort() {
		return port;
	}

}