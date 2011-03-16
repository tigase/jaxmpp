package tigase.jaxmpp.core.client.xmpp.modules.disco;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
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
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class DiscoItemsModule extends AbstractIQModule {

	public static class DiscoItemEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private final ArrayList<Item> items = new ArrayList<DiscoItemsModule.Item>();

		private String node;

		private IQ requestStanza;

		public DiscoItemEvent(EventType type) {
			super(type);
		}

		public ArrayList<Item> getItems() {
			return items;
		}

		public String getNode() {
			return node;
		}

		public IQ getRequestStanza() {
			return requestStanza;
		}

		public void setNode(String node) {
			this.node = node;
		}

		public void setRequestStanza(IQ requestStanza) {
			this.requestStanza = requestStanza;
		}
	}

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

	public final static EventType ItemsRequested = new EventType();

	private final String[] FEATURES = { "http://jabber.org/protocol/disco#items" };

	private final Observable observable;

	public DiscoItemsModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = new Observable(parentObservable);
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
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
		IQ iq = IQ.create();
		iq.setTo(jid);
		iq.setType(StanzaType.get);
		iq.addChild(new DefaultElement("query", null, "http://jabber.org/protocol/disco#items"));

		sessionObject.registerResponseHandler(iq, callback);
		writer.write(iq);

	}

	public void getItems(JID jid, DiscoItemsAsyncCallback callback) throws XMLException, JaxmppException {
		getItems(jid, (AsyncCallback) callback);
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		DiscoItemEvent event = new DiscoItemEvent(ItemsRequested);
		event.setRequestStanza(element);
		observable.fireEvent(event);

		Element result = XmlTools.makeResult(element);
		Element query = new DefaultElement("query", null, "http://jabber.org/protocol/disco#items");
		query.setAttribute("node", event.getNode());
		result.addChild(query);

		for (Item it : event.items) {
			Element e = new DefaultElement("item");
			if (it.getJid() != null)
				e.setAttribute("jid", it.getJid().toString());
			e.setAttribute("name", it.getName());
			e.setAttribute("node", it.getNode());

			query.addChild(e);
		}

		writer.write(result);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
