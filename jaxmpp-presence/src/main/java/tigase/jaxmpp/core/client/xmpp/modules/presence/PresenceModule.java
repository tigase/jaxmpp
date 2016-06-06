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

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppSessionLogic.XmppSessionEstablishedHandler;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.InitializingModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.BeforePresenceSendHandler.BeforePresenceSendEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactAvailableHandler.ContactAvailableEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactChangedPresenceHandler.ContactChangedPresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactUnavailableHandler.ContactUnavailableEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactUnsubscribedHandler.ContactUnsubscribedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.SubscribeRequestHandler.SubscribeRequestEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore.Handler;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.Set;
import java.util.logging.Level;

/**
 * Module for handling presence information.
 */
public class PresenceModule extends AbstractStanzaModule<Presence> implements InitializingModule, ContextAware, XmppSessionEstablishedHandler {

	public static final Criteria CRIT = ElementCriteria.name("presence");

	public static final String PRESENCE_STORE_KEY = "PresenceModule#PRESENCE_STORE";

	public static final String INITIAL_PRESENCE_ENABLED_KEY = "PresenceModule#INITIAL_PRESENCE_ENABLED";

	public static final String OWN_PRESENCE_STANZA_FACTORY_KEY = "PresenceModule#OWN_PRESENCE_STANZA_FACTORY";

	public PresenceModule() {
		super();
	}

	public static PresenceStore getPresenceStore(SessionObject sessionObject) {
		return sessionObject.getProperty(PRESENCE_STORE_KEY);
	}

	public static void setOwnPresenceStanzaFactory(SessionObject sessionObject, OwnPresenceStanzaFactory factory) {
		sessionObject.setProperty(Scope.user, OWN_PRESENCE_STANZA_FACTORY_KEY, factory);
	}

	public static void setPresenceStore(SessionObject sessionObject, PresenceStore presenceStore) {
		sessionObject.setProperty(Scope.user, PRESENCE_STORE_KEY, presenceStore);
	}

	public void addBeforePresenceSendHandler(BeforePresenceSendHandler handler) {
		context.getEventBus().addHandler(BeforePresenceSendHandler.BeforePresenceSendEvent.class, handler);
	}

	public void addContactAvailableHandler(ContactAvailableHandler handler) {
		context.getEventBus().addHandler(ContactAvailableHandler.ContactAvailableEvent.class, handler);
	}

	public void addContactChangedPresenceHandler(ContactChangedPresenceHandler handler) {
		context.getEventBus().addHandler(ContactChangedPresenceHandler.ContactChangedPresenceEvent.class, handler);
	}

	public void addContactUnavailableHandler(ContactUnavailableHandler handler) {
		context.getEventBus().addHandler(ContactUnavailableHandler.ContactUnavailableEvent.class, handler);
	}

	public void addContactUnsubscribedHandler(ContactUnsubscribedHandler handler) {
		context.getEventBus().addHandler(ContactUnsubscribedHandler.ContactUnsubscribedEvent.class, handler);
	}

	public void addSubscribeRequestHandler(SubscribeRequestHandler handler) {
		context.getEventBus().addHandler(SubscribeRequestHandler.SubscribeRequestEvent.class, handler);
	}

	@Override
	public void afterRegister() {
	}

	@Override
	public void beforeRegister() {
		super.beforeRegister();

		PresenceStore presenceStore = getPresenceStore();

		if (presenceStore == null) throw new RuntimeException("PresenceStore is not created!");

		presenceStore.setHandler(new Handler() {

			@Override
			public void onOffline(Presence i) throws JaxmppException {
				contactOffline(i, i.getFrom());
			}

			@Override
			public void setPresence(Show show, String status, Integer priority) throws JaxmppException {
				PresenceModule.this.setPresence(show, status, priority);
			}
		});
		context.getEventBus()
			   .addHandler(AbstractSessionObject.ClearedHandler.ClearedEvent.class,
						   new AbstractSessionObject.ClearedHandler() {

							   @Override
							   public void onCleared(SessionObject sessionObject,
													 Set<Scope> scopes) throws JaxmppException {
								   if (scopes.contains(Scope.session)) {
									   getPresenceStore().clear();
								   }
							   }
						   });
		context.getEventBus().addHandler(XmppSessionEstablishedHandler.XmppSessionEstablishedEvent.class, this);
	}

	@Override
	public void beforeUnregister() {
		// TODO Auto-generated method stub

	}

