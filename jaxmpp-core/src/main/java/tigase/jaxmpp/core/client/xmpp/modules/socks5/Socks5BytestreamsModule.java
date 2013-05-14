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

import java.util.ArrayList;
import java.util.List;
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;
import tigase.jaxmpp.core.client.xmpp.modules.PacketWriterAware;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 *
 * @author andrzej
 */
public class Socks5BytestreamsModule implements XmppModule, PacketWriterAware, ObservableAware {
	
	public static abstract class ActivateCallback implements AsyncCallback {

	}

	public static final String XMLNS_BS = "http://jabber.org/protocol/bytestreams";

	private static final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("query", XMLNS_BS));

	private static final String[] FEATURES = new String[] { XMLNS_BS };
        
        public static final EventType StreamhostsEventType = new EventType();

	private Observable observable;

	private final SessionObject session;

	private PacketWriter writer;

	public Socks5BytestreamsModule(SessionObject sessionObject) {
		session = sessionObject;
	}        

	public void addListener(EventType eventType, Listener listener) {
		observable.addListener(eventType, listener);
	}
        
	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		final IQ iq = element instanceof Stanza ? (IQ) element : (IQ) Stanza.create(element);
		process(iq);
	}
        
	public void process(IQ iq) throws XMLException, JaxmppException {
		Element query = iq.getChildrenNS("query", XMLNS_BS);
		if (query != null) {
			List<Streamhost> hosts = processStreamhosts(iq);
			if (hosts != null) {
				StreamhostsEvent event = new StreamhostsEvent(StreamhostsEventType, this.session);
				event.setId(iq.getId());
				event.setSid(iq.getChildrenNS("query", XMLNS_BS).getAttribute("sid"));
				event.setFrom(iq.getFrom());
				event.setHosts(hosts);

				observable.fireEvent(event);
				return;
			}
		}
	}

	List<Streamhost> processStreamhosts(Stanza iq) throws XMLException {
		Element query = iq.getChildrenNS("query", XMLNS_BS);
		List<Element> el_hosts = query.getChildren("streamhost");

		if (el_hosts == null)
			return null;

		List<Streamhost> hosts = new ArrayList<Streamhost>();

		if (el_hosts != null) {
			StreamhostsEvent event = new StreamhostsEvent(StreamhostsEventType, this.session);
			for (Element el_host : el_hosts) {
				String jid = el_host.getAttribute("jid");
				hosts.add(new Streamhost(jid, el_host.getAttribute("host"), Integer.parseInt(el_host.getAttribute("port"))));
			}
		}

		return hosts;
	}

        public void removeListener(Listener listener) {
		observable.removeListener(listener);
	}
        
	public void requestActivate(JID host, String sid, JID jid, ActivateCallback callback) throws XMLException,
			JaxmppException {
		if (host == null) host = jid;
		
		IQ iq = IQ.create();
		iq.setTo(host);
		iq.setType(StanzaType.set);

		Element query = new DefaultElement("query", null, XMLNS_BS);
		query.setAttribute("sid", sid);
		iq.addChild(query);

		Element activate = new DefaultElement("activate", jid.toString(), null);
		query.addChild(activate);

		writer.write(iq, callback);
	}

	public void requestStreamhosts(JID host, StreamhostsCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(host);
		iq.setType(StanzaType.get);

		Element query = new DefaultElement("query", null, XMLNS_BS);
		iq.addChild(query);

		writer.write(iq, callback);
	}
        
	public void sendStreamhosts(JID recipient, String sid, List<Streamhost> hosts, AsyncCallback callback) throws XMLException,
			JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(recipient);
		iq.setType(StanzaType.set);

		Element query = new DefaultElement("query", null, XMLNS_BS);
		iq.addChild(query);
		query.setAttribute("sid", sid);

		for (Streamhost host : hosts) {
			Element streamhost = new DefaultElement("streamhost");
			streamhost.setAttribute("jid", host.getJid().toString());
			streamhost.setAttribute("host", host.getHost());
			streamhost.setAttribute("port", String.valueOf(host.getPort()));
			query.addChild(streamhost);
		}

		writer.write(iq, (long) (3 * 60 * 1000), callback);
	}

	public void sendStreamhostUsed(JID to, String id, String sid, Streamhost streamhost) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(to);
		iq.setId(id);
		iq.setType(StanzaType.result);

		Element query = new DefaultElement("query", null, XMLNS_BS);
		query.setAttribute("sid", sid);
		iq.addChild(query);

		Element streamhostUsed = new DefaultElement("streamhost-used");
		streamhostUsed.setAttribute("jid", streamhost.getJid().toString());
		query.addChild(streamhostUsed);

		// session.registerResponseHandler(iq, callback);
		writer.write(iq);
	}
        
        @Override
        public void setPacketWriter(PacketWriter packetWriter) {
                writer = packetWriter;
        }

        @Override
        public void setObservable(Observable parentObservable) {
                observable = ObservableFactory.instance(parentObservable);
        }
        
}
