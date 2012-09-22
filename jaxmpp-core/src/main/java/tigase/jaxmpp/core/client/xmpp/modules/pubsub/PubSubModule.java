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
package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.forms.XDataType;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

/**
 * Publish-Subscribe Module.
 * <p>
 * This module implementation of <a
 * href='http://xmpp.org/extensions/xep-0060.html'>XEP-0060:
 * Publish-Subscribe</a>.
 * </p>
 * 
 * @author bmalkow
 * 
 */
public class PubSubModule extends AbstractStanzaModule<Message> {

	public abstract static class PublishAsyncCallback extends PubSubAsyncCallback {

		public abstract void onPublish(String itemId);

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
			List<Element> publishs = pubsub.getChildren("publish");
			Element publish = publishs == null || publishs.isEmpty() ? null : publishs.get(0);
			if (publish == null)
				return;

			List<Element> items = publish.getChildren("item");
			for (Element element : items) {
				onPublish(element.getAttribute("id"));
			}

		}

	}

	public static class PubSubEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Date delay;

		private String itemId;

		private String itemType;

		private Message message;

		private String nodeName;

		private Element payload;

		private JID pubSubJID;

		public PubSubEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Date getDelay() {
			return delay;
		}

		public String getItemId() {
			return itemId;
		}

		public String getItemType() {
			return itemType;
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

		public void setItemType(String itemType) {
			this.itemType = itemType;
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

	public static abstract class RetrieveItemsAsyncCallback extends PubSubAsyncCallback {

		public static class Item {

			private final String id;

			private final Element payload;

			Item(String id, Element payload) {
				super();
				this.id = id;
				this.payload = payload;
			}

			public String getId() {
				return id;
			}

			public Element getPayload() {
				return payload;
			}

		}

		protected abstract void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items);

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			final Element event = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
			List<Element> tmp = event == null ? null : event.getChildren("items");
			final Element items = tmp == null || tmp.isEmpty() ? null : tmp.get(0);
			final String nodeName = items == null ? null : items.getAttribute("node");

			ArrayList<Item> result = new ArrayList<Item>();

			List<Element> itemElements = items == null ? null : items.getChildren("item");
			if (itemElements != null)
				for (Element item : itemElements) {
					final String itemId = item.getAttribute("id");
					final Element payload = item.getFirstChild();

					Item it = new Item(itemId, payload);
					result.add(it);
				}

			onRetrieve((IQ) responseStanza, nodeName, result);
		}

	}

	public static abstract class SubscriptionAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onSubscribe(IQ response, String node, JID jid, String subID, Subscription subscription);

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
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

	public static abstract class SubscriptionOptionsAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onReceiveConfiguration(IQ responseStanza, String node, JID jid, JabberDataElement form);

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			try {
				final Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
				List<Element> tmp = pubsub.getChildren("options");
				if (tmp == null || tmp.isEmpty()) {
					tmp = pubsub.getChildren("default");
				}
				final Element options = tmp == null || tmp.isEmpty() ? null : tmp.get(0);

				String node = options.getAttribute("node");
				String jid = options.getAttribute("jid");

				final Element x = options.getChildrenNS("x", "jabber:x:data");

				final JabberDataElement form = new JabberDataElement(DefaultElement.create(x));

				onReceiveConfiguration((IQ) responseStanza, node, jid == null ? null : JID.jidInstance(jid), form);
			} catch (Exception e) {
				log.log(Level.WARNING, "Processing subscription configuration error", e);
			}
		}

	}

	public static final Criteria CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("event", "http://jabber.org/protocol/pubsub#event"));

	public static final EventType NotificationReceived = new EventType();

	private static final String PUBSUB_EVENT_XMLNS = "http://jabber.org/protocol/pubsub#event";

	private static final String PUBSUB_XMLNS = "http://jabber.org/protocol/pubsub";

	private static final String QUEUEING_XMLNS = "urn:xmpp:pubsub:queueing:0";

	private final DateTimeFormat dtf;

	private final Observable observable;

	public PubSubModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = ObservableFactory.instance(parentObservable);
		dtf = new DateTimeFormat();
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	public void configureSubscription(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement form,
			AsyncCallback callback) throws JaxmppException, XMLException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element options = new DefaultElement("options");
		options.setAttribute("node", nodeName);
		options.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(options);

		options.addChild(form.createSubmitableElement(XDataType.submit));

		writer.write(iq, callback);

	}

	public void configureSubscription(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement form,
			PubSubAsyncCallback callback) throws JaxmppException, XMLException {
		configureSubscription(pubSubJID, nodeName, subscriberJID, form, (AsyncCallback) callback);
	}

	public void deleteItem(BareJID pubSubJID, String nodeName, String itemId, AsyncCallback callback) throws XMLException,
			JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element retract = new DefaultElement("retract");
		retract.setAttribute("node", nodeName);
		pubsub.addChild(retract);

		Element item = new DefaultElement("item");
		item.setAttribute("id", itemId);
		retract.addChild(item);

		writer.write(iq, callback);
	}

	public void deleteItem(BareJID pubSubJID, String nodeName, String itemId, PubSubAsyncCallback callback)
			throws XMLException, JaxmppException {
		deleteItem(pubSubJID, nodeName, itemId, (AsyncCallback) callback);
	}

	protected void fireNotificationReceived(Message message, String nodeName, String itemType, String itemId, Element payload,
			Date delayTime) throws JaxmppException {
		PubSubEvent event = new PubSubEvent(NotificationReceived, sessionObject);
		event.setMessage(message);
		event.setPubSubJID(message.getFrom());
		event.setNodeName(nodeName);
		event.setItemId(itemId);
		event.setPayload(payload);
		event.setDelay(delayTime);
		event.setItemType(itemType);

		observable.fireEvent(event);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	public void getDefaultSubscriptionConfiguration(BareJID pubSubJID, String nodeName, AsyncCallback callback)
			throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element def = new DefaultElement("default");
		def.setAttribute("node", nodeName);
		pubsub.addChild(def);

		writer.write(iq, callback);
	}

	public void getDefaultSubscriptionConfiguration(BareJID pubSubJID, String nodeName,
			SubscriptionOptionsAsyncCallback callback) throws XMLException, JaxmppException {
		getDefaultSubscriptionConfiguration(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public void getSubscriptionConfiguration(BareJID pubSubJID, String nodeName, JID subscriberJID, AsyncCallback callback)
			throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element options = new DefaultElement("options");
		options.setAttribute("node", nodeName);
		options.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(options);

		writer.write(iq, callback);
	}

	public void getSubscriptionConfiguration(BareJID pubSubJID, String nodeName, JID subscriberJID,
			SubscriptionOptionsAsyncCallback callback) throws XMLException, JaxmppException {
		getSubscriptionConfiguration(pubSubJID, nodeName, subscriberJID, (AsyncCallback) callback);
	}

	@Override
	public void process(Message message) throws JaxmppException {
		final Element event = message.getChildrenNS("event", PUBSUB_EVENT_XMLNS);
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

		List<Element> itemElements = items == null ? null : items.getChildren();
		for (Element item : itemElements) {
			final String type = item.getName();
			final String itemId = item.getAttribute("id");
			final Element payload = item.getFirstChild();

			fireNotificationReceived(message, nodeName, type, itemId, payload, delayTime);
		}

	}

	public void publishItem(BareJID pubSubJID, String nodeName, String itemId, Element payload, AsyncCallback callback)
			throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element publish = new DefaultElement("publish");
		publish.setAttribute("node", nodeName);
		pubsub.addChild(publish);

		final Element item = new DefaultElement("item");
		item.setAttribute("id", itemId);
		publish.addChild(item);

		item.addChild(payload);

		writer.write(iq, callback);
	}

	public void publishItem(BareJID pubSubJID, String nodeName, String itemId, Element payload, PublishAsyncCallback callback)
			throws XMLException, JaxmppException {
		publishItem(pubSubJID, nodeName, itemId, payload, (AsyncCallback) callback);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void retrieveItem(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws XMLException, JaxmppException {
		retrieveItem(pubSubJID, nodeName, null, null, callback);
	}

	public void retrieveItem(BareJID pubSubJID, String nodeName, RetrieveItemsAsyncCallback callback) throws XMLException,
			JaxmppException {
		retrieveItem(pubSubJID, nodeName, null, null, callback);
	}

	public void retrieveItem(BareJID pubSubJID, String nodeName, String itemId, AsyncCallback callback) throws XMLException,
			JaxmppException {
		retrieveItem(pubSubJID, nodeName, itemId, null, callback);
	}

	public void retrieveItem(BareJID pubSubJID, String nodeName, String itemId, Integer maxItems, AsyncCallback callback)
			throws XMLException, JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.get);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element items = new DefaultElement("items");
		items.setAttribute("node", nodeName);
		if (maxItems != null) {
			items.setAttribute("max_items", maxItems.toString());
		}
		pubsub.addChild(items);

		if (itemId != null) {
			Element item = new DefaultElement("item");
			item.setAttribute("id", itemId);
			items.addChild(item);
		}

		writer.write(iq, callback);
	}

	public void retrieveItem(BareJID pubSubJID, String nodeName, String itemId, RetrieveItemsAsyncCallback callback)
			throws XMLException, JaxmppException {
		retrieveItem(pubSubJID, nodeName, itemId, null, callback);
	}

	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement options,
			AsyncCallback callback) throws XMLException, JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element subscribe = new DefaultElement("subscribe");
		subscribe.setAttribute("node", nodeName);
		subscribe.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(subscribe);

		if (options != null) {
			Element optionsElement = new DefaultElement("options");
			optionsElement.setAttribute("jid", subscriberJID.toString());
			optionsElement.setAttribute("node", nodeName);
			optionsElement.addChild(options);
			pubsub.addChild(optionsElement);
		}

		writer.write(iq, callback);
	}

	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement options,
			SubscriptionAsyncCallback callback) throws XMLException, JaxmppException {
		subscribe(pubSubJID, nodeName, subscriberJID, options, (AsyncCallback) callback);
	}

	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, SubscriptionAsyncCallback callback)
			throws XMLException, JaxmppException {
		subscribe(pubSubJID, nodeName, subscriberJID, null, (AsyncCallback) callback);
	}

	public void unlockItem(BareJID pubSubJID, String nodeName, String itemId, AsyncCallback callback) throws XMLException,
			JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element unlock = new DefaultElement("unlock", null, QUEUEING_XMLNS);
		unlock.setAttribute("node", nodeName);
		pubsub.addChild(unlock);

		Element item = new DefaultElement("item");
		item.setAttribute("id", itemId);
		unlock.addChild(item);

		writer.write(iq, callback);
	}

	public void unlockItem(BareJID pubSubJID, String nodeName, String itemId, PubSubAsyncCallback callback)
			throws XMLException, JaxmppException {
		unlockItem(pubSubJID, nodeName, itemId, (AsyncCallback) callback);
	}

	public void unsubscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, AsyncCallback callback) throws XMLException,
			JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element unsubscribe = new DefaultElement("unsubscribe");
		unsubscribe.setAttribute("node", nodeName);
		unsubscribe.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(unsubscribe);

		writer.write(iq, callback);
	}

	public void unsubscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, PubSubAsyncCallback callback)
			throws XMLException, JaxmppException {
		unsubscribe(pubSubJID, nodeName, subscriberJID, (AsyncCallback) callback);
	}

}