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
package tigase.jaxmpp.core.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.SessionEstablishmentModule;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.adhoc.AdHocCommansModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.NonSaslAuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageCarbonsModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.StreamResumedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.UnacknowledgedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.vcard.VCardModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 * Base abstract class for implementation platform-specific jaxmpp clients.
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>{@link JaxmppCore#Connected Connected}</b> : {@link JaxmppEvent
 * JaxmppEvent} ()<br>
 * <div>Fires when client is fully connected.</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link JaxmppCore#Disconnected Disconnected}</b> : {@link JaxmppEvent
 * JaxmppEvent} ()<br>
 * <div>Fires when client disconnects.</div>
 * <ul>
 * </ul></dd>
 * 
 * </dd>
 * 
 */
public abstract class JaxmppCore {

	public static class JaxmppEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Throwable caught;

		public JaxmppEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Throwable getCaught() {
			return caught;
		}

		public void setCaught(Throwable throwable) {
			this.caught = throwable;
		}
	}

	public static final String AUTOADD_STANZA_ID_KEY = "AUTOADD_STANZA_ID_KEY";

	public static final EventType Connected = new EventType();

	public static final EventType Disconnected = new EventType();

	private StreamManagementModule ackModule;

	protected Connector connector;

	protected final Logger log;

	protected final XmppModulesManager modulesManager;

	protected final Observable observable;

	protected final EventBus eventBus;

	protected Processor processor;

	protected final Listener<ResourceBindEvent> resourceBindListener;

	protected XmppSessionLogic sessionLogic;

	protected final AbstractSessionObject sessionObject;

	protected final Listener<ConnectorEvent> stanzaReceivedListener;

	protected final Listener<ConnectorEvent> streamErrorListener;

	protected Listener<StreamResumedEvent> streamResumedListener;

	protected final Listener<ConnectorEvent> streamTerminateListener;

	protected final Listener<UnacknowledgedEvent> unacknowledgedListener;

	protected final PacketWriter writer = new PacketWriter() {

		@Override
		public void write(final Element stanza) throws JaxmppException {
			if (connector.getState() != Connector.State.connected)
				throw new JaxmppException("Not connected!");
			try {
				if (stanza != null && log.isLoggable(Level.FINEST)) {
					log.finest("SENT: " + stanza.getAsString());
				}

				final Boolean autoId = sessionObject.getProperty(AUTOADD_STANZA_ID_KEY);

				if (autoId != null && autoId.booleanValue() && !stanza.getAttributes().containsKey("id")) {
					stanza.setAttribute("id", UIDGenerator.next());
				}

				connector.send(stanza);
			} catch (XMLException e) {
				throw new JaxmppException(e);
			}
		}

		@Override
		public void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException {
			sessionObject.registerResponseHandler(stanza, null, asyncCallback);
			writer.write(stanza);
		}

		@Override
		public void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
			sessionObject.registerResponseHandler(stanza, timeout, asyncCallback);
			writer.write(stanza);
		}

	};

	private Context jaxmppContext;

	protected EventBus createEventBus() {
		return new DefaultEventBus();
	}

	public JaxmppCore(SessionObject sessionObject) {
		this.eventBus = createEventBus();
		this.sessionObject = (AbstractSessionObject) sessionObject;
		this.log = Logger.getLogger(this.getClass().getName());
		observable = ObservableFactory.instance(null);

		this.jaxmppContext = new Context() {

			@Override
			public PacketWriter getWriter() {
				return JaxmppCore.this.writer;
			}

			@Override
			public SessionObject getSessionObject() {
				return JaxmppCore.this.sessionObject;
			}

			@Override
			public EventBus getEventBus() {
				return JaxmppCore.this.eventBus;
			}
		};

		modulesManager = new XmppModulesManager(observable, writer);

		this.streamResumedListener = new Listener<StreamResumedEvent>() {

			@Override
			public void handleEvent(StreamResumedEvent be) throws JaxmppException {
				onStreamResumed(be);
			}
		};
		this.resourceBindListener = new Listener<ResourceBindEvent>() {

			@Override
			public void handleEvent(ResourceBindEvent be) throws JaxmppException {
				onResourceBinded(be);
			}
		};
		this.streamTerminateListener = new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) throws JaxmppException {
				onStreamTerminated(be);
			}
		};
		this.streamErrorListener = new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) throws JaxmppException {
				onStreamError(be);
			}
		};
		this.stanzaReceivedListener = new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) throws JaxmppException {
				if (be.getStanza() != null)
					onStanzaReceived(be.getStanza());
			}
		};
		this.unacknowledgedListener = new Listener<UnacknowledgedEvent>() {

			@Override
			public void handleEvent(UnacknowledgedEvent be) throws JaxmppException {
				for (Element e : be.getElements()) {
					if (e == null)
						continue;
					String to = e.getAttribute("to");
					String from = e.getAttribute("from");

					e.setAttribute("type", "error");
					e.setAttribute("to", from);
					e.setAttribute("from", to);

					Element error = new DefaultElement("error");
					error.setAttribute("type", "wait");
					error.addChild(new DefaultElement("recipient-unavailable", null, "urn:ietf:params:xml:ns:xmpp-stanzas"));

					e.addChild(error);

					final Runnable r = processor.process(e);
					if (r != null)
						execute(r);
				}
			}
		};
	}

	public void addListener(EventType eventType, Listener<?> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<?> listener) {
		observable.addListener(listener);
	}

	public Chat createChat(JID jid) throws JaxmppException {
		return (this.modulesManager.getModule(MessageModule.class)).createChat(jid);
	}

	public abstract void disconnect() throws JaxmppException;

	public abstract void execute(Runnable runnable);

	/**
	 * Returns configurator.
	 * 
	 * This wrapper for SessionObject.
	 * 
	 * @return configuration
	 */
	public abstract <T extends ConnectionConfiguration> T getConnectionConfiguration();

	public Connector getConnector() {
		return connector;
	}

	/**
	 * Return module implementation by module class. This method calls
	 * {@linkplain XmppModulesManager#getModule(Class)}.
	 * 
	 * @param moduleClass
	 *            module class
	 * @return module implementation
	 */
	public <T extends XmppModule> T getModule(Class<T> moduleClass) {
		return modulesManager.getModule(moduleClass);
	}

	public XmppModulesManager getModulesManager() {
		return modulesManager;
	}

	public PresenceStore getPresence() {
		return this.sessionObject.getPresence();
	}

	public UserProperties getProperties() {
		return sessionObject;
	}

	public RosterStore getRoster() {
		return sessionObject.getRoster();
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public boolean isConnected() {
		return this.connector != null && this.connector.getState() == State.connected
				&& this.sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID) != null;
	}

	public boolean isSecure() {
		return connector.isSecure();
	}

	/**
	 * Whitespace ping.
	 * 
	 * @throws JaxmppException
	 */
	public void keepalive() throws JaxmppException {
		if (sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID) != null)
			this.connector.keepalive();
	}

	/**
	 * Connects to server.
	 */
	public abstract void login() throws JaxmppException;

	protected void modulesInit() {
		this.ackModule = this.modulesManager.register(new StreamManagementModule(this, sessionObject, writer));
		ackModule.addListener(StreamManagementModule.Unacknowledged, this.unacknowledgedListener);

		final AuthModule authModule = this.modulesManager.register(new AuthModule(observable, this.sessionObject,
				this.modulesManager));

		this.modulesManager.register(new PubSubModule(observable, sessionObject, writer));

		this.modulesManager.register(new MucModule(observable, sessionObject, writer));

		this.modulesManager.register(new PresenceModule(observable, sessionObject, writer));

		MessageModule messageModule = new MessageModule(observable, sessionObject, writer);

		this.modulesManager.register(new MessageCarbonsModule(sessionObject, messageModule, writer));
		this.modulesManager.register(messageModule);

		final DiscoInfoModule discoInfoModule = this.modulesManager.register(new DiscoInfoModule(sessionObject, writer,
				modulesManager));
		final DiscoItemsModule discoItemsModule = this.modulesManager.register(new DiscoItemsModule(sessionObject, writer));

		this.modulesManager.register(new AdHocCommansModule(sessionObject, writer, discoItemsModule, discoInfoModule));

		this.modulesManager.register(new SoftwareVersionModule(sessionObject, writer));
		this.modulesManager.register(new PingModule(sessionObject, writer));
		this.modulesManager.register(new ResourceBinderModule(observable, sessionObject, writer));

		this.modulesManager.register(new RosterModule(sessionObject, writer));

		this.modulesManager.register(new StreamFeaturesModule(observable, sessionObject, writer));
		this.modulesManager.register(new SaslModule(authModule.getObservable(), sessionObject, writer));
		NonSaslAuthModule nonSasl = new NonSaslAuthModule(sessionObject, writer);
		this.modulesManager.register(nonSasl);
		nonSasl.setObservable(authModule.getObservable());

		this.modulesManager.register(new VCardModule(observable, sessionObject, writer));
		this.modulesManager.register(new InBandRegistrationModule(sessionObject, writer));

		this.modulesManager.register(new SessionEstablishmentModule(observable, sessionObject, writer));

		this.modulesManager.init();
	}

	protected abstract void onException(JaxmppException e) throws JaxmppException;

	protected abstract void onResourceBinded(ResourceBindEvent be) throws JaxmppException;

	protected void onStanzaReceived(Element stanza) {
		final Runnable r = this.processor.process(stanza);
		try {
			if (ackModule.processIncomingStanza(stanza))
				return;
		} catch (XMLException e) {
			log.log(Level.WARNING, "Problem on counting", e);
		}
		execute(r);
	}

	protected abstract void onStreamError(ConnectorEvent be) throws JaxmppException;

	protected abstract void onStreamResumed(StreamResumedEvent be) throws JaxmppException;

	protected abstract void onStreamTerminated(ConnectorEvent be) throws JaxmppException;

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> connectorListener) {
		observable.removeListener(eventType, connectorListener);
	}

	public void removeListener(Listener<?> listener) {
		observable.removeListener(listener);
	}

	public void send(Stanza stanza) throws XMLException, JaxmppException {
		this.writer.write(stanza);
	}

	public void send(Stanza stanza, AsyncCallback asyncCallback) throws XMLException, JaxmppException {
		this.writer.write(stanza, asyncCallback);
	}

	public void send(Stanza stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		this.writer.write(stanza, timeout, asyncCallback);
	}

	public void sendMessage(JID toJID, String subject, String message) throws XMLException, JaxmppException {
		(this.modulesManager.getModule(MessageModule.class)).sendMessage(toJID, subject, message);
	}

	public Context getContext() {
		return jaxmppContext;
	}

}