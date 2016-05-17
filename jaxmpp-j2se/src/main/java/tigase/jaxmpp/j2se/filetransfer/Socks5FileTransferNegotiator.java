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
package tigase.jaxmpp.j2se.filetransfer;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesCache;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamInitiationOfferAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.j2se.connection.socks5bytestream.Socks5BytestreamsConnectionManager;
import tigase.jaxmpp.j2se.connection.socks5bytestream.Socks5ConnectionManager;

import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andrzej
 */
public class Socks5FileTransferNegotiator extends FileTransferNegotiatorAbstract implements
		ConnectionManager.ConnectionEstablishedHandler, FileTransferModule.FileTransferRequestHandler {

	private static final Logger log = Logger.getLogger(Socks5FileTransferNegotiator.class.getCanonicalName());
	private final String BASE = "session-";
	private final Socks5BytestreamsConnectionManager connectionManager = new Socks5BytestreamsConnectionManager();
	private final String PACKET_ID = BASE + "initiation-packet-id";
	private final String STREAM_METHOD = BASE + "stream-method";

	@Override
	public void acceptFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft)
			throws JaxmppException {
		FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
		if (ftModule != null) {
			String packetId = ft.getData(PACKET_ID);
			String streamMethod = ft.getData(STREAM_METHOD);
			if (packetId == null) {
				fireOnFailure(ft, null);
			} else if (streamMethod == null) {
				ftModule.rejectStreamInitiation(ft, packetId);
			} else {
				connectionManager.register(jaxmpp, ft);
				ftModule.acceptStreamInitiation(ft, packetId, streamMethod);
			}
		}
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public boolean isSupported(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) {
		Presence p = PresenceModule.getPresenceStore(jaxmpp.getSessionObject()).getPresence(ft.getPeer());
		CapabilitiesModule capsModule = jaxmpp.getModule(CapabilitiesModule.class);
		CapabilitiesCache capsCache = capsModule.getCache();

		try {
			String capsNode = FileTransferManager.getCapsNode(p);
			Set<String> features = (capsCache != null) ? capsCache.getFeatures(capsNode) : null;

			return (true || (features != null && features.contains(Socks5BytestreamsModule.XMLNS_BS) && features.contains(FileTransferModule.XMLNS_SI_FILE)));
		} catch (XMLException ex) {
			return true;
		}
	}

	@Override
	public void onConnectionEstablished(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket)
			throws JaxmppException {
		FileTransfer ft = (FileTransfer) connectionSession;
		if (log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "got ft incoming = {0} with packet id = {1}",
					new Object[]{ft.isIncoming(), ft.getData(Socks5ConnectionManager.PACKET_ID)});
		}
		// if it is incoming file transfer we need to notify peer about used
		// streamhost
		if (ft.isIncoming() && ft.getData(Socks5ConnectionManager.PACKET_ID) != null) {
			connectionManager.sendStreamhostUsed(ft, (String) ft.getData(Socks5ConnectionManager.PACKET_ID));
		}
		// fire notification that connection is established
		if (socket != null) {
			fireOnSuccess(ft);
		}
	}

	@Override
	public void onFileTransferRequest(SessionObject sessionObject,
									  tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer fts, String id, List<String> streamMethods) {
		FileTransfer ft = new FileTransfer(fts.getSessionObject(), fts.getPeer(), fts.getSid());
		ft.setFileInfo(fts.getFilename(), fts.getFileSize(), fts.getFileModification(), fts.getFileMimeType());
		ft.setData(PACKET_ID, id);
		ft.setData(STREAM_METHOD, streamMethods.get(0));
		fireOnRequest(sessionObject, ft);
	}

	@Override
	public void registerListeners(JaxmppCore jaxmpp) {
		jaxmpp.getModule(FileTransferModule.class).addFileTransferRequestHandler(this);
	}

	@Override
	public void rejectFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft)
			throws JaxmppException {
		FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
		if (ftModule != null) {
			String packetId = ft.getData(PACKET_ID);
			if (packetId == null) {
				fireOnFailure(ft, null);
			} else {
				ftModule.rejectStreamInitiation(ft, packetId);
			}
		}
	}

	@Override
	public void sendFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft)
			throws JaxmppException {
		FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
		if (ftModule != null) {
			connectionManager.initConnection(jaxmpp, ft, new ConnectionManager.InitializedCallback() {
				@Override
				public void initialized(JaxmppCore jaxmpp, ConnectionSession session) {
					try {
						sendFile2(jaxmpp, (FileTransfer) session);
					} catch (JaxmppException ex) {
						fireOnFailure((FileTransfer) session, ex);
					}
				}
			});
			// ftModule.sendStreamInitiationOffer(ft, new String[] {
			// FileTransferModule.XMLNS_BS });
			return;
		}

		fireOnFailure(ft, null);
	}

	public void sendFile2(final JaxmppCore jaxmpp, final FileTransfer ft) throws JaxmppException {
		FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
		if (ftModule != null) {
			ftModule.sendStreamInitiationOffer(ft, new String[]{Socks5BytestreamsModule.XMLNS_BS},
					new StreamInitiationOfferAsyncCallback(ft.getSid()) {
						@Override
						public void onAccept(String sid) {
							try {
								connectionManager.connectTcp(jaxmpp, ft);
							} catch (JaxmppException ex) {
								fireOnFailure(ft, ex);
							}
						}

						@Override
						public void onError() {
							fireOnFailure(ft, null);
						}

						@Override
						public void onReject() {
							fireOnReject(ft);
						}
					});
		}
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		connectionManager.setContext(context);
		context.getEventBus().addHandler(ConnectionManager.ConnectionEstablishedHandler.ConnectionEstablishedEvent.class, this);
	}

	@Override
	public void unregisterListeners(JaxmppCore jaxmpp) {
		jaxmpp.getModule(FileTransferModule.class).removeFileTransferRequestHandler(this);
	}
}
