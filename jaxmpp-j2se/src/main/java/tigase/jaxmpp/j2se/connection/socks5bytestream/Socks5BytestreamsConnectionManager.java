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

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Streamhost;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamhostUsedCallback;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamhostsCallback;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 * 
 * @author andrzej
 */
public class Socks5BytestreamsConnectionManager extends Socks5ConnectionManager {

	private static final Logger log = Logger.getLogger(Socks5BytestreamsConnectionManager.class.getCanonicalName());

	@Override
	public void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
		// fireOnSuccess(ft);
		session.setData(JAXMPP_KEY, jaxmpp);
		JID proxyJid = session.getData(PROXY_JID_KEY);
		if (proxyJid != null) {
			requestStreamHosts(jaxmpp, session, proxyJid);
		} else {
			sendStreamHosts(jaxmpp, session, null);
		}
	}

	@Override
	public void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback)
			throws JaxmppException {
		session.setData(JAXMPP_KEY, jaxmpp);
		discoverProxy(jaxmpp, session, callback);
	}

	public void register(final JaxmppCore jaxmpp, final ConnectionSession session) {
		session.setData(JAXMPP_KEY, jaxmpp);
		final Socks5BytestreamsModule socks5Module = jaxmpp.getModule(Socks5BytestreamsModule.class);

		Socks5BytestreamsModule.StreamhostsHandler handler = new Socks5BytestreamsModule.StreamhostsHandler() {

			@Override
			public void onStreamhostsHandler(SessionObject sessionObject, JID from, String id, String sid,
					List<Streamhost> hosts) throws JaxmppException {
				session.setData(PACKET_ID, id);
				if (session.getSid().equals(sid)) {
					jaxmpp.getEventBus().remove(this);
					for (Streamhost host : hosts) {
						try {
							connectToProxy(jaxmpp, session, session.getSid(), host);
							break;
						} catch (IOException ex) {
						}
					}
				}
			}

		};
		jaxmpp.getEventBus().addHandler(Socks5BytestreamsModule.StreamhostsHandler.StreamhostsEvent.class, handler);
	}

	protected void requestStreamHosts(final JaxmppCore jaxmpp, final ConnectionSession session, final JID proxyJid)
			throws JaxmppException {
		Socks5BytestreamsModule socks5Module = jaxmpp.getModule(Socks5BytestreamsModule.class);
		socks5Module.requestStreamhosts(proxyJid, new StreamhostsCallback(socks5Module) {

			@Override
			public void onError(Stanza responseStanza, XMPPException.ErrorCondition error) throws JaxmppException {
				sendStreamHosts(jaxmpp, session, null);
			}

			@Override
			public void onStreamhosts(List<Streamhost> hosts) throws JaxmppException {
				sendStreamHosts(jaxmpp, session, hosts);
			}

			@Override
			public void onTimeout() throws JaxmppException {
				sendStreamHosts(jaxmpp, session, null);
			}

		});
	}

	protected void sendStreamHosts(final JaxmppCore jaxmpp, final ConnectionSession ft, List<Streamhost> proxyStreamhosts)
			throws JaxmppException {
		Socks5BytestreamsModule socks5Module = jaxmpp.getModule(Socks5BytestreamsModule.class);
		List<Streamhost> streamhosts = getLocalStreamHosts(ft, ft.getSid());
		if (proxyStreamhosts != null) {
			streamhosts.addAll(proxyStreamhosts);
		}

		StreamhostUsedCallback streamhostUsedCallback = new StreamhostUsedCallback() {

			@Override
			public void onError(Exception ex, String errorText) {
				log.log(Level.SEVERE, errorText, ex);
				// fireOnFailure(ft, ex);
			}

			@Override
			public boolean onSuccess(Streamhost host) {
				System.out.println("streamhost-used = " + host.getJid());
				if (host.getJid().equals(ft.getSessionObject().getBindedJid())) {
					System.out.println("streamhost-used = 'local'");
					synchronized (ft) {
						Socket socket = ft.getData("socket");
						if (socket != null) {
							fireOnConnected(ft, socket);
						} else {
							ft.setData("streamhost-received", true);
						}
					}
				} else {
					try {
						connectToProxy(jaxmpp, ft, ft.getSid(), host);
					} catch (Exception ex) {
						log.log(Level.WARNING, "exception while connecting to proxy", ex);
						return false;
					}
				}

				return true;
			}

		};

		streamhostUsedCallback.setHosts(streamhosts);

		socks5Module.sendStreamhosts(ft.getPeer(), ft.getSid(), streamhosts, streamhostUsedCallback);

	}

	public void sendStreamhostUsed(FileTransfer ft, String packetId) throws JaxmppException {
		JaxmppCore jaxmpp = ft.getData(JAXMPP_KEY);
		Streamhost streamhost = ft.getData(STREAMHOST_KEY);
		jaxmpp.getModule(Socks5BytestreamsModule.class).sendStreamhostUsed(ft.getPeer(), packetId, ft.getSid(), streamhost);
	}

}
