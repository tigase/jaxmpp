/*
 * Socks5BytestreamsModule.java
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

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule.StreamhostsHandler.StreamhostsEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andrzej
 */
public class Socks5BytestreamsModule
		implements XmppModule {

	public static final String XMLNS_BS = "http://jabber.org/protocol/bytestreams";
	private static final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("query", XMLNS_BS));
	private static final String[] FEATURES = new String[]{XMLNS_BS};
	private final Context context;

	public Socks5BytestreamsModule(Context context) {
		this.context = context;
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
	public void process(Element element) throws JaxmppException {
		final IQ iq = element instanceof Stanza ? (IQ) element : (IQ) Stanza.create(element);
		process(iq);
	}

	public void process(IQ iq) throws JaxmppException {
		Element query = iq.getChildrenNS("query", XMLNS_BS);
		if (query != null) {
			List<Streamhost> hosts = processStreamhosts(iq);
			if (hosts != null) {
				StreamhostsEvent event = new StreamhostsEvent(this.context.getSessionObject());
				event.setId(iq.getId());
				event.setSid(iq.getChildrenNS("query", XMLNS_BS).getAttribute("sid"));
				event.setFrom(iq.getFrom());
				event.setHosts(hosts);

				context.getEventBus().fire(event);
				return;
			}
		}
	}

	List<Streamhost> processStreamhosts(Stanza iq) throws XMLException {
		Element query = iq.getChildrenNS("query", XMLNS_BS);
		List<Element> el_hosts = query.getChildren("streamhost");

		if (el_hosts == null) {
			return null;
		}

		List<Streamhost> hosts = new ArrayList<Streamhost>();

		if (el_hosts != null) {
			StreamhostsEvent event = new StreamhostsEvent(this.context.getSessionObject());
			for (Element el_host : el_hosts) {
				String jid = el_host.getAttribute("jid");
				hosts.add(new Streamhost(jid, el_host.getAttribute("host"),
										 Integer.parseInt(el_host.getAttribute("port"))));
			}
		}

		return hosts;
	}

	public void requestActivate(JID host, String sid, JID jid, ActivateCallback callback) throws JaxmppException {
		if (host == null) {
			host = jid;
		}

		IQ iq = IQ.create();
		iq.setTo(host);
		iq.setType(StanzaType.set);

		Element query = ElementFactory.create("query", null, XMLNS_BS);
		query.setAttribute("sid", sid);
		iq.addChild(query);

		Element activate = ElementFactory.create("activate", jid.toString(), null);
		query.addChild(activate);

		context.getWriter().write(iq, callback);
	}

	public void requestStreamhosts(JID host, StreamhostsCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(host);
		iq.setType(StanzaType.get);

		Element query = ElementFactory.create("query", null, XMLNS_BS);
		iq.addChild(query);

		context.getWriter().write(iq, callback);
	}

	public void sendStreamhostUsed(JID to, String id, String sid, Streamhost streamhost) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(to);
		iq.setId(id);
		iq.setType(StanzaType.result);

		Element query = ElementFactory.create("query", null, XMLNS_BS);
		query.setAttribute("sid", sid);
		iq.addChild(query);

		Element streamhostUsed = ElementFactory.create("streamhost-used");
		streamhostUsed.setAttribute("jid", streamhost.getJid().toString());
		query.addChild(streamhostUsed);

		// session.registerResponseHandler(iq, callback);
		context.getWriter().write(iq);
	}

	public void sendStreamhosts(JID recipient, String sid, List<Streamhost> hosts, AsyncCallback callback)
			throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(recipient);
		iq.setType(StanzaType.set);

		Element query = ElementFactory.create("query", null, XMLNS_BS);
		iq.addChild(query);
		query.setAttribute("sid", sid);

		for (Streamhost host : hosts) {
			Element streamhost = ElementFactory.create("streamhost");
			streamhost.setAttribute("jid", host.getJid().toString());
			streamhost.setAttribute("host", host.getHost());
			streamhost.setAttribute("port", String.valueOf(host.getPort()));
			query.addChild(streamhost);
		}

		context.getWriter().write(iq, (long) (3 * 60 * 1000), callback);
	}

	public interface StreamhostsHandler
			extends EventHandler {

		void onStreamhostsHandler(SessionObject sessionObject, JID from, String id, String sid, List<Streamhost> hosts)
				throws JaxmppException;

		class StreamhostsEvent
				extends JaxmppEvent<StreamhostsHandler> {

			private JID from;

			private List<Streamhost> hosts;

			private String id;

			private String sid;

			public StreamhostsEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(StreamhostsHandler handler) throws JaxmppException {
				handler.onStreamhostsHandler(sessionObject, from, id, sid, hosts);
			}

			public JID getFrom() {
				return from;
			}

			public void setFrom(JID from) {
				this.from = from;
			}

			public List<Streamhost> getHosts() {
				return hosts;
			}

			public void setHosts(List<Streamhost> hosts) {
				this.hosts = hosts;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getSid() {
				return sid;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

		}
	}

	public static abstract class ActivateCallback
			implements AsyncCallback {

	}

}
