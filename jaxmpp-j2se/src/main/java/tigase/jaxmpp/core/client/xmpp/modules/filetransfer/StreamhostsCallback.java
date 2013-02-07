/*
 * Tigase XMPP Client Library
 * Copyright (C) 2013 "Andrzej Wójcik" <andrzej.wojcik@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.modules.filetransfer;

import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class StreamhostsCallback implements AsyncCallback {

	private Socks5BytestreamsModule socks5Manager;

	public StreamhostsCallback(Socks5BytestreamsModule socks5Manager) {
		this.socks5Manager = socks5Manager;
	}

	public abstract void onStreamhosts(List<Streamhost> hosts) throws JaxmppException;

	@Override
	public void onSuccess(Stanza stanza) throws JaxmppException {
		List<Streamhost> hosts = socks5Manager.processStreamhosts(stanza);
		if (hosts != null) {
			onStreamhosts(hosts);
		}
	}

}