/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2013 "Bartosz Ma��kowski" <bartosz.malkowski@tigase.org>
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
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
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

	public static class AffiliationElement extends ElementWrapper {

		public AffiliationElement() {
			this(new DefaultElement("affiliation"));
		}

		public AffiliationElement(Element affiliation) {
			super(affiliation);
		}

		public Affiliation getAffiliation() throws XMLException {
			String s = getAttribute("affiliation");
			return s == null ? null : Affiliation.valueOf(s);
		}

		public JID getJID() throws XMLException {
			String j = getAttribute("jid");
			return j == null ? null : JID.jidInstance(j);
		}

		public String getNode() throws XMLException {
			return getAttribute("node");
		}

		public void setAffiliation(Affiliation affiliation) throws XMLException {
			setAttribute("affiliation", affiliation == null ? null : affiliation.name());
		}

		public void setJID(JID subscriberJID) throws XMLException {
			setAttribute("jid", subscriberJID == null ? null : subscriberJID.toString());
		}

		public void setNode(String nodeName) throws XMLException {
			setAttribute("node", nodeName);
		}

	}

	public static abstract class AffiliationsRetrieveAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onRetrieve(IQ response, String node, Collection<AffiliationElement> affiliations);

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
			if (pubsub == null) pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_OWNER_XMLNS);
			Element affiliations = getFirstChild(pubsub, "affiliations");
			String node = affiliations.getAttribute("node");

			ArrayList<AffiliationElement> affiliationWrappers = new ArrayList<AffiliationElement>();
			List<Element> afch = affiliations.getChildren();
			if (afch != null)
				for (Element element : afch) {
					affiliationWrappers.add(new AffiliationElement(element));
				}

			onRetrieve((IQ) responseStanza, node, affiliationWrappers);
		}
	}

	public static abstract class NodeConfigurationAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onReceiveConfiguration(IQ responseStanza, String node, JabberDataElement form);

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			try {
				final Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
				List<Element> tmp = pubsub.getChildren("configure");
				if (tmp == null || tmp.isEmpty()) {
					tmp = pubsub.getChildren("default");
				}
				final Element configure = tmp == null || tmp.isEmpty() ? null : tmp.get(0);

				String node = configure.getAttribute("node");

				final Element x = configure.getChildrenNS("x", "jabber:x:data");

				final JabberDataElement form = new JabberDataElement(DefaultElement.create(x));

				onReceiveConfiguration((IQ) responseStanza, node, form);
			} catch (Exception e) {
				log.log(Level.WARNING, "Processing node configuration error", e);
			}
		}

	}

	public abstract static class PublishAsyncCallback extends PubSubAsyncCallback {

		public abstract void onPublish(String itemId);

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
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
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
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

		protected abstract void onSubscribe(IQ response, SubscriptionElement subscriptionElement);

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
			Element subscription = getFirstChild(pubsub, "subscription");

			SubscriptionElement subWrapper = new SubscriptionElement(subscription);

			onSubscribe((IQ) responseStanza, subWrapper);
		}
	}

	public static class SubscriptionElement extends ElementWrapper {

		public SubscriptionElement() {
			this(new DefaultElement("subscription"));
		}

		public SubscriptionElement(Element subscription) {
			super(subscription);
		}

		public JID getJID() throws XMLException {
			String j = getAttribute("jid");
			return j == null ? null : JID.jidInstance(j);
		}

		public String getNode() throws XMLException {
			return getAttribute("node");
		}

		public String getSubID() throws XMLException {
			return getAttribute("subid");
		}

		public Subscription getSubscription() throws XMLException {
			String s = getAttribute("subscription");
			return s == null ? null : Subscription.valueOf(s);
		}

		public void setJID(JID subscriberJID) throws XMLException {
			setAttribute("jid", subscriberJID == null ? null : subscriberJID.toString());
		}

		public void setNode(String nodeName) throws XMLException {
			setAttribute("node", nodeName);
		}

		public void setSubID(String subID) throws XMLException {
			setAttribute("subid", subID);
		}

		public void setSubscription(Subscription subscription) throws XMLException {
			setAttribute("subscription", subscription == null ? null : subscription.name());
		}

	}

	/**
	 * Class with definitions of filters for retrieving subscriptions. It is for
	 * extension <code>tigase:pubsub:1</code>. It allows to filter returned
	 * subscription list for example by domain.
	 */
	public static class SubscriptionFilterExtension {

		private String jidContains;

		public String getJidContains() {
			return jidContains;
		}

		public void setJidContains(String jidContains) {
			this.jidContains = jidContains;
		}

	}

	public static abstract class SubscriptionOptionsAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onReceiveConfiguration(IQ responseStanza, String node, JID jid, JabberDataElement form);

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
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

	public static abstract class SubscriptionsRetrieveAsyncCallback extends PubSubAsyncCallback {

		protected abstract void onRetrieve(IQ response, String node, Collection<SubscriptionElement> subscriptions);

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_XMLNS);
			if (pubsub == null)
				pubsub = responseStanza.getChildrenNS("pubsub", PUBSUB_OWNER_XMLNS);
			Element subscriptions = getFirstChild(pubsub, "subscriptions");
			String node = subscriptions.getAttribute("node");

			ArrayList<SubscriptionElement> subscriptionWrappers = new ArrayList<SubscriptionElement>();
			List<Element> subch = subscriptions.getChildren();
			if (subch != null)
				for (Element element : subch) {
					subscriptionWrappers.add(new SubscriptionElement(element));
				}

			onRetrieve((IQ) responseStanza, node, subscriptionWrappers);
		}
	}

	public static final Criteria CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("event", "http://jabber.org/protocol/pubsub#event"));

	public static final EventType NotificationReceived = new EventType();

	private static final String PUBSUB_EVENT_XMLNS = "http://jabber.org/protocol/pubsub#event";

	private static final String PUBSUB_OWNER_XMLNS = "http://jabber.org/protocol/pubsub#owner";

	private static final String PUBSUB_XMLNS = "http://jabber.org/protocol/pubsub";

	private static final String QUEUEING_XMLNS = "urn:xmpp:pubsub:queueing:0";

	/**
	 * Create empty <code>jabber:x:data</code> element prepared to submit.
	 * 
	 * @return empty submitable node configuration element.
	 */
	public static JabberDataElement createNodeConfiguration() throws JaxmppException {
		JabberDataElement c = new JabberDataElement(XDataType.submit);
		c.addHiddenField("FORM_TYPE", "http://jabber.org/protocol/pubsub#node_config");
		return c;
	}

	private final DateTimeFormat dtf;

	public PubSubModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(ObservableFactory.instance(parentObservable), sessionObject, packetWriter);
		dtf = new DateTimeFormat();
	}

	/**
	 * Submit configuration of node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param configuration
	 *            data element with configuration
	 * @param callback
	 *            request callback
	 */
	public void configureNode(BareJID pubSubJID, String nodeName, JabberDataElement configuration, AsyncCallback callback)
			throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_OWNER_XMLNS);
		iq.addChild(pubsub);

		final Element configure = new DefaultElement("configure");
		configure.setAttribute("node", nodeName);
		pubsub.addChild(configure);

		configure.addChild(configuration.createSubmitableElement(XDataType.submit));

		writer.write(iq, callback);
	}

	/**
	 * Submit configuration of node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param configuration
	 *            data element with configuration
	 * @param callback
	 *            request callback
	 */
	public void configureNode(BareJID pubSubJID, String nodeName, JabberDataElement configuration, PubSubAsyncCallback callback)
			throws JaxmppException {
		configureNode(pubSubJID, nodeName, configuration, (AsyncCallback) callback);

	}

	/**
	 * Submit configuration of subscription.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param subscriberJID
	 *            subscriber JID
	 * @param form
	 *            data element with configuration
	 * @param callback
	 *            request callback
	 */
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

	/**
	 * Submit configuration of subscription.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param subscriberJID
	 *            subscriber JID
	 * @param form
	 *            data element with configuration
	 * @param callback
	 *            request callback
	 */
	public void configureSubscription(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement form,
			PubSubAsyncCallback callback) throws JaxmppException {
		configureSubscription(pubSubJID, nodeName, subscriberJID, form, (AsyncCallback) callback);
	}

	/**
	 * Create node on PubSub Service.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node to be created
	 * @param callback
	 *            callback
	 */
	public void createNode(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		createNode(pubSubJID, nodeName, null, callback);
	}

	/**
	 * Create node on PubSub Service.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node to be created
	 * @param nodeConfiguration
	 *            node configuration
	 * @param callback
	 *            callback
	 */
	public void createNode(BareJID pubSubJID, String nodeName, JabberDataElement nodeConfiguration, AsyncCallback callback)
			throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element create = new DefaultElement("create");
		create.setAttribute("node", nodeName);
		pubsub.addChild(create);

		if (nodeConfiguration != null) {
			final Element configure = new DefaultElement("configure");
			configure.addChild(nodeConfiguration.createSubmitableElement(XDataType.submit));
			pubsub.addChild(configure);
		}

		writer.write(iq, callback);
	}

	/**
	 * Create node on PubSub Service.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node to be created
	 * @param nodeConfiguration
	 *            node configuration
	 * @param callback
	 *            callback
	 */
	public void createNode(BareJID pubSubJID, String nodeName, JabberDataElement nodeConfiguration, PubSubAsyncCallback callback)
			throws JaxmppException {
		createNode(pubSubJID, nodeName, nodeConfiguration, (AsyncCallback) callback);
	}

	/**
	 * Create node on PubSub Service.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node to be created
	 * @param config
	 *            node configuration
	 * @param callback
	 *            callback
	 */
	public void createNode(BareJID pubSubJID, String nodeName, PubSubAsyncCallback callback) throws JaxmppException {
		createNode(pubSubJID, nodeName, null, (AsyncCallback) callback);
	}

	/**
	 * Delete an entity from the affiliations list. Formelly it sets affiliation
	 * to <code>{@linkplain Affiliation#none none}</code>.
	 * 
	 * @param pubSubJID
	 * @param nodeName
	 * @param subscriberJID
	 * @param callback
	 * @throws JaxmppException
	 */
	public void deleteAffiliation(BareJID pubSubJID, String nodeName, JID subscriberJID, PubSubAsyncCallback callback)
			throws JaxmppException {
		setAffiliation(pubSubJID, nodeName, subscriberJID, Affiliation.none, callback);
	}

	/**
	 * Delete published item.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node
	 * @param itemId
	 *            ID of item
	 * @param callback
	 *            request callback
	 */
	public void deleteItem(BareJID pubSubJID, String nodeName, String itemId, AsyncCallback callback) throws JaxmppException {
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

	/**
	 * Delete published item.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node
	 * @param itemId
	 *            ID of item
	 * @param callback
	 *            request callback
	 */
	public void deleteItem(BareJID pubSubJID, String nodeName, String itemId, PubSubAsyncCallback callback)
			throws JaxmppException {
		deleteItem(pubSubJID, nodeName, itemId, (AsyncCallback) callback);
	}

	/**
	 * Delete node from PubSub service.
	 * 
	 * @param pubSubJID
	 *            address of PubSub service
	 * @param nodeName
	 *            na of node to delete
	 * @param callback
	 *            callback
	 */
	public void deleteNode(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, "http://jabber.org/protocol/pubsub#owner");
		iq.addChild(pubsub);

		final Element delete = new DefaultElement("delete");
		delete.setAttribute("node", nodeName);
		pubsub.addChild(delete);

		writer.write(iq, callback);
	}

	/**
	 * Delete node from PubSub service.
	 * 
	 * @param pubSubJID
	 *            address of PubSub service
	 * @param nodeName
	 *            na of node to delete
	 * @param callback
	 *            callback
	 */
	public void deleteNode(BareJID pubSubJID, String nodeName, PubSubAsyncCallback callback) throws JaxmppException {
		deleteNode(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Delete subscriber. Formelly it sets subscription to
	 * <code>{@linkplain Subscription#none none}</code>.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node
	 * @param subscriberJID
	 *            subscriber JID
	 * @param callback
	 *            request callback
	 */
	public void deleteSubscription(BareJID pubSubJID, String nodeName, JID subscriberJID, PubSubAsyncCallback callback)
			throws JaxmppException {
		setSubscription(pubSubJID, nodeName, subscriberJID, Subscription.none, callback);
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

	/**
	 * Gets default subscription configuration.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param callback
	 *            request callback.
	 */
	public void getDefaultSubscriptionConfiguration(BareJID pubSubJID, String nodeName, AsyncCallback callback)
			throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.get);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element def = new DefaultElement("default");
		def.setAttribute("node", nodeName);
		pubsub.addChild(def);

		writer.write(iq, callback);
	}

	/**
	 * Gets default subscription configuration.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param callback
	 *            request callback.
	 */
	public void getDefaultSubscriptionConfiguration(BareJID pubSubJID, String nodeName,
			SubscriptionOptionsAsyncCallback callback) throws JaxmppException {
		getDefaultSubscriptionConfiguration(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	/**
	 * Get node configuration. May be used to get default node configuration.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            node name. If <code>null</code> then default node
	 *            configuration will be requested.
	 * @param callback
	 *            request callback.
	 */
	public void getNodeConfiguration(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.get);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_OWNER_XMLNS);
		iq.addChild(pubsub);

		final Element configure;
		if (nodeName == null) {
			configure = new DefaultElement("default");
		} else {
			configure = new DefaultElement("configure");
			configure.setAttribute("node", nodeName);
		}
		pubsub.addChild(configure);

		writer.write(iq, callback);
	}

	/**
	 * Get node configuration. May be used to get default node configuration.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            node name. If <code>null</code> then default node
	 *            configuration will be requested.
	 * @param callback
	 *            request callback.
	 */
	public void getNodeConfiguration(BareJID pubSubJID, String nodeName, NodeConfigurationAsyncCallback callback)
			throws JaxmppException {
		getNodeConfiguration(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Get default node configuration.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param callback
	 *            request callback
	 */
	public void getNodeConfigurationDefault(BareJID pubSubJID, AsyncCallback callback) throws JaxmppException {
		getNodeConfiguration(pubSubJID, null, callback);
	}

	/**
	 * Get default node configuration.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param callback
	 *            request callback
	 */
	public void getNodeConfigurationDefault(BareJID pubSubJID, NodeConfigurationAsyncCallback callback) throws JaxmppException {
		getNodeConfiguration(pubSubJID, null, (AsyncCallback) callback);
	}

	/**
	 * Get subscription options.
	 * 
	 * @param pubSubJID
	 *            JID of PubSub Service
	 * @param nodeName
	 *            node name
	 * @param subscriberJID
	 *            subcriber JID
	 * @param callback
	 *            request callback
	 */
	public void getSubscriptionConfiguration(BareJID pubSubJID, String nodeName, JID subscriberJID, AsyncCallback callback)
			throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.get);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_XMLNS);
		iq.addChild(pubsub);

		final Element options = new DefaultElement("options");
		options.setAttribute("node", nodeName);
		options.setAttribute("jid", subscriberJID.toString());
		pubsub.addChild(options);

		writer.write(iq, callback);
	}

	/**
	 * Get subscriptions options.
	 * 
	 * @param pubSubJID
	 *            JID of PubSub Service
	 * @param nodeName
	 *            node name
	 * @param subscriberJID
	 *            subcriber JID
	 * @param callback
	 *            request callback
	 */
	public void getSubscriptionConfiguration(BareJID pubSubJID, String nodeName, JID subscriberJID,
			SubscriptionOptionsAsyncCallback callback) throws JaxmppException {
		getSubscriptionConfiguration(pubSubJID, nodeName, subscriberJID, (AsyncCallback) callback);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Publish item in PubSub service.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param itemId
	 *            ID of item to be published. If <code>null</code> then Service
	 *            will generate unique item ID.
	 * @param payload
	 *            element to be publish.
	 * @param callback
	 *            request callback.
	 */
	public void publishItem(BareJID pubSubJID, String nodeName, String itemId, Element payload, AsyncCallback callback)
			throws JaxmppException {
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

	/**
	 * Publish item in PubSub service.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param itemId
	 *            ID of item to be published. If <code>null</code> then Service
	 *            will generate unique item ID.
	 * @param payload
	 *            element to be publish.
	 * @param callback
	 *            request callback.
	 */
	public void publishItem(BareJID pubSubJID, String nodeName, String itemId, Element payload, PublishAsyncCallback callback)
			throws JaxmppException {
		publishItem(pubSubJID, nodeName, itemId, payload, (AsyncCallback) callback);
	}

	/**
	 * Purge the node of all published items.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param callback
	 *            request callback
	 */
	public void purge(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);
		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_OWNER_XMLNS);
		iq.addChild(pubsub);

		final Element purge = new DefaultElement("purge");
		purge.setAttribute("node", nodeName);
		pubsub.addChild(purge);

		writer.write(iq, callback);
	}

	/**
	 * Purge the node of all published items.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param callback
	 *            request callback
	 */
	public void purge(BareJID pubSubJID, String nodeName, PubSubAsyncCallback callback) throws JaxmppException {
		purge(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Retrieve affiliations. Owner affiliation is required.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node. If <code>null</code> then affiliations from all
	 *            nodes will be requested.
	 * @param callback
	 *            request callback.
	 */
	public void retrieveAffiliations(BareJID pubSubJID, String nodeName, AffiliationsRetrieveAsyncCallback callback)
			throws JaxmppException {
		retrieveAffiliations(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Retrieve affiliations. Owner affiliation is required.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node. If <code>null</code> then affiliations from all
	 *            nodes will be requested.
	 * @param callback
	 *            request callback.
	 */
	public void retrieveAffiliations(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		retrieveAffiliations(pubSubJID, nodeName, PUBSUB_OWNER_XMLNS, callback);
	}

	protected void retrieveAffiliations(BareJID pubSubJID, String nodeName, String xmlns, AsyncCallback callback)
			throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.get);
		final Element pubsub = new DefaultElement("pubsub", null, xmlns);
		iq.addChild(pubsub);

		final Element affiliations = new DefaultElement("affiliations");
		affiliations.setAttribute("node", nodeName);
		pubsub.addChild(affiliations);

		writer.write(iq, callback);
	}

	/**
	 * Gets ALL published items from node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param callback
	 *            request callback
	 */
	public void retrieveItem(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		retrieveItem(pubSubJID, nodeName, null, null, callback);
	}

	/**
	 * Gets ALL published items from node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param callback
	 *            request callback
	 */
	public void retrieveItem(BareJID pubSubJID, String nodeName, RetrieveItemsAsyncCallback callback) throws JaxmppException {
		retrieveItem(pubSubJID, nodeName, null, null, callback);
	}

	/**
	 * Gets published item from node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param itemId
	 *            ID of item to pe retrieve.
	 * @param callback
	 *            request callback
	 */
	public void retrieveItem(BareJID pubSubJID, String nodeName, String itemId, AsyncCallback callback) throws JaxmppException {
		retrieveItem(pubSubJID, nodeName, itemId, null, callback);
	}

	/**
	 * Gets published item(s) from node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param itemId
	 *            ID of item to pe retrieve. May be <code>null</code>. Then all
	 *            items will be requested.
	 * @param maxItems
	 *            maximum amount of items to be retrieve.
	 * @param callback
	 *            request callback
	 */
	public void retrieveItem(BareJID pubSubJID, String nodeName, String itemId, Integer maxItems, AsyncCallback callback)
			throws JaxmppException {
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

	/**
	 * Gets published item from node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node
	 * @param itemId
	 *            ID of item to pe retrieve.
	 * @param callback
	 *            request callback
	 */
	public void retrieveItem(BareJID pubSubJID, String nodeName, String itemId, RetrieveItemsAsyncCallback callback)
			throws JaxmppException {
		retrieveItem(pubSubJID, nodeName, itemId, null, callback);
	}

	/**
	 * Retrieve own affiliations.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node. If <code>null</code> then affiliations from all
	 *            nodes will be requested.
	 * @param callback
	 *            request callback.
	 */
	public void retrieveOwnAffiliations(BareJID pubSubJID, String nodeName, AffiliationsRetrieveAsyncCallback callback)
			throws JaxmppException {
		retrieveOwnAffiliations(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Retrieve own affiliations.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node. If <code>null</code> then affiliations from all
	 *            nodes will be requested.
	 * @param callback
	 *            request callback.
	 */
	public void retrieveOwnAffiliations(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		retrieveAffiliations(pubSubJID, nodeName, PUBSUB_XMLNS, callback);
	}

	/**
	 * Retrieve own subscriptions.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node. If <code>null</code> then subscriptions from all
	 *            nodes will be requested.
	 * @param callback
	 *            request callback
	 */
	public void retrieveOwnSubscription(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		retrieveSubscription(pubSubJID, nodeName, PUBSUB_XMLNS, null, callback);
	}

	/**
	 * Retrieve own subscriptions.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node. If <code>null</code> then subscriptions from all
	 *            nodes will be requested.
	 * @param callback
	 *            request callback
	 */
	public void retrieveOwnSubscription(BareJID pubSubJID, String nodeName, SubscriptionsRetrieveAsyncCallback callback)
			throws JaxmppException {
		retrieveOwnSubscription(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Retrieve all subscriptions of given node. Owner affiliation is required.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param callback
	 *            request callback
	 */
	public void retrieveSubscription(BareJID pubSubJID, String nodeName, AsyncCallback callback) throws JaxmppException {
		retrieveSubscription(pubSubJID, nodeName, PUBSUB_OWNER_XMLNS, null, callback);
	}

	protected void retrieveSubscription(BareJID pubSubJID, String nodeName, String xmlns,
			SubscriptionFilterExtension filterExt, AsyncCallback callback) throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.get);
		final Element pubsub = new DefaultElement("pubsub", null, xmlns);
		iq.addChild(pubsub);

		final Element subscriptions = new DefaultElement("subscriptions");
		subscriptions.setAttribute("node", nodeName);
		pubsub.addChild(subscriptions);

		if (filterExt != null) {
			Element filter = new DefaultElement("filter", null, "tigase:pubsub:1");

			if (filterExt.getJidContains() != null) {
				Element f = new DefaultElement("jid", null, null);
				f.setAttribute("contains", filterExt.getJidContains());
				filter.addChild(f);
			}

			subscriptions.addChild(filter);
		}

		writer.write(iq, callback);
	}

	/**
	 * Retrieve all subscriptions of given node. Owner affiliation is required.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param filterExt
	 *            subscription filter.
	 * @param callback
	 *            request callback
	 */
	public void retrieveSubscription(BareJID pubSubJID, String nodeName, SubscriptionFilterExtension filterExt,
			AsyncCallback callback) throws JaxmppException {
		retrieveSubscription(pubSubJID, nodeName, PUBSUB_OWNER_XMLNS, filterExt, callback);
	}

	/**
	 * Retrieve all subscriptions of given node. Owner affiliation is required.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param callback
	 *            request callback
	 */
	public void retrieveSubscription(BareJID pubSubJID, String nodeName, SubscriptionsRetrieveAsyncCallback callback)
			throws JaxmppException {
		retrieveSubscription(pubSubJID, nodeName, (AsyncCallback) callback);
	}

	/**
	 * Modify or set multiple affiliations.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param affiliationElements
	 *            array of affiliations elements.
	 * @param callback
	 *            request callback
	 */
	public void setAffiliation(BareJID pubSubJID, String nodeName, AffiliationElement[] affiliationElements,
			PubSubAsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_OWNER_XMLNS);
		iq.addChild(pubsub);

		final Element affiliations = new DefaultElement("affiliations");
		affiliations.setAttribute("node", nodeName);
		pubsub.addChild(affiliations);

		for (AffiliationElement affiliationElement : affiliationElements) {
			affiliations.addChild(affiliationElement);
		}

		writer.write(iq, callback);
	}

	/**
	 * Modify or set affiliation.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node
	 * @param subscriberJID
	 *            subscriber JID
	 * @param affiliation
	 *            new affiliation.
	 * @param callback
	 *            request callback
	 */
	public void setAffiliation(BareJID pubSubJID, String nodeName, JID subscriberJID, Affiliation affiliation,
			PubSubAsyncCallback callback) throws JaxmppException {
		AffiliationElement ae = new AffiliationElement();
		ae.setJID(subscriberJID);
		ae.setAffiliation(affiliation);

		setAffiliation(pubSubJID, nodeName, new AffiliationElement[] { ae }, callback);
	}

	/**
	 * Modify or set subscription.
	 * 
	 * @param pubSubJID
	 *            PubSub service address
	 * @param nodeName
	 *            name of node
	 * @param subscriberJID
	 *            subscriber JID
	 * @param subscription
	 *            new subscription
	 * @param callback
	 *            request callback
	 */
	public void setSubscription(BareJID pubSubJID, String nodeName, JID subscriberJID, Subscription subscription,
			PubSubAsyncCallback callback) throws JaxmppException {
		SubscriptionElement se = new SubscriptionElement();
		se.setJID(subscriberJID);
		se.setSubscription(subscription);

		setSubscription(pubSubJID, nodeName, new SubscriptionElement[] { se }, callback);
	}

	/**
	 * Modify or set multiple subscriptions.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param subscriptionElements
	 *            array of subscription elements.
	 * @param callback
	 *            request callback
	 */
	public void setSubscription(BareJID pubSubJID, String nodeName, SubscriptionElement[] subscriptionElements,
			PubSubAsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance(pubSubJID));
		iq.setType(StanzaType.set);

		final Element pubsub = new DefaultElement("pubsub", null, PUBSUB_OWNER_XMLNS);
		iq.addChild(pubsub);

		final Element subscriptions = new DefaultElement("subscriptions");
		subscriptions.setAttribute("node", nodeName);
		pubsub.addChild(subscriptions);

		for (SubscriptionElement subscriptionElement : subscriptionElements) {
			subscriptions.addChild(subscriptionElement);
		}

		writer.write(iq, callback);
	}

	/**
	 * Subscribe to a Node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node to be subscribe
	 * @param subscriberJID
	 *            subscriber JID.
	 * @param options
	 *            subscription options. <code>null</code> is allowed.
	 * @param callback
	 *            request callback.
	 */
	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement options,
			AsyncCallback callback) throws JaxmppException {
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

	/**
	 * Subscribe to a Node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node to be subscribe
	 * @param subscriberJID
	 *            subscriber JID.
	 * @param options
	 *            subscription options. <code>null</code> is allowed.
	 * @param callback
	 *            request callback.
	 */
	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, JabberDataElement options,
			SubscriptionAsyncCallback callback) throws JaxmppException {
		subscribe(pubSubJID, nodeName, subscriberJID, options, (AsyncCallback) callback);
	}

	/**
	 * Subscribe to a Node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node to be subscribe
	 * @param subscriberJID
	 *            subscriber JID.
	 * @param callback
	 *            request callback.
	 */
	public void subscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, SubscriptionAsyncCallback callback)
			throws JaxmppException {
		subscribe(pubSubJID, nodeName, subscriberJID, null, (AsyncCallback) callback);
	}

	/**
	 * Unlock assigned item. This method may be used with PubSub service and
	 * nodes with active <a
	 * href='http://xmpp.org/extensions/xep-0254.html'>PubSub Queueing</a>.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param itemId
	 *            ID of item to be unlock.
	 * @param callback
	 *            request callback.
	 */
	public void unlockItem(BareJID pubSubJID, String nodeName, String itemId, AsyncCallback callback) throws JaxmppException {
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

	/**
	 * Unlock assigned item. This method may be used with PubSub service and
	 * nodes with active <a
	 * href='http://xmpp.org/extensions/xep-0254.html'>PubSub Queueing</a>.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node.
	 * @param itemId
	 *            ID of item to be unlock.
	 * @param callback
	 *            request callback.
	 */
	public void unlockItem(BareJID pubSubJID, String nodeName, String itemId, PubSubAsyncCallback callback)
			throws JaxmppException {
		unlockItem(pubSubJID, nodeName, itemId, (AsyncCallback) callback);
	}

	/**
	 * Unsubscribe from a Node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node to be unsubscribe.
	 * @param subscriberJID
	 *            JID of subscriber
	 * @param callback
	 *            request callback
	 */
	public void unsubscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, AsyncCallback callback)
			throws JaxmppException {
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

	/**
	 * Unsubscribe from a Node.
	 * 
	 * @param pubSubJID
	 *            PubSub service address.
	 * @param nodeName
	 *            name of node to be unsubscribe.
	 * @param subscriberJID
	 *            JID of subscriber
	 * @param callback
	 *            request callback
	 */
	public void unsubscribe(BareJID pubSubJID, String nodeName, JID subscriberJID, PubSubAsyncCallback callback)
			throws JaxmppException {
		unsubscribe(pubSubJID, nodeName, subscriberJID, (AsyncCallback) callback);
	}

}