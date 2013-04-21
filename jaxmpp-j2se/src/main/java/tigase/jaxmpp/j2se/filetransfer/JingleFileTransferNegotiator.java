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
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesCache;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
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
import tigase.jaxmpp.j2se.connection.ConnectionManager.InitializedCallback;
import tigase.jaxmpp.j2se.connection.socks5bytestream.JingleSocks5BytestreamsConnectionManager;
import tigase.jaxmpp.j2se.connection.socks5bytestream.Socks5BytestreamsConnectionManager;

/**
 *
 * @author andrzej
 */
public class JingleFileTransferNegotiator extends FileTransferNegotiatorAbstract {

	private static final Logger log = Logger.getLogger(JingleFileTransferNegotiator.class.getCanonicalName());
	
	public static final String JINGLE_FT_XMLNS = "urn:xmpp:jingle:apps:file-transfer:3";

	private static final String TRANSPORTS_KEY = "transports-key";
	
	private static DateTimeFormat dateTimeFormat = new DateTimeFormat();
	
    private final JingleSocks5BytestreamsConnectionManager connectionManager = new JingleSocks5BytestreamsConnectionManager();
	
	private final Listener<JingleSessionAcceptEvent> sessionAcceptListener = new Listener<JingleSessionAcceptEvent>() {

		@Override
		public void handleEvent(JingleSessionAcceptEvent be) throws JaxmppException {
			log.log(Level.FINER, "jingle session accepted");
		}
		
	};
	
//	private final Listener<JingleSessionInfoEvent> sessionInfoListener = new Listener<JingleSessionInfoEvent>() {
//
//		@Override
//		public void handleEvent(JingleSessionInfoEvent be) throws JaxmppException {
//			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//		}
//		
//	};

	private final Listener<JingleSessionInitiationEvent> sessionInitiationListener = new Listener<JingleSessionInitiationEvent>() {

		@Override
		public void handleEvent(JingleSessionInitiationEvent be) throws JaxmppException {
			Element desc = be.getDescription();
			
			if (!JINGLE_FT_XMLNS.equals(desc.getXMLNS())) 
				return;
			
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
			if (ft.getTransferredBytes() > 0) {
				log.log(Level.FINE, "transfer finished");
			}
			else {
				fireOnFailure(ft, null);
			}
		}
		
	};
	
	private final Listener<JingleTransportInfoEvent> transportInfoListener = new Listener<JingleTransportInfoEvent>() {

		@Override
		public void handleEvent(JingleTransportInfoEvent be) throws JaxmppException {
			JID sender = be.getSender();
			String sid = be.getSid();
			final FileTransfer ft = sessions.get(sid);
			Element content = be.getContent();
			if (content != null) {
				List<Element> transports = content.getChildren("transport");
				if (transports == null || transports.isEmpty()) {
					return;
				}

				Transport transport = new Transport(transports.get(0));

				List<Element> candidatesUsed = transport.getChildren("candidate-used");
				if (candidatesUsed == null || candidatesUsed.isEmpty()) {
					return;
				}

				Element candidateUsed = candidatesUsed.get(0);
				String cid = candidateUsed.getAttribute("cid");

				Candidate candidate = getCandidate(ft, transport.getSid(), cid);
				if (candidate == null) {
					return;
				}

				fireOnSuccess(ft);
			}
		}
		
	};

	private final Timer timer = new Timer();
	
	private static final long TIMEOUT = 5 * 60 * 1000;
	private Map<String, FileTransfer> sessions = Collections.synchronizedMap(new HashMap<String,FileTransfer>());
	
	@Override
	public boolean isSupported(JaxmppCore jaxmpp, tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer ft) {
		Presence p = ft.getSessionObject().getPresence().getPresence(ft.getPeer());
		CapabilitiesModule capsModule = jaxmpp.getModule(CapabilitiesModule.class);
		CapabilitiesCache capsCache = capsModule.getCache();

		try {
			String capsNode = FileTransferManager.getCapsNode(p);
			Set<String> features = (capsCache != null) ? capsCache.getFeatures(capsNode) : null;

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
		final String sid = UUID.randomUUID().toString();

		// creation of session description
		Element description = new DefaultElement("description");
		description.setXMLNS(JINGLE_FT_XMLNS);

		Element offer = new DefaultElement("offer");
		description.addChild(offer);

		Element file = new DefaultElement("file");
		file.addChild(new DefaultElement("name", file.getName(), null));
		file.addChild(new DefaultElement("size", String.valueOf(ft.getFileSize()), null));
		if (ft.getFileModification() != null) {
			file.addChild(new DefaultElement("date", dateTimeFormat.format(ft.getFileModification()), null));
		}
		offer.addChild(file);

		List<Transport> transports = getTransports((FileTransfer) ft);		
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
		jaxmpp.addListener(JingleModule.JingleTransportInfo, transportInfoListener);
	}

	@Override
	public void unregisterListeners(JaxmppCore jaxmpp) {
		jaxmpp.removeListener(JingleModule.JingleSessionInitiation, sessionInitiationListener);
		jaxmpp.removeListener(JingleModule.JingleSessionAccept, sessionAcceptListener);
		jaxmpp.removeListener(JingleModule.JingleSessionTerminate, sessionTerminateListener);
		jaxmpp.removeListener(JingleModule.JingleTransportInfo, transportInfoListener);
	}
	
	protected List<Transport> getTransports(FileTransfer ft) throws XMLException, JaxmppException {
		List<Transport> transports = ft.getData(TRANSPORTS_KEY);
		if (transports == null) {
			transports = new ArrayList<Transport>();

			Transport transport = this.connectionManager.getTransport(ft);
			if (transport != null) {
				transports.add(transport);
			}
			
			ft.setData(TRANSPORTS_KEY, transports);
		}
		
		return transports;
	}
	
	public static Candidate getCandidate(FileTransfer ft, String sid, String cid) throws JaxmppException {
		List<Transport> transports = ft.getData(TRANSPORTS_KEY);
		if (transports != null) {
			for (Transport trans : transports) {
				if (!sid.equals(trans.getSid()))
					continue;
				
				List<Candidate> candidates = trans.getCandidates();
				if (candidates != null) {
					for (Candidate candidate : candidates) {
						if (cid.equals(candidate.getCid())) {
							return candidate;
						}
					}
				}
			}
		}
		return null;
	}
}
