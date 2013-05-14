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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesCache;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferEvent;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferModule;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Candidate;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionAcceptEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionInitiationEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionTerminateEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleTransportInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Transport;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.j2se.connection.ConnectionEvent;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.j2se.connection.ConnectionManager.InitializedCallback;
import tigase.jaxmpp.j2se.connection.ConnectionSessionHandler;
import tigase.jaxmpp.j2se.connection.socks5bytestream.JingleSocks5BytestreamsConnectionManager;
import tigase.jaxmpp.j2se.connection.socks5bytestream.Socks5BytestreamsConnectionManager;

/**
 *
 * @author andrzej
 */
public class JingleFileTransferNegotiator extends FileTransferNegotiatorAbstract implements ConnectionSessionHandler {

	private static final Logger log = Logger.getLogger(JingleFileTransferNegotiator.class.getCanonicalName());
	
	public static final String JINGLE_FT_XMLNS = "urn:xmpp:jingle:apps:file-transfer:3";

	private static final String TRANSPORTS_KEY = "transports-key";
	
	private static DateTimeFormat dateTimeFormat = new DateTimeFormat();
	
    private final JingleSocks5BytestreamsConnectionManager connectionManager = new JingleSocks5BytestreamsConnectionManager(this);
	
	private final Listener<JingleSessionAcceptEvent> sessionAcceptListener = new Listener<JingleSessionAcceptEvent>() {

		@Override
		public void handleEvent(JingleSessionAcceptEvent be) throws JaxmppException {
			if (sessions.containsKey(be.getSid())) {
				be.setJingleHandled(true);
			
				log.log(Level.FINER, "jingle session accepted");			
			}
		}
		
	};
	
	private final Listener<JingleSessionInitiationEvent> sessionInitiationListener = new Listener<JingleSessionInitiationEvent>() {

		@Override
		public void handleEvent(JingleSessionInitiationEvent be) throws JaxmppException {
			Element desc = be.getDescription();
			
			if (!JINGLE_FT_XMLNS.equals(desc.getXMLNS())) 
				return;
			
			be.setJingleHandled(true);
			
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
				}
				else if ("size".equals(elName)) {
					filesize = Long.parseLong(child.getValue());
				}
			}
			
			FileTransfer ft = new FileTransfer(be.getSessionObject(), be.getSender(), be.getSid());
            ft.setFileInfo(name, filesize, lastModified, mimetype);

			ft.setData(TRANSPORTS_KEY, be.getTransports());
			
			FileTransferEvent ev = new FileTransferEvent(NEGOTIATION_REQUEST, be.getSessionObject(), ft);
			fireOnRequest(ev);
		}

	};

	private final Listener<JingleSessionTerminateEvent> sessionTerminateListener = new Listener<JingleSessionTerminateEvent>() {

		@Override
		public void handleEvent(JingleSessionTerminateEvent be) throws JaxmppException {
			FileTransfer ft = sessions.get(be.getSid());
			if (ft == null) return;
			be.setJingleHandled(true);
			if (ft.getTransferredBytes() > 0) {
				log.log(Level.FINE, "transfer finished");
			}
			else {
				fireOnFailure(ft, null);
			}
		}
		
	};
	
	private final Listener<ConnectionEvent> connectionEstablishedListener = new Listener<ConnectionEvent>() {
		@Override
		public void handleEvent(ConnectionEvent be) throws JaxmppException {
			FileTransfer ft = (FileTransfer) be.getConnectionSession();
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "got ft incoming = {0} with packet id = {1}", new Object[]{ft.isIncoming(), ft.getData(Socks5BytestreamsConnectionManager.PACKET_ID)});
			}
			// fire notification that connection is established
			if (be.getType() == ConnectionManager.CONNECTION_ESTABLISHED && be.getSocket() != null) {
				fireOnSuccess(ft);
			}
		}
	};
	
	private final Timer timer = new Timer();
	
	private static final String[] FEATURES = { JINGLE_FT_XMLNS, JingleSocks5BytestreamsConnectionManager.XMLNS };
	private static final long TIMEOUT = 5 * 60 * 1000;
	private Map<String, FileTransfer> sessions = Collections.synchronizedMap(new HashMap<String,FileTransfer>());
	
	@Override
	public void setObservable(Observable observableParent) {
		super.setObservable(observableParent);
		connectionManager.setObservable(observable);
		observable.addListener(ConnectionManager.CONNECTION_ESTABLISHED, connectionEstablishedListener);
	}
	
	@Override
	public String[] getFeatures() {
		return FEATURES;
	}
	
	@Override
	public boolean isSupported(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) {
		Presence p = ft.getSessionObject().getPresence().getPresence(ft.getPeer());
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
			
			return (features != null && features.contains(JingleModule.JINGLE_XMLNS) 
					&& features.contains(JINGLE_FT_XMLNS) 
					&& features.contains(JingleSocks5BytestreamsConnectionManager.XMLNS));
		} catch (XMLException ex) {
			return false;
		}
	}
	
	
	@Override
	public void sendFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) throws JaxmppException {
		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		final String sid = ft.getSid();//UUID.randomUUID().toString();

		// creation of session description
		Element description = new DefaultElement("description");
		description.setXMLNS(JINGLE_FT_XMLNS);

		Element offer = new DefaultElement("offer");
		description.addChild(offer);

		connectionManager.initConnection(jaxmpp, ft, null);
		
		Element file = new DefaultElement("file");
		file.addChild(new DefaultElement("name", file.getName(), null));
		file.addChild(new DefaultElement("size", String.valueOf(ft.getFileSize()), null));
		if (ft.getFileModification() != null) {
			file.addChild(new DefaultElement("date", dateTimeFormat.format(ft.getFileModification()), null));
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
	public void acceptFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) throws JaxmppException {
		final String sid = ft.getSid();
		sessions.put(sid, (FileTransfer) ft);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sessions.remove(sid);
			}			
		}, TIMEOUT);
		
		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		jingleModule.acceptSession(ft.getPeer(), sid, "ex", new DefaultElement("description", null, JINGLE_FT_XMLNS), null);
		
		connectionManager.connectTcp(jaxmpp, ft);
	}

	@Override
	public void rejectFile(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) throws JaxmppException {
		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		sessions.remove(ft.getSid());
		jingleModule.terminateSession(ft.getPeer(), ft.getSid(), ft.getPeer());
	}

	@Override
	public void registerListeners(JaxmppCore jaxmpp) {
		jaxmpp.addListener(JingleModule.JingleSessionInitiation, sessionInitiationListener);
		jaxmpp.addListener(JingleModule.JingleSessionAccept, sessionAcceptListener);
		jaxmpp.addListener(JingleModule.JingleSessionTerminate, sessionTerminateListener);
	}

	@Override
	public void unregisterListeners(JaxmppCore jaxmpp) {
		jaxmpp.removeListener(JingleModule.JingleSessionInitiation, sessionInitiationListener);
		jaxmpp.removeListener(JingleModule.JingleSessionAccept, sessionAcceptListener);
		jaxmpp.removeListener(JingleModule.JingleSessionTerminate, sessionTerminateListener);
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
	public ConnectionSession getSession(String sid) {
		return sessions.get(sid);
	}
}
