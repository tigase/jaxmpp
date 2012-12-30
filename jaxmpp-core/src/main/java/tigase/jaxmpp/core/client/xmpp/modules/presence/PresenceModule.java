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
package tigase.jaxmpp.core.client.xmpp.modules.presence;

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
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore.Handler;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Module for handling presence information.
 */
public class PresenceModule extends AbstractStanzaModule<Presence> {

	/**
	 * Presence related event.
	 */
	public static class PresenceEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private boolean cancelled;

		private JID jid;

		private Presence presence;

		private Integer priority;

		private Show show;

		private String status;

		public PresenceEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		/**
		 * Cancel processing event.
		 */
		public void cancel() {
			this.cancelled = true;
		}

		/**
		 * Returns JID of entity related to presence event.
		 * 
		 * @return
		 */
		public JID getJid() {
			return jid;
		}

		/**
		 * Returns Presence stanza.
		 * 
		 * @return presence stanza.
		 */
		public Presence getPresence() {
			return presence;
		}

		/**
		 * Returns priority information received with presence.
		 * 
		 * @return priority or <code>null</code> if not present.
		 */
		public Integer getPriority() {
			return priority;
		}

		/**
		 * Returns substate of presence.
		 * 
		 * @return presence sub-state.
		 */
		public Show getShow() {
			return show;
		}

		/**
		 * Returns human readable presence description.
		 * 
		 * @return
		 */
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

	/**
	 * Event fired before initial presence is sent.
	 */
	public static final EventType BeforeInitialPresence = new EventType();

	/**
	 * Event fired before each presence sent by client.
	 */
	public static final EventType BeforePresenceSend = new EventType();

	/**
	 * Event fired when contact is available.
	 */
	public static final EventType ContactAvailable = new EventType();

	/**
	 * Event fired when contact changed his presence.
	 */
	public static final EventType ContactChangedPresence = new EventType();

	/**
	 * Event fired when contact goes offline.
	 */
	public static final EventType ContactUnavailable = new EventType();

	/**
	 * Event fired when contact is unsubscribed.
	 */
	public static final EventType ContactUnsubscribed = new EventType();

	public static final Criteria CRIT = ElementCriteria.name("presence");

	/**
	 * Event fired when subscription request is received.
	 */
	public static final EventType SubscribeRequest = new EventType();

	public PresenceModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(ObservableFactory.instance(parentObservable), sessionObject, packetWriter);
		this.sessionObject.getPresence().setHandler(new Handler() {

			@Override
			public void onOffline(Presence i) throws JaxmppException {
				contactOffline(i.getFrom());
			}

			@Override
			public void setPresence(Show show, String status, Integer priority) throws XMLException, JaxmppException {
				PresenceModule.this.setPresence(show, status, priority);
			}
		});
	}

	protected void contactOffline(final JID jid) throws JaxmppException {
		PresenceEvent event = new PresenceEvent(ContactUnavailable, sessionObject);
		event.setJid(jid);

		observable.fireEvent(event);

	}

	/**
	 * 
	 * @param presence
	 * @return <code>true</code> if event is cancelled
	 * @throws JaxmppException
	 */
	protected boolean fireBeforePresenceSend(final Presence presence) throws JaxmppException {
		PresenceEvent event = new PresenceEvent(BeforePresenceSend, sessionObject);
		event.setPresence(presence);

		observable.fireEvent(BeforePresenceSend, event);

		return event.isCancelled();
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
	public void process(final Presence presence) throws JaxmppException {
		final JID fromJid = presence.getFrom();
		log.finest("Presence received from " + fromJid + " :: " + presence.getAsString());
		if (fromJid == null)
			return;

		boolean availableOld = sessionObject.getPresence().isAvailable(fromJid.getBareJid());
		sessionObject.getPresence().update(presence);
		boolean availableNow = sessionObject.getPresence().isAvailable(fromJid.getBareJid());

		final StanzaType type = presence.getType();

		PresenceEvent event;
		if (type == StanzaType.unsubscribed) {
			event = new PresenceEvent(ContactUnsubscribed, sessionObject);
		} else if (type == StanzaType.subscribe) {
			// subscribe
			log.finer("Subscribe from " + fromJid);
			event = new PresenceEvent(SubscribeRequest, sessionObject);
		} else if (!availableOld && availableNow) {
			// sontact available
			log.finer("Presence online from " + fromJid);
			event = new PresenceEvent(ContactAvailable, sessionObject);
		} else if (availableOld && !availableNow) {
			// contact unavailable
			log.finer("Presence offline from " + fromJid);
			event = new PresenceEvent(ContactUnavailable, sessionObject);
		} else {
			log.finer("Presence change from " + fromJid);
			event = new PresenceEvent(ContactChangedPresence, sessionObject);
		}
		event.setPresence(presence);
		event.setJid(fromJid);
		event.setShow(presence.getShow());
		event.setStatus(presence.getStatus());
		event.setPriority(presence.getPriority());

		observable.fireEvent(event);
	}

	public void sendInitialPresence() throws XMLException, JaxmppException {
		PresenceEvent event = new PresenceEvent(BeforeInitialPresence, sessionObject);
		observable.fireEvent(event);

		if (event.isCancelled())
			return;

		Presence presence = Presence.create();
		presence.setPriority(event.getPriority());
		presence.setStatus(event.getStatus());
		presence.setShow(event.getShow());
		if (sessionObject.getProperty(SessionObject.NICKNAME) != null) {
			presence.setNickname((String) sessionObject.getProperty(SessionObject.NICKNAME));
		}

		if (fireBeforePresenceSend(presence))
			return;

		writer.write(presence);
	}

	/**
	 * Sends own presence.
	 * 
	 * @param show
	 *            presence substate.
	 * @param status
	 *            human readable description of status.
	 * @param priority
	 *            priority.
	 */
	public void setPresence(Show show, String status, Integer priority) throws XMLException, JaxmppException {
		Presence presence = Presence.create();
		presence.setShow(show);
		presence.setStatus(status);
		presence.setPriority(priority);
		if (sessionObject.getProperty(SessionObject.NICKNAME) != null) {
			presence.setNickname((String) sessionObject.getProperty(SessionObject.NICKNAME));
		}
		if (fireBeforePresenceSend(presence))
			return;

		writer.write(presence);
	}

	/**
	 * Subscribe for presence.
	 * 
	 * @param jid
	 *            JID
	 */
	public void subscribe(JID jid) throws JaxmppException, XMLException {
		Presence p = Presence.create();
		p.setType(StanzaType.subscribe);
		p.setTo(jid);

		if (fireBeforePresenceSend(p))
			return;

		writer.write(p);
	}

	public void subscribed(JID jid) throws JaxmppException, XMLException {
		Presence p = Presence.create();
		p.setType(StanzaType.subscribed);
		p.setTo(jid);

		if (fireBeforePresenceSend(p))
			return;

		writer.write(p);
	}

	public void unsubscribe(JID jid) throws JaxmppException, XMLException {
		Presence p = Presence.create();
		p.setType(StanzaType.unsubscribe);
		p.setTo(jid);

		if (fireBeforePresenceSend(p))
			return;

		writer.write(p);
	}

	public void unsubscribed(JID jid) throws JaxmppException, XMLException {
		Presence p = Presence.create();
		p.setType(StanzaType.unsubscribed);
		p.setTo(jid);

		if (fireBeforePresenceSend(p))
			return;

		writer.write(p);
	}

}