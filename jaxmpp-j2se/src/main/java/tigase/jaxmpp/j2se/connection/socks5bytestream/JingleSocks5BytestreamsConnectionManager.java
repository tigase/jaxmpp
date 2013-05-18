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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Candidate;
import tigase .jaxmpp.core.client.xmpp.modules.jingle.JingleModule;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleTransportInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Transport;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Streamhost;
import tigase.jaxmpp.j2se.connection.ConnectionEvent;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.j2se.connection.ConnectionSessionHandler;
import static tigase.jaxmpp.j2se.connection.socks5bytestream.Socks5ConnectionManager.JAXMPP_KEY;

/**
 *
 * @author andrzej
 */
public class JingleSocks5BytestreamsConnectionManager extends Socks5ConnectionManager {

	private static final Logger log = Logger.getLogger("JingleSocks5BytestreamsConnectionManager");
	
	public static final String XMLNS = "urn:xmpp:jingle:transports:s5b:1";
	
	public static final String TRANSPORTS_KEY = "transports-key";
	
	public static final String TRANSPORT_USED_KEY = "transport-used";
	public static final String CANDIDATE_USED_KEY = "candidate-used";

	public static final String SOCKS5_TRANSPORT_KEY = "socks5-transport-key-" + XMLNS;

	private final ConnectionSessionHandler connectionSessionHandler;
	
	private final Listener<ConnectionEvent> connectionEstablishedListener = new Listener<ConnectionEvent>() {

		@Override
		public void handleEvent(ConnectionEvent be) throws JaxmppException {
			ConnectionSession session = be.getConnectionSession();
			if (session.isIncoming()) {
				// sending candidate-used
				sendCandidateUsed(be.getConnectionSession());
			}						
		}
		
	};
	
	public JingleSocks5BytestreamsConnectionManager(ConnectionSessionHandler handler) {
		connectionSessionHandler = handler;
	}
	
	@Override
	public void setObservable(Observable observable) {
		super.setObservable(observable);
		observable.addListener(CONNECTION_ESTABLISHED, connectionEstablishedListener);		
	}
	
	@Override
	public void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback) throws JaxmppException {
		session.setData(JAXMPP_KEY, jaxmpp);
		
		Listener<JingleTransportInfoEvent> listener = jaxmpp.getSessionObject().getUserProperty(XMLNS + "#JingleTransportInfo");
		if (listener == null) {
			listener = new Listener<JingleTransportInfoEvent>() {
				@Override
				public void handleEvent(JingleTransportInfoEvent be) throws JaxmppException {
					Element content = be.getContent();
					if (content == null) {
						return;
					}
					Element transport = content.getChildrenNS("transport", XMLNS);
					if (transport == null) {
						return;
					}

					be.setJingleHandled(true);
					String transportSid = transport.getAttribute("sid");
					List<Element> candidatesUsed = transport.getChildren("candidate-used");
					if (candidatesUsed != null && !candidatesUsed.isEmpty()) {
						Element candidateUsed = candidatesUsed.get(0);

						candidateUsedReceived(be.getSender(), be.getSid(), transportSid, candidateUsed.getAttribute("cid"));
					}
				}
			};
			jaxmpp.addListener(JingleModule.JingleTransportInfo, listener);			
			jaxmpp.getSessionObject().setUserProperty(XMLNS + "#JingleTransportInfo", listener);
		}
		
		if (callback != null) {
			callback.initialized(jaxmpp, session);		
		}
	}

	@Override
	public void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
        session.setData(JAXMPP_KEY, jaxmpp);		
		List<Transport> transports = session.getData(TRANSPORTS_KEY);
		if (transports != null) {
			boolean established = false;
			for (Transport transport : transports) {
				if (!XMLNS.equals(transport.getXMLNS()))
					continue;
				
				List<Candidate> candidates = transport.getCandidates();
				for (Candidate candidate : candidates) {
					try {
						session.setData(TRANSPORT_USED_KEY, transport);
						session.setData(CANDIDATE_USED_KEY, candidate);
						connectToProxy(jaxmpp, session, transport.getSid(), candidate);
						established = true;
						break;
					} catch (IOException ex) {
						session.removeData(TRANSPORT_USED_KEY);
						session.removeData(CANDIDATE_USED_KEY);
						log.log(Level.FINER, "exception connection to candidate, trying next one", ex);
					}
				}
				if (established) {
					break;
				}
			}
		}
	}

	@Override
	public void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Transport getTransport(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
		session.setData(JAXMPP_KEY, jaxmpp);

		String sid = UUID.randomUUID().toString();
		Transport transport = new Transport(XMLNS, sid, Transport.Mode.tcp);

		List<Streamhost> streamhosts = getLocalStreamHosts(session, sid);
		if (streamhosts == null) 
			return null;
		
		int priority = streamhosts.size();
		for (Streamhost host : streamhosts) {
			String cid = UUID.randomUUID().toString();
			JID jid = host.getJid();
			transport.addCandidate(new Candidate(cid, host.getHost(), host.getPort(), jid, priority, 
					session.getSessionObject().getBindedJid().equals(jid) ? Candidate.Type.direct : Candidate.Type.proxy));
		}
		
		session.setData(SOCKS5_TRANSPORT_KEY, transport);
		
		return transport;
	}
	
	private void sendCandidateUsed(ConnectionSession session) throws JaxmppException {
		JaxmppCore jaxmpp = (JaxmppCore) session.getData(JAXMPP_KEY);
		Transport transport = (Transport) session.getData(TRANSPORT_USED_KEY);
		Candidate candidateUsed = (Candidate) session.getData(CANDIDATE_USED_KEY);
		
		JingleModule jingleModule = jaxmpp.getModule(JingleModule.class);
		
		Element candidateUsedEl = new DefaultElement("candidate-used");
		candidateUsedEl.setAttribute("cid", candidateUsed.getCid());
		
		Element transportEl = new DefaultElement("transport");
		transportEl.setAttribute("sid", transport.getSid());
		transportEl.setXMLNS(XMLNS);
		transportEl.addChild(candidateUsedEl);
		
		Element contentEl = new DefaultElement("content");
		contentEl.setAttribute("initiator", "creator");
		contentEl.setAttribute("name", "ex");
		contentEl.addChild(transportEl);
		
		jingleModule.transportInfo(session.getPeer(), jaxmpp.getSessionObject().getBindedJid(), session.getSid(), contentEl);
	}
	
	private void candidateUsedReceived(JID sender, String sid, String transportSid, String cid) {
		ConnectionSession session = connectionSessionHandler.getSession(sid);
		if (session == null)
			return;
		
		try {
			Transport transport = session.getData(SOCKS5_TRANSPORT_KEY);
			if (!transport.getSid().equals(transportSid)) {
				return;
			}

			Candidate candidate = null;
			for (Candidate c : transport.getCandidates()) {
				if (c.getCid().equals(cid)) {
					candidate = c;
					break;
				}
			}

			if (candidate.getType() == Candidate.Type.proxy) {
				// need to activate proxy
				JaxmppCore jaxmpp = (JaxmppCore) session.getData(JAXMPP_KEY);
				connectToProxy(jaxmpp, session, transport.getSid(), candidate);
			}
			else {
				Socket socket = session.getData("socket");
				fireOnConnected(session, socket);
			}
		} catch (Exception ex) {
			this.fireOnFailure(session);
		}
	}
	
}