	protected void contactOffline(Presence i, final JID jid) throws JaxmppException {
		fireEvent(new ContactUnavailableEvent(context.getSessionObject(), i, jid, null));
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public PresenceStore getPresenceStore() {
		return getPresenceStore(context.getSessionObject());
	}

	@Override
	public void onXmppSessionEstablished(SessionObject sessionObject) throws JaxmppException {
		Boolean initial_presence_enabled = sessionObject.getProperty(INITIAL_PRESENCE_ENABLED_KEY);
		if (initial_presence_enabled == null) {
			sendInitialPresence();
		} else if (initial_presence_enabled) {
			sendInitialPresence();
		} else if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Skipping sending initial presence");
		}
	}

	@Override
	public void process(final Presence presence) throws JaxmppException {
		final JID fromJid = presence.getFrom();
		log.finest("Presence received from " + fromJid + " :: " + presence.getAsString());
		if (fromJid == null) return;

		boolean availableOld = getPresenceStore().isAvailable(fromJid.getBareJid());
		getPresenceStore().update(presence);
		boolean availableNow = getPresenceStore().isAvailable(fromJid.getBareJid());

		final StanzaType type = presence.getType();

		if (type == StanzaType.unsubscribed) {
			fireEvent(new ContactUnsubscribedEvent(context.getSessionObject(),
												   presence,
												   presence.getFrom().getBareJid()));
		} else if (type == StanzaType.subscribe) {
			// subscribe
			log.finer("Subscribe from " + fromJid);
			fireEvent(new SubscribeRequestEvent(context.getSessionObject(), presence, presence.getFrom().getBareJid()));
		} else if (!availableOld && availableNow) {
			// sontact available
			log.finer("Presence online from " + fromJid);
			fireEvent(new ContactChangedPresenceEvent(context.getSessionObject(),
													  presence,
													  presence.getFrom(),
													  presence.getShow(),
													  presence.getStatus(),
													  presence.getPriority()));
			fireEvent(new ContactAvailableEvent(context.getSessionObject(),
												presence,
												presence.getFrom(),
												presence.getShow(),
												presence.getStatus(),
												presence.getPriority()));
		} else if (availableOld && !availableNow) {
			// contact unavailable
			log.finer("Presence offline from " + fromJid);
			fireEvent(new ContactChangedPresenceEvent(context.getSessionObject(),
													  presence,
													  presence.getFrom(),
													  presence.getShow(),
													  presence.getStatus(),
													  presence.getPriority()));
			fireEvent(new ContactUnavailableEvent(context.getSessionObject(),
												  presence,
												  presence.getFrom(),
												  presence.getStatus()));
		} else {
			log.finer("Presence change from " + fromJid);
			fireEvent(new ContactChangedPresenceEvent(context.getSessionObject(),
													  presence,
													  presence.getFrom(),
													  presence.getShow(),
													  presence.getStatus(),
													  presence.getPriority()));
		}
	}

	public void removeBeforePresenceSendHandler(BeforePresenceSendHandler handler) {
		context.getEventBus().remove(BeforePresenceSendHandler.BeforePresenceSendEvent.class, handler);
	}

	public void removeContactAvailableHandler(ContactAvailableHandler handler) {
		context.getEventBus().remove(ContactAvailableHandler.ContactAvailableEvent.class, handler);
	}

	public void removeContactChangedPresenceHandler(ContactChangedPresenceHandler handler) {
		context.getEventBus().remove(ContactChangedPresenceHandler.ContactChangedPresenceEvent.class, handler);
	}

	public void removeContactUnavailableHandler(ContactUnavailableHandler handler) {
		context.getEventBus().remove(ContactUnavailableHandler.ContactUnavailableEvent.class, handler);
	}

	public void removeContactUnsubscribedHandler(ContactUnsubscribedHandler handler) {
		context.getEventBus().remove(ContactUnsubscribedHandler.ContactUnsubscribedEvent.class, handler);
	}

	public void removeSubscribeRequestHandler(SubscribeRequestHandler handler) {
		context.getEventBus().remove(SubscribeRequestHandler.SubscribeRequestEvent.class, handler);
	}

