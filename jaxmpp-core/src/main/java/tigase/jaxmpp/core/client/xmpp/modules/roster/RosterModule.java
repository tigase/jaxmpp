package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem.Subscription;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule.RosterEvent.ChangeAction;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class RosterModule extends AbstractIQModule {

	public static class RosterEvent extends BaseEvent {

		public static enum ChangeAction {
			askCancelled,
			subscribed,
			unsubscribed,

		}

		private static final long serialVersionUID = 1L;

		private RosterItem item;

		RosterEvent(EventType type, RosterItem item) {
			this(type, item, null);
		}

		RosterEvent(EventType type, RosterItem item, ChangeAction action) {
			super(type);
			this.item = item;
		}

		public RosterItem getItem() {
			return item;
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

	private final static RosterItem fill(final RosterItem rosterItem, String name, Subscription subscription,
			Collection<String> groups, boolean ask) {
		rosterItem.setName(name);
		rosterItem.setSubscription(subscription);
		rosterItem.setAsk(ask);
		rosterItem.getGroups().clear();
		rosterItem.getGroups().addAll(groups);
		return rosterItem;
	}

	private final static Subscription getSubscription(String x) {
		if (x == null)
			return null;
		return Subscription.valueOf(x);
	}

	private final Observable observable;

	public RosterModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = new Observable(parentObservable);
		sessionObject.getRoster().setHandler(new RosterStore.Handler() {

			@Override
			public void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback)
					throws XMLException, JaxmppException {
				RosterModule.this.add(jid, name, groups, asyncCallback);
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
	}

	protected void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback) throws XMLException,
			JaxmppException {
		RosterItem item = new RosterItem(jid);
		fill(item, name, Subscription.none, groups, false);

		IQ iq = IQ.create();
		iq.setTo(getRosterRequestsReceiver());
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(new DefaultElement("query xmlns", null, "jabber:iq:roster"));
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

		sessionObject.registerResponseHandler(iq, c);
		writer.write(iq);
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	private void fireEvent(RosterEvent event) {
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

	private JID getRosterRequestsReceiver() {
		JID j = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (j == null)
			return null;
		return JID.jidInstance(j.getBareJid());
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	private void processRosterItem(final Element item) throws XMLException {
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
			fill(currentItem, name, subscription, groups, ask);
			event = new RosterEvent(ItemRemoved, currentItem);
			sessionObject.getRoster().removeItem(jid);
			log.fine("Roster item " + jid + " removed");
		} else if (currentItem == null) {
			// add new item
			currentItem = new RosterItem(jid);
			event = new RosterEvent(ItemAdded, currentItem);
			fill(currentItem, name, subscription, groups, ask);
			sessionObject.getRoster().addItem(currentItem);
			log.fine("Roster item " + jid + " added");
		} else if (currentItem.isAsk() && ask && (subscription == Subscription.from || subscription == Subscription.none)) {
			// ask cancelled
			fill(currentItem, name, subscription, groups, ask);
			event = new RosterEvent(ItemUpdated, currentItem, ChangeAction.askCancelled);
			log.fine("Roster item " + jid + " ask cancelled");
		} else if (currentItem.getSubscription() == Subscription.both && subscription == Subscription.from
				|| currentItem.getSubscription() == Subscription.to && subscription == Subscription.none) {
			// unsubscribed
			fill(currentItem, name, subscription, groups, ask);
			event = new RosterEvent(ItemUpdated, currentItem, ChangeAction.unsubscribed);
			log.fine("Roster item " + jid + " unsubscribed");
		} else if (currentItem.getSubscription() == Subscription.from && subscription == Subscription.both
				|| currentItem.getSubscription() == Subscription.none && subscription == Subscription.to) {
			// subscribed
			fill(currentItem, name, subscription, groups, ask);
			event = new RosterEvent(ItemUpdated, currentItem, ChangeAction.subscribed);
			log.fine("Roster item " + jid + " subscribed");
		} else {
			event = new RosterEvent(ItemUpdated, currentItem);
			fill(currentItem, name, subscription, groups, ask);
			log.fine("Roster item " + jid + " updated");
		}

		fireEvent(event);
	}

	private void processRosterQuery(final Element query) throws XMLException {
		List<Element> items = query.getChildren("item");
		for (Element element : items) {
			processRosterItem(element);
		}
	}

	@Override
	protected void processSet(final IQ stanza) throws XMPPException, XMLException {
		final JID bindedJid = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (stanza.getFrom() != null && !stanza.getFrom().getDomain().equals(bindedJid.getDomain()))
			throw new XMPPException(ErrorCondition.not_allowed);

		Element query = stanza.getQuery();
		processRosterQuery(query);
	}

	protected void remove(BareJID jid) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(getRosterRequestsReceiver());
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(new DefaultElement("query xmlns", null, "jabber:iq:roster"));
		Element item = query.addChild(new DefaultElement("item"));
		item.setAttribute("jid", jid.toString());
		item.setAttribute("subscription", Subscription.remove.name());

		sessionObject.registerResponseHandler(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
				Element query = ((IQ) responseStanza).getQuery();
				processRosterQuery(query);
			}

			@Override
			public void onTimeout() throws XMLException {
				// TODO Auto-generated method stub

			}
		});
		writer.write(iq);
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void rosterRequest() throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(getRosterRequestsReceiver());
		iq.setType(StanzaType.get);
		iq.addChild(new DefaultElement("query", null, "jabber:iq:roster"));

		sessionObject.registerResponseHandler(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
				Element query = ((IQ) responseStanza).getQuery();
				processRosterQuery(query);
			}

			@Override
			public void onTimeout() throws XMLException {
				// TODO Auto-generated method stub

			}
		});
		writer.write(iq);
	}

	protected void update(RosterItem item) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(getRosterRequestsReceiver());
		iq.setType(StanzaType.set);
		final Element query = iq.addChild(new DefaultElement("query xmlns", null, "jabber:iq:roster"));
		query.addChild(createItem(item));

		sessionObject.registerResponseHandler(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
				Element query = ((IQ) responseStanza).getQuery();
				processRosterQuery(query);
			}

			@Override
			public void onTimeout() throws XMLException {
				// TODO Auto-generated method stub

			}
		});
		writer.write(iq);
	}

}
