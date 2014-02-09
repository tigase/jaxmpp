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

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesCache;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule;
import tigase.jaxmpp.core.client.xmpp.utils.MutableBoolean;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Transport;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.j2se.connection.ConnectionSessionHandler;
import tigase.jaxmpp.j2se.connection.socks5bytestream.JingleSocks5BytestreamsConnectionManager;
import tigase.jaxmpp.j2se.connection.socks5bytestream.Socks5ConnectionManager;

/**
 * 
 * @author andrzej
 */
public class JingleFileTransferNegotiator extends FileTransferNegotiatorAbstract implements ConnectionSessionHandler,
		JingleModule.JingleSessionAcceptHandler, JingleModule.JingleSessionInitiationHandler,
		JingleModule.JingleSessionTerminateHandler, ConnectionManager.ConnectionEstablishedHandler {

	private static DateTimeFormat dateTimeFormat = new DateTimeFormat();
	public static final String JINGLE_FT_XMLNS = "urn:xmpp:jingle:apps:file-transfer:3";
	private static final String[] FEATURES = { JINGLE_FT_XMLNS, JingleSocks5BytestreamsConnectionManager.XMLNS };
	private static final Logger log = Logger.getLogger(JingleFileTransferNegotiator.class.getCanonicalName());
	private static final long TIMEOUT = 5 * 60 * 1000;
	private static final String TRANSPORTS_KEY = "transports-key";
	private final JingleSocks5BytestreamsConnectionManager connectionManager = new JingleSocks5BytestreamsConnectionManager(
			this);
	private Map<String, FileTransfer> sessions = Collections.synchronizedMap(new HashMap<String, FileTransfer>());
	private final Timer timer = new Timer();

	@Override
	public void acceptFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft)
			throws JaxmppException {
		final String sid = ft.getSid();
		sessions.put(sid, (FileTransfer) ft);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sessions.remove(sid);
			}
		}, TIMEOUT);

		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		jingleModule.acceptSession(ft.getPeer(), sid, "ex", ElementFactory.create("description", null, JINGLE_FT_XMLNS), null);

		connectionManager.connectTcp(jaxmpp, ft);
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	public ConnectionSession getSession(String sid) {
		return sessions.get(sid);
	}

	protected List<Transport> getTransports(JaxmppCore jaxmpp, FileTransfer ft) throws XMLException, JaxmppException {
		List<Transport> transports = ft.getData(TRANSPORTS_KEY);
		if (transports == null) {
			transports = new ArrayList<Transport>();

			Transport transport = this.connectionManager.getTransport(jaxmpp, ft);
			if (transport != null) {
				transports.add(transport);
			}

			ft.setData(TRANSPORTS_KEY, transports);
		}

		return transports;
	}

	@Override
	public boolean isSupported(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) {
		Presence p = PresenceModule.getPresenceStore(jaxmpp.getSessionObject()).getPresence(ft.getPeer());
		CapabilitiesModule capsModule = jaxmpp.getModule(CapabilitiesModule.class);
		CapabilitiesCache capsCache = capsModule.getCache();

		try {
			String capsNode = FileTransferManager.getCapsNode(p);
			Set<String> features = (capsCache != null) ? capsCache.getFeatures(capsNode) : null;

			String featuresStr = "for " + ft.getPeer().toString() + " for caps = " + capsNode + " got = ";
			if (features != null) {
				for (String feature : features) {
					featuresStr += "\n" + feature;
				}
			}

			return (features != null && features.contains(JingleModule.JINGLE_XMLNS) && features.contains(JINGLE_FT_XMLNS) && features.contains(JingleSocks5BytestreamsConnectionManager.XMLNS));
		} catch (XMLException ex) {
			return false;
		}
	}

	@Override
	public void onConnectionEstablished(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket)
			throws JaxmppException {
		FileTransfer ft = (FileTransfer) connectionSession;
		if (log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "got ft incoming = {0} with packet id = {1}",
					new Object[] { ft.isIncoming(), ft.getData(Socks5ConnectionManager.PACKET_ID) });
		}
		// fire notification that connection is established
		if (socket != null) {
			fireOnSuccess(ft);
		}
	}

	@Override
	public void onJingleSessionAccept(SessionObject sessionObject, JID sender, String sid, Element description,
			List<Transport> transports, MutableBoolean handled) {
		if (sessions.containsKey(sid)) {
			handled.setValue(true);
			log.log(Level.FINER, "jingle session accepted");
		}
	}

	@Override
	public void onJingleSessionInitiation(SessionObject sessionObject, JID sender, String sid, Element desc,
			List<Transport> transports, MutableBoolean handled) {
		try {
			if (!JINGLE_FT_XMLNS.equals(desc.getXMLNS())) {
				return;
			}

			handled.setValue(true);

			Element file = null;

			List<Element> elems = desc.getChildren();
			for (Element e : elems) {
				if ("offer".equals(e.getName())) {
					for (Element f : e.getChildren()) {
						if ("file".equals(f.getName())) {
							file = f;
							break;
						}
					}
					break;
				}
			}

			if (file == null) {
				log.log(Level.WARNING, "received file request but without file description = " + desc.getAsString());
				return;
			}

			String name = null;
			Long filesize = null;
			String mimetype = null;
			Date lastModified = null;

			for (Element child : file.getChildren()) {
				String elName = child.getName();
				if ("name".equals(elName)) {
					name = child.getValue();
				} else if ("size".equals(elName)) {
					filesize = Long.parseLong(child.getValue());
				}
			}

			FileTransfer ft = new FileTransfer(sessionObject, sender, sid);
			ft.setFileInfo(name, filesize, lastModified, mimetype);

			ft.setData(TRANSPORTS_KEY, transports);

			fireOnRequest(sessionObject, ft);
		} catch (JaxmppException ex) {
			log.log(Level.SEVERE, "Exception during processing JingleSessionInitiation", ex);
			handled.setValue(false);
		}
	}

	@Override
	public void onJingleSessionTerminate(SessionObject sessionObject, JID sender, String sid, MutableBoolean handled) {
		FileTransfer ft = sessions.get(sid);
		if (ft == null) {
			return;
		}
		handled.setValue(true);
		if (ft.getTransferredBytes() > 0) {
			log.log(Level.FINE, "transfer finished");
		} else {
			fireOnFailure(ft, null);
		}
	}

	@Override
	public void registerListeners(JaxmppCore jaxmpp) {
		jaxmpp.getEventBus().addHandler(JingleModule.JingleSessionInitiationHandler.JingleSessionInitiationEvent.class, this);
		jaxmpp.getEventBus().addHandler(JingleModule.JingleSessionAcceptHandler.JingleSessionAcceptEvent.class, this);
		jaxmpp.getEventBus().addHandler(JingleModule.JingleSessionTerminateHandler.JingleSessionTerminateEvent.class, this);
	}

	@Override
	public void rejectFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft)
			throws JaxmppException {
		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		sessions.remove(ft.getSid());
		jingleModule.terminateSession(ft.getPeer(), ft.getSid(), ft.getPeer());
	}

	@Override
	public void sendFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft)
			throws JaxmppException {
		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		final String sid = ft.getSid();// UUID.randomUUID().toString();

		// creation of session description
		Element description = ElementFactory.create("description");
		description.setXMLNS(JINGLE_FT_XMLNS);

		Element offer = ElementFactory.create("offer");
		description.addChild(offer);

		connectionManager.initConnection(jaxmpp, ft, null);

		Element file = ElementFactory.create("file");
		file.addChild(ElementFactory.create("name", file.getName(), null));
		file.addChild(ElementFactory.create("size", String.valueOf(ft.getFileSize()), null));
		if (ft.getFileModification() != null) {
			file.addChild(ElementFactory.create("date", dateTimeFormat.format(ft.getFileModification()), null));
		}
		offer.addChild(file);

		List<Transport> transports = getTransports(jaxmpp, (FileTransfer) ft);
		sessions.put(sid, (FileTransfer) ft);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sessions.remove(sid);
			}
		}, TIMEOUT);

		jingleModule.initiateSession(ft.getPeer(), sid, "ex", description, transports);
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		connectionManager.setContext(context);
		context.getEventBus().addHandler(ConnectionManager.ConnectionEstablishedHandler.ConnectionEstablishedEvent.class,
				connectionManager, this);
	}

	@Override
	public void unregisterListeners(JaxmppCore jaxmpp) {
		jaxmpp.getEventBus().remove(JingleModule.JingleSessionInitiationHandler.JingleSessionInitiationEvent.class, this);
		jaxmpp.getEventBus().remove(JingleModule.JingleSessionAcceptHandler.JingleSessionAcceptEvent.class, this);
		jaxmpp.getEventBus().remove(JingleModule.JingleSessionTerminateHandler.JingleSessionTerminateEvent.class, this);
	}
}