	public void sendInitialPresence() throws JaxmppException {
		OwnPresenceStanzaFactory factory = context.getSessionObject().getProperty(OWN_PRESENCE_STANZA_FACTORY_KEY);
		Presence presence = factory == null ? Presence.create() : factory.create(context.getSessionObject());

		if (context.getSessionObject().getProperty(SessionObject.NICKNAME) != null) {
			presence.setNickname((String) context.getSessionObject().getProperty(SessionObject.NICKNAME));
		}

		fireEvent(new BeforePresenceSendEvent(context.getSessionObject(), presence));

		write(presence);
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	public void setInitialPresence(boolean enabled) {
		context.getSessionObject().setProperty(INITIAL_PRESENCE_ENABLED_KEY, enabled);
	}

	/**
	 * Sends own presence.
	 *
	 * @param show     presence substate.
	 * @param status   human readable description of status.
	 * @param priority priority.
	 */
	public void setPresence(Show show, String status, Integer priority) throws JaxmppException {
		OwnPresenceStanzaFactory factory = context.getSessionObject().getProperty(OWN_PRESENCE_STANZA_FACTORY_KEY);
		Presence presence = factory == null ? Presence.create() : factory.create(context.getSessionObject());

		presence.setShow(show);
		presence.setStatus(status);
		presence.setPriority(priority);
		if (context.getSessionObject().getProperty(SessionObject.NICKNAME) != null) {
			presence.setNickname((String) context.getSessionObject().getProperty(SessionObject.NICKNAME));
		}

		fireEvent(new BeforePresenceSendEvent(context.getSessionObject(), presence));

		write(presence);
	}

	/**
	 * Subscribe for presence.
	 *
	 * @param jid JID
	 */
	public void subscribe(JID jid) throws JaxmppException {
		Presence p = Presence.create();
		p.setType(StanzaType.subscribe);
		p.setTo(jid);

		write(p);
	}

	public void subscribed(JID jid) throws JaxmppException {
		Presence p = Presence.create();
		p.setType(StanzaType.subscribed);
		p.setTo(jid);

		write(p);
	}

	public void unsubscribe(JID jid) throws JaxmppException {
		Presence p = Presence.create();
		p.setType(StanzaType.unsubscribe);
		p.setTo(jid);

		write(p);
	}

	public void unsubscribed(JID jid) throws JaxmppException {
		Presence p = Presence.create();
		p.setType(StanzaType.unsubscribed);
		p.setTo(jid);

		write(p);
	}

	/**
	 * Event fired before each presence sent by client.
	 */
	public interface BeforePresenceSendHandler extends EventHandler {

		void onBeforePresenceSend(SessionObject sessionObject, Presence presence) throws JaxmppException;

		class BeforePresenceSendEvent extends JaxmppEvent<BeforePresenceSendHandler> {

			private Presence presence;

			public BeforePresenceSendEvent(SessionObject sessionObject, Presence presence) {
				super(sessionObject);
				this.presence = presence;
			}

			@Override
			public void dispatch(BeforePresenceSendHandler handler) throws JaxmppException {
				handler.onBeforePresenceSend(sessionObject, presence);
			}

			public Presence getPresence() {
				return presence;
			}

			public void setPresence(Presence presence) {
				this.presence = presence;
			}

		}
	}

	/**
	 * Event fired when contact (understood as bare JID) becomes available. Fired when first resource of JID becomes
	 * available.
	 */
	public interface ContactAvailableHandler extends EventHandler {

		void onContactAvailable(SessionObject sessionObject, Presence stanza, JID jid, Show show, String status,
								Integer priority) throws JaxmppException;

		class ContactAvailableEvent extends JaxmppEvent<ContactAvailableHandler> {

			private JID jid;

			private Integer priority;

			private Show show;

			private Presence stanza;

			private String status;

			public ContactAvailableEvent(SessionObject sessionObject,
										 Presence presence,
										 JID jid,
										 Show show,
										 String statusMessage,
										 Integer priority) {
				super(sessionObject);
				this.stanza = presence;
				this.jid = jid;
				this.show = show;
				this.status = statusMessage;
				this.priority = priority;
			}

			@Override
			public void dispatch(ContactAvailableHandler handler) throws JaxmppException {
				handler.onContactAvailable(sessionObject, stanza, jid, show, status, priority);
			}

			public JID getJid() {
				return jid;
			}

			public void setJid(JID jid) {
				this.jid = jid;
			}

			public Integer getPriority() {
				return priority;
			}

			public void setPriority(Integer priority) {
				this.priority = priority;
			}

			public Show getShow() {
				return show;
			}

			public void setShow(Show show) {
				this.show = show;
			}

			public Presence getStanza() {
				return stanza;
			}

			public void setStanza(Presence stanza) {
				this.stanza = stanza;
			}

			public String getStatus() {
				return status;
			}

			public void setStatus(String status) {
				this.status = status;
			}

		}
	}


	/**
	 * Event fired when contact changed his presence.
	 */
	public interface ContactChangedPresenceHandler extends EventHandler {

		void onContactChangedPresence(SessionObject sessionObject, Presence stanza, JID jid, Show show, String status,
									  Integer priority) throws JaxmppException;

		class ContactChangedPresenceEvent extends JaxmppEvent<ContactChangedPresenceHandler> {

			private JID jid;

			private Integer priority;

			private Show show;

			private Presence stanza;

			private String status;

			public ContactChangedPresenceEvent(SessionObject sessionObject,
											   Presence presence,
											   JID jid,
											   Show show,
											   String statusMessage,
											   Integer priority) {
				super(sessionObject);
				this.stanza = presence;
				this.jid = jid;
				this.show = show;
				this.status = statusMessage;
				this.priority = priority;
			}

			@Override
			public void dispatch(ContactChangedPresenceHandler handler) throws JaxmppException {
				handler.onContactChangedPresence(sessionObject, stanza, jid, show, status, priority);
			}

			public JID getJid() {
				return jid;
			}

			public void setJid(JID jid) {
				this.jid = jid;
			}

			public Integer getPriority() {
				return priority;
			}

			public void setPriority(Integer priority) {
				this.priority = priority;
			}

			public Show getShow() {
				return show;
			}

			public void setShow(Show show) {
				this.show = show;
			}

			public Presence getStanza() {
				return stanza;
			}

			public void setStanza(Presence stanza) {
				this.stanza = stanza;
			}

			public String getStatus() {
				return status;
			}

			public void setStatus(String status) {
				this.status = status;
			}

		}
	}

	/**
	 * Event fired when contact (understood as bare JID) goes offline. Fired when no more resources are available.
	 */
	public interface ContactUnavailableHandler extends EventHandler {

		void onContactUnavailable(SessionObject sessionObject, Presence stanza, JID jid, String status);

		class ContactUnavailableEvent extends JaxmppEvent<ContactUnavailableHandler> {

			private JID jid;

			private Presence stanza;

			private String status;

			public ContactUnavailableEvent(SessionObject sessionObject,
										   Presence presence,
										   JID jid,
										   String statusMessage) {
				super(sessionObject);
				this.stanza = presence;
				this.jid = jid;
				this.status = statusMessage;
			}

			@Override
			public void dispatch(ContactUnavailableHandler handler) {
				handler.onContactUnavailable(sessionObject, stanza, jid, status);
			}

			public JID getJid() {
				return jid;
			}

			public void setJid(JID jid) {
				this.jid = jid;
			}

			public Presence getStanza() {
				return stanza;
			}

			public void setStanza(Presence stanza) {
				this.stanza = stanza;
			}

			public String getStatus() {
				return status;
			}

			public void setStatus(String status) {
				this.status = status;
			}

		}
	}

	/**
	 * Event fired when contact is unsubscribed.
	 */
	public interface ContactUnsubscribedHandler extends EventHandler {

		void onContactUnsubscribed(SessionObject sessionObject, Presence stanza, BareJID jid);

		class ContactUnsubscribedEvent extends JaxmppEvent<ContactUnsubscribedHandler> {

			private BareJID jid;

			private Presence stanza;

			public ContactUnsubscribedEvent(SessionObject sessionObject, Presence presence, BareJID bareJID) {
				super(sessionObject);
				this.stanza = presence;
				this.jid = bareJID;
			}

			@Override
			public void dispatch(ContactUnsubscribedHandler handler) {
				handler.onContactUnsubscribed(sessionObject, stanza, jid);
			}

			public BareJID getJid() {
				return jid;
			}

			public void setJid(BareJID jid) {
				this.jid = jid;
			}

			public Presence getStanza() {
				return stanza;
			}

			public void setStanza(Presence stanza) {
				this.stanza = stanza;
			}

		}
	}

	public interface OwnPresenceStanzaFactory {

		Presence create(SessionObject sessionObject);
	}

	/**
	 * Event fired when subscription request is received.
	 */
	public interface SubscribeRequestHandler extends EventHandler {

		void onSubscribeRequest(SessionObject sessionObject, Presence stanza, BareJID jid);

		class SubscribeRequestEvent extends JaxmppEvent<SubscribeRequestHandler> {

			private BareJID jid;

			private Presence stanza;

			public SubscribeRequestEvent(SessionObject sessionObject, Presence presence, BareJID bareJID) {
				super(sessionObject);
				this.stanza = presence;
				this.jid = bareJID;
			}

			@Override
			public void dispatch(SubscribeRequestHandler handler) {
				handler.onSubscribeRequest(sessionObject, stanza, jid);
			}

			public BareJID getJid() {
				return jid;
			}

			public void setJid(BareJID jid) {
				this.jid = jid;
			}

			public Presence getStanza() {
				return stanza;
			}

			public void setStanza(Presence stanza) {
				this.stanza = stanza;
			}

		}
	}

}