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
package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.InitializingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem.Subscription;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule.RosterEvent.ChangeAction;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Module for roster manipulation.
 */
public class RosterModule extends AbstractIQModule implements InitializingModule {

	public static class RosterEvent extends BaseEvent {

		public static enum ChangeAction {
			askCancelled,
			subscribed,
			unsubscribed,

		}

		private static final long serialVersionUID = 1L;

		private Set<String> changedGroups;

		private RosterItem item;

		RosterEvent(EventType type, RosterItem item, ChangeAction action, SessionObject sessionObject) {
			super(type, sessionObject);
			this.item = item;
		}

		RosterEvent(EventType type, RosterItem item, SessionObject sessionObject) {
			this(type, item, null, sessionObject);
		}

		public Set<String> getChangedGroups() {
			return changedGroups;
		}

		public RosterItem getItem() {
			return item;
		}

		void setChangedGroups(Set<String> modifiedGroups) {
			this.changedGroups = modifiedGroups;
		}
	}

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("query", new String[] { "xmlns" }, new String[] { "jabber:iq:roster" }));

	public static final EventType ItemAdded = new EventType();

	public static final EventType ItemRemoved = new EventType();

	public static final EventType ItemUpdated = new EventType();

	private static final Element createItem(final RosterItem item) throws XMLException {
		Element result = new DefaultElement("item");
		result.setAttribute("jid", item.getJid().toString());
		result.setAttribute("name", item.getName());
		for (String gr : item.getGroups()) {
			result.addChild(new DefaultElement("group", gr, null));
		}
		return result;
	}

	final static RosterItem fill(final RosterItem rosterItem, String name, Subscription subscription,
			Collection<String> groups, boolean ask) {
		rosterItem.setName(name);
		rosterItem.setSubscription(subscription);
		rosterItem.setAsk(ask);
		if (groups != null) {
			rosterItem.getGroups().clear();
			rosterItem.getGroups().addAll(groups);
		}
		return rosterItem;
	}

	private final static Subscription getSubscription(String x) {
		if (x == null)
			return null;
		return Subscription.valueOf(x);
	}

	private final RosterCacheProvider versionProvider;

	public RosterModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		sessionObject.getRoster().setHandler(new RosterStore.Handler() {

			@Override
			public void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback)
					throws XMLException, JaxmppException {
				RosterModule.this.add(jid, name, groups, asyncCallback);
			}

			@Override
			public void cleared() {
				RosterModule.this.onRosterCleared();
			}

			@Override
			public void remove(BareJID jid) throws XMLException, JaxmppException {
				RosterModule.this.remove(jid);
			}

			@Override
			public void update(RosterItem item) throws XMLException, JaxmppException {
				RosterModule.this.update(item);
			}
		});
		this.versionProvider = UniversalFactory.createInstance(RosterCacheProvider.class.getName());
	}

	protected void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback) throws XMLException,
			JaxmppException {
		RosterItem item = new RosterItem(jid, sessionObject);
		item.setData(RosterItem.ID_KEY, createId(jid));
		fill(item, name, Subscription.none, groups, false);

		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(new DefaultElement("query", null, "jabber:iq:roster"));
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

		writer.write(iq, c);
	}

	@Override
	public void afterRegister() {
		loadFromCache();
	}

	@Override
	public void beforeRegister() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUnregister() {
		// TODO Auto-generated method stub

	}

	private long createId(BareJID jid) {
		return (sessionObject.getUserBareJid() + "::" + jid).hashCode();
	}

	private void fireEvent(RosterEvent event) throws JaxmppException {
		if (event == null)
			return;
		observable.fireEvent(event.getType(), event);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public RosterCacheProvider getVersionProvider() {
		return versionProvider;
	}

	private boolean isRosterVersioningAvailable() throws XMLException {
		if (versionProvider == null)
			return false;
		Element features = sessionObject.getStreamFeatures();
		if (features == null)
			return false;
		if (features.getChildrenNS("ver", "urn:xmpp:features:rosterver") != null)
			return true;
		return false;
	}

	private void loadFromCache() {
		if (versionProvider != null) {
			final RosterStore roster = sessionObject.getRoster();
			Collection<RosterItem> items = versionProvider.loadCachedRoster(sessionObject);
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
	protected void processGet(IQ element) throws XMPPException, XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	private void processRosterItem(final Element item) throws JaxmppException {
		final BareJID jid = BareJID.bareJIDInstance(item.getAttribute("jid"));
		final String name = item.getAttribute("name");
		final Subscription subscription = getSubscription(item.getAttribute("subscription"));
		final boolean ask = item.getAttribute("ask") != null && "subscribe".equals(item.getAttribute("ask"));
		final ArrayList<String> groups = new ArrayList<String>();
		for (Element group : item.getChildren("group")) {
			groups.add(group.getValue());
		}

		RosterItem currentItem = sessionObject.getRoster().get(jid);
		RosterEvent event = null;
		if (subscription == Subscription.remove && currentItem != null) {
			// remove item
			HashSet<String> groupsOld = new HashSet<String>(sessionObject.getRoster().groups);
			fill(currentItem, name, subscription, null, ask);
			event = new RosterEvent(ItemRemoved, currentItem, sessionObject);
			sessionObject.getRoster().removeItem(jid);
			Set<String> modifiedGroups = sessionObject.getRoster().calculateModifiedGroups(groupsOld);
			event.setChangedGroups(modifiedGroups);
			log.fine("Roster item " + jid + " removed");
		} else if (currentItem == null) {
			// add new item
			currentItem = new RosterItem(jid, sessionObject);
			currentItem.setData(RosterItem.ID_KEY, createId(jid));
			event = new RosterEvent(ItemAdded, currentItem, sessionObject);
			fill(currentItem, name, subscription, groups, ask);
			Set<String> modifiedGroups = sessionObject.getRoster().addItem(currentItem);
			event.setChangedGroups(modifiedGroups);
			log.fine("Roster item " + jid + " added");
		} else if (currentItem.isAsk() && ask && (subscription == Subscription.from || subscription == Subscription.none)) {
			// ask cancelled
			fill(currentItem, name, subscription, null, ask);
			event = new RosterEvent(ItemUpdated, currentItem, ChangeAction.askCancelled, sessionObject);
			log.fine("Roster item " + jid + " ask cancelled");
		} else if (currentItem.getSubscription() == Subscription.both && subscription == Subscription.from
				|| currentItem.getSubscription() == Subscription.to && subscription == Subscription.none) {
			// unsubscribed
			fill(currentItem, name, subscription, null, ask);
			event = new RosterEvent(ItemUpdated, currentItem, ChangeAction.unsubscribed, sessionObject);
			log.fine("Roster item " + jid + " unsubscribed");
		} else if (currentItem.getSubscription() == Subscription.from && subscription == Subscription.both
				|| currentItem.getSubscription() == Subscription.none && subscription == Subscription.to) {
			// subscribed
			fill(currentItem, name, subscription, null, ask);
			event = new RosterEvent(ItemUpdated, currentItem, ChangeAction.subscribed, sessionObject);
			log.fine("Roster item " + jid + " subscribed");
		} else {
			event = new RosterEvent(ItemUpdated, currentItem, sessionObject);
			HashSet<String> groupsOld = new HashSet<String>(sessionObject.getRoster().groups);
			fill(currentItem, name, subscription, groups, ask);
			Set<String> modifiedGroups = sessionObject.getRoster().calculateModifiedGroups(groupsOld);
			event.setChangedGroups(modifiedGroups);

			log.fine("Roster item " + jid + " updated");
		}

		fireEvent(event);
	}

	private void processRosterQuery(final Element query, boolean force) throws JaxmppException {
		if (query != null) {
			if (force)
				sessionObject.getRoster().removeAll();

			List<Element> items = query.getChildren("item");
			String ver = query.getAttribute("ver");
			for (Element element : items) {
				processRosterItem(element);
			}

			if (versionProvider != null && ver != null) {
				versionProvider.updateReceivedVersion(sessionObject, ver);
			}
		}
	}

	@Override
	protected void processSet(final IQ stanza) throws JaxmppException {
		final JID bindedJid = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (stanza.getFrom() != null && !stanza.getFrom().getBareJid().equals(bindedJid.getBareJid()))
			throw new XMPPException(ErrorCondition.not_allowed);

		Element query = stanza.getQuery();
		processRosterQuery(query, false);
	}

	protected void remove(BareJID jid) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(new DefaultElement("query", null, "jabber:iq:roster"));
		Element item = query.addChild(new DefaultElement("item"));
		item.setAttribute("jid", jid.toString());
		item.setAttribute("subscription", Subscription.remove.name());

		writer.write(iq, new AsyncCallback() {

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
	public void rosterRequest() throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		DefaultElement query = new DefaultElement("query", null, "jabber:iq:roster");
		if (isRosterVersioningAvailable()) {
			String x = versionProvider.getCachedVersion(sessionObject);
			if (sessionObject.getRoster().getCount() == 0) {
				x = "";
				versionProvider.updateReceivedVersion(sessionObject, x);
			}
			if (x != null)
				query.setAttribute("ver", x);
		}
		iq.addChild(query);

		writer.write(iq, new AsyncCallback() {

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

	protected void update(RosterItem item) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(new DefaultElement("query xmlns", null, "jabber:iq:roster"));
		query.addChild(createItem(item));

		writer.write(iq, new AsyncCallback() {

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
}