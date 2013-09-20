/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.modules.disco;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class DiscoItemsModule extends AbstractIQModule {

	public static abstract class DiscoItemsAsyncCallback implements AsyncCallback {

		public abstract void onInfoReceived(String attribute, ArrayList<Item> items) throws XMLException;

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			final Element query = responseStanza.getChildrenNS("query", "http://jabber.org/protocol/disco#items");
			List<Element> ritems = query.getChildren("item");
			ArrayList<Item> items = new ArrayList<DiscoItemsModule.Item>();
			for (Element i : ritems) {
				Item to = new Item();
				if (i.getAttribute("jid") != null)
					to.setJid(JID.jidInstance(i.getAttribute("jid")));
				to.setName(i.getAttribute("name"));
				to.setNode(i.getAttribute("node"));
				items.add(to);
			}
			onInfoReceived(query.getAttribute("node"), items);
		}

	}

	public static class Item {

		private JID jid;

		private String name;

		private String node;

		public JID getJid() {
			return jid;
		}

		public String getName() {
			return name;
		}

		public String getNode() {
			return node;
		}

		public void setJid(JID jid) {
			this.jid = jid;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setNode(String node) {
			this.node = node;
		}
	}

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("query", new String[] { "xmlns" }, new String[] { "http://jabber.org/protocol/disco#items" }));


	private final String[] FEATURES = { "http://jabber.org/protocol/disco#items" };

	public DiscoItemsModule(Context context) {
		super(context);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	public void getItems(JID jid, AsyncCallback callback) throws XMLException, JaxmppException {
		getItems(jid, null, callback);
	}

	public void getItems(JID jid, DiscoItemsAsyncCallback callback) throws XMLException, JaxmppException {
		getItems(jid, (AsyncCallback) callback);
	}

	public void getItems(JID jid, String node, AsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(jid);
		iq.setType(StanzaType.get);
		Element query = new DefaultElement("query", null, "http://jabber.org/protocol/disco#items");
		if (node != null) {
			query.setAttribute("node", node);
		}
		iq.addChild(query);

		writer.write(iq, callback);
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		Element query = element.getChildrenNS("query", "http://jabber.org/protocol/disco#items");

		final String requestedNode = query.getAttribute("node");

		DiscoItemEvent event = new DiscoItemEvent(ItemsRequested, sessionObject);
		event.setRequestStanza(element);
		event.setNode(requestedNode);
		observable.fireEvent(event);

		Element result = XmlTools.makeResult(element);
		Element queryResult = new DefaultElement("query", null, "http://jabber.org/protocol/disco#items");
		queryResult.setAttribute("node", event.getNode());
		result.addChild(queryResult);

		for (Item it : event.items) {
			Element e = new DefaultElement("item");
			if (it.getJid() != null)
				e.setAttribute("jid", it.getJid().toString());
			e.setAttribute("name", it.getName());
			e.setAttribute("node", it.getNode());

			queryResult.addChild(e);
		}

		writer.write(result);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}