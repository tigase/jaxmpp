/*
 * StreamhostUsedCallback.java
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
package tigase.jaxmpp.core.client.xmpp.modules.socks5;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andrzej
 */
public abstract class StreamhostUsedCallback
		implements AsyncCallback {

	private static final Logger log = Logger.getLogger(
			"tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamhostUsedCallback");
	private List<Streamhost> hosts;

	public List<Streamhost> getHosts() {
		return this.hosts;
	}

	public void setHosts(List<Streamhost> hosts) {
		this.hosts = hosts;
	}

	public abstract void onError(Exception ex, String errorText);

	@Override
	public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
		onError(null, error.toString());
	}

	@Override
	public void onSuccess(Stanza responseStanza) throws JaxmppException {
		IQ iq = (IQ) responseStanza;
		Element query = iq.getChildrenNS("query", Socks5BytestreamsModule.XMLNS_BS);
		JID streamhostUsed = JID.jidInstance(query.getFirstChild().getAttribute("jid"));
		boolean connected = false;
		for (Streamhost host : getHosts()) {

			// is it possible that we try to activate same record twice?
			// we get 'connection error' but also 'activation for
			// xxx@sss succeeded'
			// how it is possible?

			if (streamhostUsed.equals(host.getJid())) {
				try {
					log.log(Level.FINEST, "activating stream for = " + host.getJid());
					// if (host.getJid().equals(ft.jid.toString())) {
					// ft.outgoingConnected();
					// } else {
					// ft.connectToProxy(host, null);
					// }
					log.log(Level.FINEST, "activation of stream completed");
					connected = onSuccess(host);
					log.log(Level.FINEST, "connected set to = " + connected);
					if (connected) {
						break;
					}
				} catch (Exception ex) {
					log.log(Level.FINEST, "exception connecting to proxy", ex);
					// stop();
					onError(ex, null);
				}
			}
		}
		if (!connected) {
			log.log(Level.FINEST, "result = " + connected);
			onError(null, "connection error");
		}
	}

	public abstract boolean onSuccess(Streamhost host);

	@Override
	public void onTimeout() throws JaxmppException {
		onError(null, "request timed out");
	}
}
