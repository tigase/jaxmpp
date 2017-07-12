/*
 * RosterModule.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.roster;

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppSessionLogic.XmppSessionEstablishedHandler;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.*;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem.Subscription;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule.ItemAddedHandler.ItemAddedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule.ItemRemovedHandler.ItemRemovedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule.ItemUpdatedHandler.ItemUpdatedEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.*;

/**
 * Module for roster manipulation.
 */
public class RosterModule
		extends AbstractIQModule
		implements ContextAware, InitializingModule, XmppSessionEstablishedHandler {

	public static final Criteria CRIT = ElementCriteria.name("iq")
			.add(ElementCriteria.name("query", new String[]{"xmlns"}, new String[]{"jabber:iq:roster"}));
	public static final String ROSTER_STORE_KEY = "RosterModule#ROSTER_STORE";

	public enum Action {
		askCancelled,
		subscribed,
		unsubscribed,

	}

	private RosterCacheProvider versionProvider;

	private static final Element createItem(final RosterItem item) throws XMLException {
		Element result = ElementFactory.create("item");
		result.setAttribute("jid", item.getJid().toString());
		result.setAttribute("name", item.getName());
		for (String gr : item.getGroups()) {
			result.addChild(ElementFactory.create("group", gr, null));
		}
		return result;
	}

	final static RosterItem fill(final RosterItem rosterItem, String name, Subscription subscription,
								 Collection<String> groups, boolean ask, boolean approved) {
		rosterItem.setName(name);
		rosterItem.setSubscription(subscription);
		rosterItem.setAsk(ask);
		rosterItem.setApproved(approved);
		if (groups != null) {
			rosterItem.getGroups().clear();
			rosterItem.getGroups().addAll(groups);
		}
		return rosterItem;
	}

	public static RosterStore getRosterStore(SessionObject sessionObject) {
		return sessionObject.getProperty(ROSTER_STORE_KEY);
	}

	private final static Subscription getSubscription(String x) {
		if (x == null) {
			return null;
		}
		return Subscription.valueOf(x);
	}

	public static void setRosterStore(SessionObject sessionObject, RosterStore rosterStore) {
		rosterStore.setSessionObject(sessionObject);
		sessionObject.setProperty(Scope.user, ROSTER_STORE_KEY, rosterStore);
	}

	public RosterModule() {
		super();
	}

	public RosterModule(RosterCacheProvider versionProvider) {
		super();
		this.versionProvider = versionProvider;
	}

	protected void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback)
			throws JaxmppException {
		RosterItem item = new RosterItem(jid, context.getSessionObject());
		fill(item, name, Subscription.none, groups, false, false);

		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(ElementFactory.create("query", null, "jabber:iq:roster"));
		query.addChild(createItem(item));

		AsyncCallback c = asyncCallback != null ? asyncCallback : new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
			}

			@Override
			public void onTimeout() throws XMLException {
			}
		};

		write(iq, c);
	}

	@Override
	public void afterRegister() {
		loadFromCache();
	}

	@Override
	public void beforeRegister() {
		if (context == null) {
			throw new RuntimeException("Context cannot be null!");
		}

		RosterStore rosterStore = RosterModule.getRosterStore(context.getSessionObject());
		if (rosterStore == null) {
			rosterStore = new DefaultRosterStore();
			RosterModule.setRosterStore(context.getSessionObject(), rosterStore);
		}

		getRosterStore().setHandler(new RosterStore.Handler() {

			@Override
			public void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback)
					throws JaxmppException {
				RosterModule.this.add(jid, name, groups, asyncCallback);
			}

			@Override
			public void cleared() {
				RosterModule.this.onRosterCleared();
			}

			@Override
			public void remove(BareJID jid) throws JaxmppException {
				RosterModule.this.remove(jid);
			}

			@Override
			public void update(RosterItem item) throws JaxmppException {
				RosterModule.this.update(item);
			}
		});
		context.getEventBus()
				.addHandler(AbstractSessionObject.ClearedHandler.ClearedEvent.class,
							new AbstractSessionObject.ClearedHandler() {

								@Override
								public void onCleared(SessionObject sessionObject, Set<Scope> scopes)
										throws JaxmppException {
									if (scopes.contains(Scope.user)) {
										getRosterStore().clear();
									}
								}
							});
		if (this.versionProvider == null) {
			this.versionProvider = UniversalFactory.createInstance(RosterCacheProvider.class.getName());
		}
		context.getEventBus().addHandler(XmppSessionEstablishedHandler.XmppSessionEstablishedEvent.class, this);
	}

	@Override
	public void beforeUnregister() {
		// TODO Auto-generated method stub

	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public RosterStore getRosterStore() {
		return getRosterStore(context.getSessionObject());
	}

	public RosterCacheProvider getVersionProvider() {
		return versionProvider;
	}

	private boolean isRosterVersioningAvailable() throws XMLException {
		if (versionProvider == null) {
			return false;
		}
		Element features = StreamFeaturesModule.getStreamFeatures(context.getSessionObject());
		if (features == null) {
			return false;
		}
		return features.getChildrenNS("ver", "urn:xmpp:features:rosterver") != null;
	}

	private void loadFromCache() {
		if (versionProvider != null) {
			final RosterStore roster = getRosterStore();
			Collection<RosterItem> items = versionProvider.loadCachedRoster(context.getSessionObject());
			if (items != null) {
				for (RosterItem rosterItem : items) {
					roster.addItem(rosterItem);
				}
			}
		}
	}

	protected void onRosterCleared() {
		loadFromCache();
	}

	@Override
	public void onXmppSessionEstablished(SessionObject sessionObject) throws JaxmppException {
		rosterRequest();
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	private void processRosterItem(final Element item) throws JaxmppException {
		final BareJID jid = BareJID.bareJIDInstance(item.getAttribute("jid"));
		final String name = item.getAttribute("name");
		final Subscription subscription = getSubscription(item.getAttribute("subscription"));
		final boolean ask = item.getAttribute("ask") != null && "subscribe".equals(item.getAttribute("ask"));
		final boolean approved = item.getAttribute("approved") != null && "true".equals(item.getAttribute("approved"));
		final ArrayList<String> groups = new ArrayList<String>();
		for (Element group : item.getChildren("group")) {
			groups.add(group.getValue());
		}

		RosterItem currentItem = getRosterStore().get(jid);
		if (subscription == Subscription.remove && currentItem != null) {
			// remove item
			HashSet<String> groupsOld = new HashSet<String>(getRosterStore().getGroups());
			fill(currentItem, name, subscription, null, ask, approved);
			getRosterStore().removeItem(jid);
			Set<String> modifiedGroups = getRosterStore().calculateModifiedGroups(groupsOld);
			fireEvent(new ItemRemovedEvent(context.getSessionObject(), currentItem, modifiedGroups));
			log.fine("Roster item " + jid + " removed");
		} else if (currentItem == null) {
			// add new item
			currentItem = new RosterItem(jid, context.getSessionObject());
			fill(currentItem, name, subscription, groups, ask, approved);
			Set<String> modifiedGroups = getRosterStore().addItem(currentItem);
			fireEvent(new ItemAddedEvent(context.getSessionObject(), currentItem, modifiedGroups));
			log.fine("Roster item " + jid + " added");
		} else if (currentItem.isAsk() && ask &&
				(subscription == Subscription.from || subscription == Subscription.none)) {
			// ask cancelled
			fill(currentItem, name, subscription, null, ask, approved);
			// store needs to know that item has changed!
			getRosterStore().addItem(currentItem);
			fireEvent(new ItemUpdatedEvent(context.getSessionObject(), currentItem, Action.askCancelled, null));
			log.fine("Roster item " + jid + " ask cancelled");
		} else if (currentItem.getSubscription() == Subscription.both && subscription == Subscription.from ||
				currentItem.getSubscription() == Subscription.to && subscription == Subscription.none) {
			// unsubscribed
			fill(currentItem, name, subscription, null, ask, approved);
			// store needs to know that item has changed!
			getRosterStore().addItem(currentItem);
			fireEvent(new ItemUpdatedEvent(context.getSessionObject(), currentItem, Action.unsubscribed, null));
			log.fine("Roster item " + jid + " unsubscribed");
		} else if (currentItem.getSubscription() == Subscription.from && subscription == Subscription.both ||
				currentItem.getSubscription() == Subscription.none && subscription == Subscription.to) {
			// subscribed
			fill(currentItem, name, subscription, null, ask, approved);
			// store needs to know that item has changed!
			getRosterStore().addItem(currentItem);
			fireEvent(new ItemUpdatedEvent(context.getSessionObject(), currentItem, Action.subscribed, null));
			log.fine("Roster item " + jid + " subscribed");
		} else {
			HashSet<String> groupsOld = new HashSet<String>(getRosterStore().getGroups());
			fill(currentItem, name, subscription, groups, ask, approved);
			// store needs to know that item has changed!
			getRosterStore().addItem(currentItem);
			Set<String> modifiedGroups = getRosterStore().calculateModifiedGroups(groupsOld);
			fireEvent(new ItemUpdatedEvent(context.getSessionObject(), currentItem, null, modifiedGroups));
			log.fine("Roster item " + jid + " updated");
		}

	}

	private void processRosterQuery(final Element query, boolean force) throws JaxmppException {
		if (query != null) {
			if (force) {
				getRosterStore().removeAll();
			}

			List<Element> items = query.getChildren("item");
			String ver = query.getAttribute("ver");
			for (Element element : items) {
				processRosterItem(element);
			}

			if (versionProvider != null && ver != null) {
				versionProvider.updateReceivedVersion(context.getSessionObject(), ver);
			}
		}
	}

	@Override
	protected void processSet(final IQ stanza) throws JaxmppException {
		final JID bindedJid = context.getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (stanza.getFrom() != null && !stanza.getFrom().getBareJid().equals(bindedJid.getBareJid())) {
			throw new XMPPException(ErrorCondition.not_allowed);
		}

		Element query = stanza.getQuery();
		processRosterQuery(query, false);
	}

	protected void remove(BareJID jid) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(ElementFactory.create("query", null, "jabber:iq:roster"));
		Element item = query.addChild(ElementFactory.create("item"));
		item.setAttribute("jid", jid.toString());
		item.setAttribute("subscription", Subscription.remove.name());

		write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
			}

			@Override
			public void onTimeout() throws XMLException {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * Requests for roster. Roster will be send by server asynchronously.
	 */
	public void rosterRequest() throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		Element query = ElementFactory.create("query", null, "jabber:iq:roster");
		if (isRosterVersioningAvailable()) {
			String x = versionProvider.getCachedVersion(context.getSessionObject());
			if (getRosterStore().getCount() == 0) {
				x = "";
				versionProvider.updateReceivedVersion(context.getSessionObject(), x);
			}
			if (x != null) {
				query.setAttribute("ver", x);
			}
		}
		iq.addChild(query);

		write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
				Element query = ((IQ) responseStanza).getQuery();

				processRosterQuery(query, true);
			}

			@Override
			public void onTimeout() throws XMLException {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	protected void update(RosterItem item) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(ElementFactory.create("query", null, "jabber:iq:roster"));
		query.addChild(createItem(item));

		write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
			}

			@Override
			public void onTimeout() throws XMLException {
				// TODO Auto-generated method stub

			}
		});
	}

	public interface ItemAddedHandler
			extends EventHandler {

		void onItemAdded(SessionObject sessionObject, RosterItem item, Set<String> modifiedGroups);

		class ItemAddedEvent
				extends JaxmppEvent<ItemAddedHandler> {

			private RosterItem item;

			private Set<String> modifiedGroups;

			public ItemAddedEvent(SessionObject sessionObject, RosterItem currentItem, Set<String> modifiedGroups) {
				super(sessionObject);
				this.item = currentItem;
				this.modifiedGroups = modifiedGroups;
			}

			@Override
			public void dispatch(ItemAddedHandler handler) {
				handler.onItemAdded(sessionObject, item, modifiedGroups);
			}

			public RosterItem getItem() {
				return item;
			}

			public void setItem(RosterItem item) {
				this.item = item;
			}

			public Set<String> getModifiedGroups() {
				return modifiedGroups;
			}

			public void setModifiedGroups(Set<String> modifiedGroups) {
				this.modifiedGroups = modifiedGroups;
			}

		}
	}

	public interface ItemRemovedHandler
			extends EventHandler {

		void onItemRemoved(SessionObject sessionObject, RosterItem item, Set<String> modifiedGroups);

		class ItemRemovedEvent
				extends JaxmppEvent<ItemRemovedHandler> {

			private RosterItem item;

			private Set<String> modifiedGroups;

			public ItemRemovedEvent(SessionObject sessionObject, RosterItem currentItem, Set<String> modifiedGroups) {
				super(sessionObject);
				this.item = currentItem;
				this.modifiedGroups = modifiedGroups;
			}

			@Override
			public void dispatch(ItemRemovedHandler handler) {
				handler.onItemRemoved(sessionObject, item, modifiedGroups);
			}

			public RosterItem getItem() {
				return item;
			}

			public void setItem(RosterItem item) {
				this.item = item;
			}

			public Set<String> getModifiedGroups() {
				return modifiedGroups;
			}

			public void setModifiedGroups(Set<String> modifiedGroups) {
				this.modifiedGroups = modifiedGroups;
			}

		}
	}

	public interface ItemUpdatedHandler
			extends EventHandler {

		void onItemUpdated(SessionObject sessionObject, RosterItem item, Action action, Set<String> modifiedGroups);

		class ItemUpdatedEvent
				extends JaxmppEvent<ItemUpdatedHandler> {

			private Action action;

			private RosterItem item;

			private Set<String> modifiedGroups;

			public ItemUpdatedEvent(SessionObject sessionObject, RosterItem currentItem, Action action,
									Set<String> modifiedGroups) {
				super(sessionObject);
				this.item = currentItem;
				this.action = action;
				this.modifiedGroups = modifiedGroups;
			}

			@Override
			public void dispatch(ItemUpdatedHandler handler) {
				handler.onItemUpdated(sessionObject, item, action, modifiedGroups);
			}

			public Action getChangeAction() {
				return action;
			}

			public void setChangeAction(Action action) {
				this.action = action;
			}

			public RosterItem getItem() {
				return item;
			}

			public void setItem(RosterItem item) {
				this.item = item;
			}

			public Set<String> getModifiedGroups() {
				return modifiedGroups;
			}

			public void setModifiedGroups(Set<String> modifiedGroups) {
				this.modifiedGroups = modifiedGroups;
			}

		}
	}
}