package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import java.util.Date;
import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
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
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class PubSubModule extends AbstractStanzaModule<Message> {

	public static class PubSubEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Date delay;

		private String itemId;

		private Message message;

		private String nodeName;

		private Element payload;

		private JID pubSubJID;

		public PubSubEvent(EventType type) {
			super(type);
		}

		public Date getDelay() {
			return delay;
		}

		public String getItemId() {
			return itemId;
		}

		public Message getMessage() {
			return message;
		}

		public String getNodeName() {
			return nodeName;
		}

		public Element getPayload() {
			return payload;
		}

		public JID getPubSubJID() {
			return pubSubJID;
		}

		public void setDelay(Date delayTime) {
			this.delay = delayTime;
		}

		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public void setMessage(Message message) {
			this.message = message;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

		public void setPayload(Element payload) {
			this.payload = payload;
		}

		public void setPubSubJID(JID pubSubJID) {
			this.pubSubJID = pubSubJID;
		}
	}

	public static abstract class SubscriptionAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onSubscribe(IQ response, String node, JID jid, String subID, Subscription subscription);

		@Override
		public final void onSuccess(Stanza responseStanza) throws XMLException {
			Element pubsub = responseStanza.getChildrenNS("pubsub", "http://jabber.org/protocol/pubsub");
			List<Element> subscriptions = pubsub.getChildren("subscription");
			Element subscription = subscriptions == null || subscriptions.isEmpty() ? null : subscriptions.get(0);

			String node = subscription.getAttribute("node");
			String jid = subscription.getAttribute("jid");
			String subid = subscription.getAttribute("subid");
			String sub = subscription.getAttribute("subscription");

			onSubscribe((IQ) responseStanza, node, jid == null ? null : JID.jidInstance(jid), subid, sub == null ? null
					: Subscription.valueOf(sub));
		}
	}

	public static final Criteria CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("event", "http://jabber.org/protocol/pubsub#event"));

	public static final EventType NotificationReceived = new EventType();

	private final DateTimeFormat dtf;

	private final Observable observable;

	public PubSubModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = new Observable(parentObservable);
		dtf = new DateTimeFormat();
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	protected void fireNotificationReceived(Message message, String nodeName, String intemID, Element payload, Date delayTime)
			throws XMLException {
		PubSubEvent event = new PubSubEvent(NotificationReceived);
		event.setMessage(message);
		event.setPubSubJID(message.getFrom());
		event.setNodeName(nodeName);
		event.setItemId(intemID);
		event.setPayload(payload);
		event.setDelay(delayTime);

		observable.fireEvent(event);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Message message) throws XMPPException, XMLException {
		final Element event = message.getChildrenNS("event", "http://jabber.org/protocol/pubsub#event");
		List<Element> tmp = event == null ? null : event.getChildren("items");
		final Element items = tmp == null || tmp.isEmpty() ? null : tmp.get(0);
		final String nodeName = items == null ? null : items.getAttribute("node");

		final Element delay = message.getChildrenNS("delay", "urn:xmpp:delay");
		Date delayTime;
		if (delay != null && delay.getAttribute("stamp") != null) {
			delayTime = dtf.parse(delay.getAttribute("stamp"));
		} else {
			delayTime = null;
		}

		List<Element> itemElements = items == null ? null : items.getChildren("item");
		for (Element item : itemElements) {
			final String itemId = item.getAttribute("id");
			final Element payload = item.getFirstChild();

			fireNotificationReceived(message, nodeName, itemId, payload, delayTime);
		}

	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	protected void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, AsyncCallback callback)
			throws XMLException, JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, "http://jabber.org/protocol/pubsub");
		iq.addChild(pubsub);

		final Element subscribe = new DefaultElement("subscribe");
		subscribe.setAttribute("node", nodeName);
		subscribe.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(subscribe);

		sessionObject.registerResponseHandler(iq, callback);
		writer.write(iq);
	}

	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, SubscriptionAsyncCallback callback)
			throws XMLException, JaxmppException {
		subscribe(pubSubJID, nodeName, subscriberJID, (AsyncCallback) callback);
	}

	protected void unsubscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, AsyncCallback callback)
			throws XMLException, JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, "http://jabber.org/protocol/pubsub");
		iq.addChild(pubsub);

		final Element unsubscribe = new DefaultElement("unsubscribe");
		unsubscribe.setAttribute("node", nodeName);
		unsubscribe.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(unsubscribe);

		sessionObject.registerResponseHandler(iq, callback);
		writer.write(iq);
	}

	public void unsubscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, PubSubAsyncCallback callback)
			throws XMLException, JaxmppException {
		unsubscribe(pubSubJID, nodeName, subscriberJID, (AsyncCallback) callback);
	}

}
