/*
 * DiscoveryModule.java
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

package tigase.jaxmpp.core.client.xmpp.modules.disco;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.ModuleProvider;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.ServerFeaturesReceivedHandler.ServerFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.*;

public class DiscoveryModule
		extends AbstractIQModule {

	public final static String IDENTITY_CATEGORY_KEY = "IDENTITY_CATEGORY_KEY";
	public final static String IDENTITY_TYPE_KEY = "IDENTITY_TYPE_KEY";
	public static final String INFO_XMLNS = "http://jabber.org/protocol/disco#info";
	public static final String ITEMS_XMLNS = "http://jabber.org/protocol/disco#items";
	public static final String SERVER_FEATURES_KEY = "SERVER_FEATURES_KEY";
	public static final String ACCOUNT_FEATURES_KEY = "ACCOUNT_FEATURES_KEY";
	private final NodeDetailsCallback NULL_NODE_DETAILS_CALLBACK = new DefaultNodeDetailsCallback(this);
	private final Map<String, NodeDetailsCallback> callbacks = new HashMap<String, NodeDetailsCallback>();
	private final Criteria criteria;
	private final String[] features;
	private ModuleProvider moduleProvider;

	public DiscoveryModule() {
		setNodeCallback(null, NULL_NODE_DETAILS_CALLBACK);
		this.features = new String[]{INFO_XMLNS, ITEMS_XMLNS};
		this.criteria = ElementCriteria.name("iq")
				.add(new Or(ElementCriteria.name("query", new String[]{"xmlns"}, new String[]{ITEMS_XMLNS}),
							ElementCriteria.name("query", new String[]{"xmlns"}, new String[]{INFO_XMLNS})));
	}

	public void addServerFeaturesReceivedHandler(ServerFeaturesReceivedHandler handler) {
		context.getEventBus().addHandler(ServerFeaturesReceivedHandler.ServerFeaturesReceivedEvent.class, handler);
	}

	@Override
	public void beforeRegister() {
		super.beforeRegister();
		this.moduleProvider = context.getModuleProvider();
	}

	public void discoverOwnerAccountFeatures(final DiscoInfoAsyncCallback callback) throws JaxmppException {
		final DiscoInfoAsyncCallback diac = new DiscoInfoAsyncCallback(null) {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				if (callback != null) {
					callback.onError(responseStanza, error);
				}
			}

			@Override
			protected void onInfoReceived(String node, Collection<Identity> identities, Collection<String> features)
					throws XMLException {
				HashSet<String> ff = new HashSet<String>();
				ff.addAll(features);
				context.getSessionObject().setProperty(ACCOUNT_FEATURES_KEY, ff);

				final AccountFeaturesReceivedHandler.AccountFeaturesReceivedEvent event = new AccountFeaturesReceivedHandler.AccountFeaturesReceivedEvent(
						context.getSessionObject(), (IQ) this.responseStanza, ff.toArray(new String[]{}));
				fireEvent(event);
				if (callback != null) {
					callback.onInfoReceived(node, identities, features);
				}
			}

			@Override
			public void onTimeout() throws JaxmppException {
				if (callback != null) {
					callback.onTimeout();
				}
			}
		};

		JID jid = context.getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (jid != null) {
			getInfo(JID.jidInstance(jid.getBareJid()), null, (AsyncCallback) diac);
		}
	}

	public void discoverServerFeatures(final DiscoInfoAsyncCallback callback) throws JaxmppException {
		final DiscoInfoAsyncCallback diac = new DiscoInfoAsyncCallback(null) {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				if (callback != null) {
					callback.onError(responseStanza, error);
				}
			}

			@Override
			protected void onInfoReceived(String node, Collection<Identity> identities, Collection<String> features)
					throws XMLException {
				HashSet<String> ff = new HashSet<String>();
				ff.addAll(features);
				context.getSessionObject().setProperty(SERVER_FEATURES_KEY, ff);

				final ServerFeaturesReceivedEvent event = new ServerFeaturesReceivedEvent(context.getSessionObject(),
																						  (IQ) this.responseStanza,
																						  ff.toArray(new String[]{}));
				fireEvent(event);
				if (callback != null) {
					callback.onInfoReceived(node, identities, features);
				}
			}

			@Override
			public void onTimeout() throws JaxmppException {
				if (callback != null) {
					callback.onTimeout();
				}
			}
		};

		JID jid = context.getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (jid != null) {
			getInfo(JID.jidInstance(jid.getDomain()), null, (AsyncCallback) diac);
		}
	}

	@Override
	public Criteria getCriteria() {
		return criteria;
	}

	@Override
	public String[] getFeatures() {
		return features;
	}

	public void getInfo(JID jid, DiscoInfoAsyncCallback callback) throws JaxmppException {
		getInfo(jid, null, (AsyncCallback) callback);
	}

	public void getInfo(JID jid, String node, AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		if (jid != null) {
			iq.setTo(jid);
		}
		iq.setType(StanzaType.get);
		Element query = ElementFactory.create("query", null, INFO_XMLNS);
		if (node != null) {
			query.setAttribute("node", node);
		}
		iq.addChild(query);

		write(iq, callback);
	}

	public void getInfo(JID jid, String node, DiscoInfoAsyncCallback callback) throws JaxmppException {
		getInfo(jid, node, (AsyncCallback) callback);
	}

	public void getItems(JID jid, AsyncCallback callback) throws JaxmppException {
		getItems(jid, null, callback);
	}

	public void getItems(JID jid, DiscoItemsAsyncCallback callback) throws JaxmppException {
		getItems(jid, (AsyncCallback) callback);
	}

	public void getItems(JID jid, String node, AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(jid);
		iq.setType(StanzaType.get);
		Element query = ElementFactory.create("query", null, ITEMS_XMLNS);
		if (node != null) {
			query.setAttribute("node", node);
		}
		iq.addChild(query);

		write(iq, callback);
	}

	@Override
	protected void processGet(IQ element) throws JaxmppException {
		final Element q = element.getFirstChild("query");
		final String node = q.getAttribute("node");
		final NodeDetailsCallback callback = callbacks.get(node);

		if (callback == null) {
			throw new XMPPException(ErrorCondition.item_not_found);
		}

		if (INFO_XMLNS.equals(q.getXMLNS())) {
			processGetInfo(element, q, node, callback);
		} else if (ITEMS_XMLNS.equals(q.getXMLNS())) {
			processGetItems(element, q, node, callback);
		} else {
			throw new XMPPException(ErrorCondition.bad_request);
		}
	}

	private void processGetInfo(IQ stanza, Element queryElement, String node, NodeDetailsCallback callback)
			throws JaxmppException {
		final Identity identity = callback.getIdentity(context.getSessionObject(), stanza, node);
		final String[] features = callback.getFeatures(context.getSessionObject(), stanza, node);

		Element result = XmlTools.makeResult(stanza);

		Element queryResult = ElementFactory.create("query", null, INFO_XMLNS);
		queryResult.setAttribute("node", node);
		result.addChild(queryResult);

		if (identity != null) {
			Element identityElement = ElementFactory.create("identity");
			identityElement.setAttribute("category", identity.getCategory());
			identityElement.setAttribute("type", identity.getType());
			identityElement.setAttribute("name", identity.getName());
			queryResult.addChild(identityElement);
		}

		if (features != null) {
			for (String feature : features) {
				Element f = ElementFactory.create("feature");
				f.setAttribute("var", feature);
				queryResult.addChild(f);
			}
		}

		write(result);
	}

	private void processGetItems(IQ stanza, Element queryElement, String node, NodeDetailsCallback callback)
			throws JaxmppException {
		final Item[] items = callback.getItems(context.getSessionObject(), stanza, node);

		Element result = XmlTools.makeResult(stanza);
		Element queryResult = ElementFactory.create("query", null, ITEMS_XMLNS);
		queryResult.setAttribute("node", node);
		result.addChild(queryResult);

		if (items != null) {
			for (Item it : items) {
				Element e = ElementFactory.create("item");
				if (it.getJid() != null) {
					e.setAttribute("jid", it.getJid().toString());
				}
				e.setAttribute("name", it.getName());
				e.setAttribute("node", it.getNode());

				queryResult.addChild(e);
			}
		}

		write(result);
	}

	@Override
	protected void processSet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	public void removeNodeCallback(String nodeName) {
		this.callbacks.remove(nodeName);
	}

	public void removeServerFeaturesReceivedHandler(ServerFeaturesReceivedHandler handler) {
		context.getEventBus().remove(ServerFeaturesReceivedHandler.ServerFeaturesReceivedEvent.class, handler);
	}

	public void setNodeCallback(String nodeName, NodeDetailsCallback callback) {
		this.callbacks.put(nodeName, callback == null ? NULL_NODE_DETAILS_CALLBACK : callback);
	}

	public interface AccountFeaturesReceivedHandler
			extends EventHandler {

		void onAccountFeaturesReceived(SessionObject sessionObject, IQ stanza, String[] features);

		class AccountFeaturesReceivedEvent
				extends JaxmppEvent<AccountFeaturesReceivedHandler> {

			private final String[] features;

			private final IQ stanza;

			public AccountFeaturesReceivedEvent(SessionObject sessionObject, IQ responseStanza, String[] features) {
				super(sessionObject);
				this.stanza = responseStanza;
				this.features = features;
			}

			@Override
			public void dispatch(AccountFeaturesReceivedHandler handler) {
				handler.onAccountFeaturesReceived(sessionObject, stanza, features);
			}

		}
	}

	public interface ServerFeaturesReceivedHandler
			extends EventHandler {

		void onServerFeaturesReceived(SessionObject sessionObject, IQ stanza, String[] features);

		class ServerFeaturesReceivedEvent
				extends JaxmppEvent<ServerFeaturesReceivedHandler> {

			private final String[] features;

			private final IQ stanza;

			public ServerFeaturesReceivedEvent(SessionObject sessionObject, IQ responseStanza, String[] features) {
				super(sessionObject);
				this.stanza = responseStanza;
				this.features = features;
			}

			@Override
			public void dispatch(ServerFeaturesReceivedHandler handler) {
				handler.onServerFeaturesReceived(sessionObject, stanza, features);
			}

		}
	}

	public static class DefaultNodeDetailsCallback
			implements NodeDetailsCallback {

		private DiscoveryModule discoveryModule;

		public DefaultNodeDetailsCallback(DiscoveryModule discoveryModule) {
			super();
			this.discoveryModule = discoveryModule;
		}

		@Override
		public String[] getFeatures(SessionObject sessionObject, IQ requestStanza, String node) {
			return discoveryModule.moduleProvider.getAvailableFeatures().toArray(new String[]{});
		}

		@Override
		public Identity getIdentity(SessionObject sessionObject, IQ requestStanza, String node) {
			Identity identity = new Identity();

			String category = sessionObject.getProperty(IDENTITY_CATEGORY_KEY);
			String type = sessionObject.getProperty(IDENTITY_TYPE_KEY);
			String nme = sessionObject.getProperty(SoftwareVersionModule.NAME_KEY);
			identity.setCategory(category == null ? "client" : category);
			identity.setName(nme == null ? SoftwareVersionModule.DEFAULT_NAME_VAL : nme);
			identity.setType(type == null ? "pc" : type);

			return identity;
		}

		@Override
		public Item[] getItems(SessionObject sessionObject, IQ requestStanza, String node) {
			return null;
		}

	}

	public static abstract class DiscoInfoAsyncCallback
			implements AsyncCallback {

		protected Stanza responseStanza;
		private String requestedNode;

		public DiscoInfoAsyncCallback(final String requestedNode) {
			this.requestedNode = requestedNode;
		}

		protected abstract void onInfoReceived(String node, Collection<Identity> identities,
											   Collection<String> features) throws XMLException;

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			this.responseStanza = responseStanza;
			Element query = responseStanza.getChildrenNS("query", INFO_XMLNS);
			List<Element> identities = query.getChildren("identity");
			ArrayList<Identity> idres = new ArrayList<Identity>();
			for (Element id : identities) {
				Identity t = new Identity();
				t.setName(id.getAttribute("name"));
				t.setType(id.getAttribute("type"));
				t.setCategory(id.getAttribute("category"));
				idres.add(t);
			}

			List<Element> features = query.getChildren("feature");
			ArrayList<String> feres = new ArrayList<String>();
			for (Element element : features) {
				String v = element.getAttribute("var");
				if (v != null) {
					feres.add(v);
				}
			}

			String n = query.getAttribute("node");
			onInfoReceived(n == null ? requestedNode : n, idres, feres);
		}
	}

	public static abstract class DiscoItemsAsyncCallback
			implements AsyncCallback {

		public abstract void onInfoReceived(String attribute, ArrayList<Item> items) throws XMLException;

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			final Element query = responseStanza.getChildrenNS("query", ITEMS_XMLNS);
			List<Element> ritems = query.getChildren("item");
			ArrayList<Item> items = new ArrayList<Item>();
			for (Element i : ritems) {
				Item to = new Item();
				if (i.getAttribute("jid") != null) {
					to.setJid(JID.jidInstance(i.getAttribute("jid")));
				}
				to.setName(i.getAttribute("name"));
				to.setNode(i.getAttribute("node"));
				items.add(to);
			}
			onInfoReceived(query.getAttribute("node"), items);
		}

	}

	public static class Identity {

		private String category;

		private String name;

		private String type;

		public String getCategory() {
			return category == null ? "" : category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getName() {
			return name == null ? "" : name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type == null ? "" : type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public static class Item {

		private JID jid;

		private String name;

		private String node;

		public JID getJid() {
			return jid;
		}

		public void setJid(JID jid) {
			this.jid = jid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getNode() {
			return node;
		}

		public void setNode(String node) {
			this.node = node;
		}

		@Override
		public String toString() {
			return "Item{" + "jid=" + jid + ", name=" + name + ", node=" + node + '}';
		}

	}

}
