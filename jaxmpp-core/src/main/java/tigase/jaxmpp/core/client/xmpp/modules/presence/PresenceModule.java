package tigase.jaxmpp.core.client.xmpp.modules.presence;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore.Handler;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class PresenceModule extends AbstractStanzaModule {

	public static class PresenceEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private boolean cancelled;

		private JID jid;

		private Presence presence;

		private Integer priority;

		private Show show;

		private String status;

		public PresenceEvent(EventType type) {
			super(type);
		}

		public void cancel() {
			this.cancelled = true;
		}

		public JID getJid() {
			return jid;
		}

		public Presence getPresence() {
			return presence;
		}

		public Integer getPriority() {
			return priority;
		}

		public Show getShow() {
			return show;
		}

		public String getStatus() {
			return status;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public void setJid(JID jid) {
			this.jid = jid;
		}

		public void setPresence(Presence presence) {
			this.presence = presence;
		}

		public void setPriority(Integer priority) {
			this.priority = priority;
		}

		public void setShow(Show show) {
			this.show = show;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

	public static final EventType AVAILABLE = new EventType();

	public static final EventType BEFORE_INITIAL_PRESENCE = new EventType();

	public static final Criteria CRIT = ElementCriteria.name("presence");

	public static final EventType PRESENCE_CHANGE = new EventType();

	public static final EventType SUBSCRIBE = new EventType();

	public static final EventType UNAVAILABLE = new EventType();

	private final Observable observable = new Observable();

	public PresenceModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.sessionObject.getPresence().setHandler(new Handler() {

			@Override
			public void setPresence(Show show, String status, Integer priority) throws XMLException {
				PresenceModule.this.setPresence(show, status, priority);
			}
		});
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
		return null;
	}

	public PresenceStore getPresence() {
		return this.sessionObject.getPresence();
	}

	@Override
	public void process(Stanza element) throws XMPPException, XMLException {
		final JID fromJid = element.getFrom();
		if (fromJid == null)
			return;
		final Presence presence = (Presence) element;

		boolean availableOld = sessionObject.getPresence().isAvailable(fromJid.getBareJid());
		sessionObject.getPresence().update(presence);
		boolean availableNow = sessionObject.getPresence().isAvailable(fromJid.getBareJid());

		final StanzaType type = presence.getType();

		PresenceEvent event;
		if (type == StanzaType.subscribe) {
			// subscribe
			event = new PresenceEvent(SUBSCRIBE);
		} else if (!availableOld && availableNow) {
			// sontact available
			event = new PresenceEvent(AVAILABLE);
		} else if (availableOld && !availableNow) {
			// contact unavailable
			event = new PresenceEvent(UNAVAILABLE);
		} else {
			event = new PresenceEvent(PRESENCE_CHANGE);
		}
		event.setPresence(presence);
		event.setJid(fromJid);
		event.setShow(presence.getShow());
		event.setStatus(presence.getStatus());
		event.setPriority(presence.getPriority());

		observable.fireEvent(event);
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void sendInitialPresence() throws XMLException {
		PresenceEvent event = new PresenceEvent(BEFORE_INITIAL_PRESENCE);
		observable.fireEvent(event);

		if (event.isCancelled())
			return;

		Presence presence = Presence.create();
		presence.setPriority(event.getPriority());
		presence.setStatus(event.getStatus());
		presence.setShow(event.getShow());

		writer.write(presence);
	}

	public void setPresence(Show show, String status, Integer priority) throws XMLException {
		Presence presence = Presence.create();
		presence.setShow(show);
		presence.setStatus(status);
		presence.setPriority(priority);

		writer.write(presence);
	}

}
