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
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Candidate;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.Transport;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Streamhost;

/**
 *
 * @author andrzej
 */
public class JingleSocks5BytestreamsConnectionManager extends Socks5ConnectionManager {

	private static final Logger log = Logger.getLogger("JingleSocks5BytestreamsConnectionManager");
	
	public static final String XMLNS = "urn:xmpp:jingle:transports:s5b:1";
	
	public static final String TRANSPORTS_KEY = "transports-key";
	
	@Override
	public void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback) throws JaxmppException {
		callback.initialized(jaxmpp, session);
	}

	@Override
	public void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
		List<Transport> transports = session.getData(TRANSPORTS_KEY);
		if (transports != null) {
			for (Transport transport : transports) {
				if (!XMLNS.equals(transport.getXMLNS()))
					continue;
				
				List<Candidate> candidates = transport.getCandidates();
				for (Candidate candidate : candidates) {
					try {
						connectToProxy(jaxmpp, session, candidate);
						break;
					} catch (IOException ex) {
						log.log(Level.FINER, "exception connection to candidate, trying next one", ex);
					}
				}
			}
		}
	}

	@Override
	public void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Transport getTransport(ConnectionSession session) throws JaxmppException {
		Transport transport = new Transport(XMLNS, session.getSid(), Transport.Mode.tcp);

		List<Streamhost> streamhosts = getLocalStreamHosts(session);
		if (streamhosts == null) 
			return null;
		
		int priority = streamhosts.size();
		for (Streamhost host : streamhosts) {
			String cid = UUID.randomUUID().toString();
			JID jid = host.getJid();
			transport.addCandidate(new Candidate(cid, host.getHost(), host.getPort(), jid, priority, 
					session.getSessionObject().getBindedJid().equals(jid) ? Candidate.Type.direct : Candidate.Type.proxy));
		}
		return transport;
	}
	
}
