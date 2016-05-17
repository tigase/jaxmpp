/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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

import tigase.jaxmpp.core.client.Connector.ErrorHandler;
import tigase.jaxmpp.core.client.Connector.StanzaReceivedHandler;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.Connector.StreamTerminatedHandler;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.DefaultXMPPStream;
import tigase.jaxmpp.core.client.xmpp.modules.*;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindSuccessHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.NonSaslAuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.StreamResumedHandler;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.UnacknowledgedHandler;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StreamPacket;
import tigase.jaxmpp.core.client.xmpp.stream.XmppStreamsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
//import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;

/**
 * Base abstract class for implementation platform-specific jaxmpp clients.
 *
 *
 */
public abstract class JaxmppCore {
	public static final String AUTOADD_STANZA_ID_KEY = "AUTOADD_STANZA_ID_KEY";
	protected final Logger log;
	protected Connector connector;
	protected Context context;
	protected DefaultXMPPStream defaultXMPPStream = new DefaultXMPPStream() {

		@Override
		public void write(Element stanza) throws JaxmppException {
			connector.send(stanza);
		}
	};
	protected EventBus eventBus;
	protected XmppModulesManager modulesManager;
	protected Processor processor;
	protected Map<Class<Property>, Property> properties = new HashMap<Class<Property>, Property>();
	protected XmppSessionLogic sessionLogic;
	protected SessionObject sessionObject;
	protected XmppStreamsManager streamsManager;
	protected PacketWriter writer = new PacketWriter() {

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

				context.getStreamsManager().writeToStream(stanza);

			} catch (XMLException e) {
				throw new JaxmppException(e);
			}
		}

		@Override
		public void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException {
			ResponseManager.registerResponseHandler(sessionObject, stanza, null, asyncCallback);
			writer.write(stanza);
		}

		@Override
		public void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
			ResponseManager.registerResponseHandler(sessionObject, stanza, timeout, asyncCallback);
			writer.write(stanza);
		}

	};
	private StreamManagementModule ackModule;

	public JaxmppCore() {
		this.log = Logger.getLogger(this.getClass().getName());
	}

	protected EventBus createEventBus() {
		return new DefaultEventBus();
	}

	// public Chat createChat(JID jid) throws JaxmppException {
	// return
	// (this.modulesManager.getModule(MessageModule.class)).createChat(jid);
	// }

	/**
	 * Closes XMPP session.
	 */
	public abstract void disconnect() throws JaxmppException;

	/**
	 * Executes task in executor. Used to handle received stanzas.
	 *
	 * @param runnable
	 *            task to execute.
	 */
	public abstract void execute(Runnable runnable);

	public <T extends Property> T get(Class<T> property) {
		return (T) properties.get(property);
	}

	/**
	 * Returns configurator.
	 *
	 * This wrapper for SessionObject.
	 *
	 * @return configuration
	 */
	public abstract <T extends ConnectionConfiguration> T getConnectionConfiguration();

	/**
	 * Returns connector.
	 *
	 * @return {@link Connector} used by jaxmpp.
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Returns {@link Context} of this JaXMPP instance.
	 *
	 * @return {@link Context}.
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Returns {@link EventBus} of this JaXMPP instance.
	 *
	 * @return {@link EventBus}.
	 */
	public EventBus getEventBus() {
		return eventBus;
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

	/**
	 * Returns {@link XmppModulesManager ModuleManager}.
	 *
	 * @return {@link XmppModulesManager}.
	 */
	public XmppModulesManager getModulesManager() {
		return modulesManager;
	}

	/**
	 * Returns {@link UserProperties}.
	 *
	 * @return {@link UserProperties}.
	 */
	public UserProperties getProperties() {
		return sessionObject;
	}

	// /**
	// * Returns {@link PresenceStore}.
	// *
	// * @return {@link PresenceStore}.
	// */
	// public PresenceStore getPresence() {
	// return PresenceModule.getPresenceStore(sessionObject);
	// }

	/**
	 * Returns {@link SessionObject}.
	 *
	 * @return {@link SessionObject}.
	 */
	public SessionObject getSessionObject() {
		return sessionObject;
	}

	// /**
	// * Returns {@link RosterStore}.
	// *
	// * @return {@link RosterStore}.
	// */
	// public RosterStore getRoster() {
	// return RosterModule.getRosterStore(sessionObject);
	// }

	protected void init() {
		if (this.eventBus == null)
			throw new RuntimeException("EventBus cannot be null!");
		if (this.sessionObject == null)
			throw new RuntimeException("SessionObject cannot be null!");
		if (this.writer == null)
			throw new RuntimeException("PacketWriter cannot be null!");
		if (ResponseManager.getResponseManager(sessionObject) == null)
			throw new RuntimeException("ResponseManager cannot be null!");
		// if (RosterModule.getRosterStore(sessionObject) == null)
		// throw new RuntimeException("RosterModule cannot be null!");
		// if (PresenceModule.getPresenceStore(sessionObject) == null)
		// throw new RuntimeException("PresenceModule cannot be null!");

		sessionObject.setProperty(XmppStreamsManager.DEFAULT_XMPP_STREAM_KEY, defaultXMPPStream);

		if (this.sessionObject instanceof EventBusAware) {
			((EventBusAware) this.sessionObject).setEventBus(eventBus);
		}

		assert ResponseManager.getResponseManager(sessionObject) != null;
		// assert RosterModule.getRosterStore(sessionObject) != null;
		// assert PresenceModule.getPresenceStore(sessionObject) != null;

		this.context = new Context() {

			@Override
			public EventBus getEventBus() {
				return JaxmppCore.this.eventBus;
			}

			@Override
			public ModuleProvider getModuleProvider() {
				return JaxmppCore.this.modulesManager;
			}

			@Override
			public SessionObject getSessionObject() {
				return JaxmppCore.this.sessionObject;
			}

			@Override
			public XmppStreamsManager getStreamsManager() {
				return streamsManager;
			}

			@Override
			public PacketWriter getWriter() {
				return JaxmppCore.this.writer;
			}
		};

		this.streamsManager = new XmppStreamsManager();
		this.streamsManager.setContext(context);
		XmppStreamsManager.setStreamsManager(sessionObject, streamsManager);

		modulesManager = new XmppModulesManager(context);

		eventBus.addHandler(StreamResumedHandler.StreamResumedEvent.class, new StreamResumedHandler() {

			@Override
			public void onStreamResumed(SessionObject sessionObject, Long h, String previd) throws JaxmppException {
				JaxmppCore.this.onStreamResumed(h, previd);
			}
		});

		eventBus.addHandler(ResourceBindSuccessHandler.ResourceBindSuccessEvent.class, new ResourceBindSuccessHandler() {

			@Override
			public void onResourceBindSuccess(SessionObject sessionObject, JID bindedJid) throws JaxmppException {
				JaxmppCore.this.onResourceBindSuccess(bindedJid);
			}
		});

		eventBus.addHandler(StreamTerminatedHandler.StreamTerminatedEvent.class, new StreamTerminatedHandler() {

			@Override
			public void onStreamTerminated(SessionObject sessionObject) throws JaxmppException {
				JaxmppCore.this.onStreamTerminated();
			}
		});

		eventBus.addHandler(ErrorHandler.ErrorEvent.class, new ErrorHandler() {

			@Override
			public void onError(SessionObject sessionObject, StreamError condition, Throwable caught) throws JaxmppException {
				JaxmppCore.this.onStreamError(condition, caught);
			}
		});

		eventBus.addHandler(StanzaReceivedHandler.StanzaReceivedEvent.class, new StanzaReceivedHandler() {

			@Override
			public void onStanzaReceived(SessionObject sessionObject, StreamPacket stanza) {
				JaxmppCore.this.onStanzaReceived(stanza);
			}
		});

		eventBus.addHandler(UnacknowledgedHandler.UnacknowledgedEvent.class, new UnacknowledgedHandler() {

			@Override
			public void onUnacknowledged(SessionObject sessionObject, List<Element> elements) throws JaxmppException {
				JaxmppCore.this.onUnacknowledged(elements);
			}
		});

		eventBus.addHandler(Connector.DisconnectedHandler.DisconnectedEvent.class, new Connector.DisconnectedHandler() {

			@Override
			public void onDisconnected(SessionObject sessionObject) {
				JaxmppCore.this.onConnectorStopped();
			}
		});

	}

	/**
	 * Returns connection state.
	 *
	 * @return <code>true</code> if XMPP connection is established.
	 */
	public boolean isConnected() {
		return this.connector != null && this.connector.getState() == State.connected
				&& this.sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID) != null;
	}

	/**
	 * Returns connection security state.
	 *
	 * @return <code>true</code> if connection is established and secured.
	 */
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
	 * Connects to XMPP server.
	 */
	public abstract void login() throws JaxmppException;

	protected void modulesInit() {
		this.ackModule = this.modulesManager.register(new StreamManagementModule(this));

		this.modulesManager.register(new AuthModule());

		// MessageModule messageModule = new MessageModule(context);
		//
		// this.modulesManager.register(new MessageCarbonsModule(context,
		// messageModule));
		// this.modulesManager.register(messageModule);

		this.modulesManager.register(new DiscoveryModule());

		this.modulesManager.register(new SoftwareVersionModule());
		this.modulesManager.register(new PingModule());
		this.modulesManager.register(new ResourceBinderModule());

		this.modulesManager.register(new StreamFeaturesModule());
		this.modulesManager.register(new SaslModule());
		NonSaslAuthModule nonSasl = new NonSaslAuthModule();
		this.modulesManager.register(nonSasl);

		this.modulesManager.register(new SessionEstablishmentModule());

	}

	protected void onConnectorStopped() {
		eventBus.fire(new LoggedOutHandler.LoggedOutEvent(sessionObject));
	}

	protected abstract void onException(JaxmppException e) throws JaxmppException;

	protected abstract void onResourceBindSuccess(JID bindedJID) throws JaxmppException;

	protected void onStanzaReceived(StreamPacket stanza) {
		final Runnable r = this.processor.process(stanza);
		try {
			if (ackModule.processIncomingStanza(stanza))
				return;
		} catch (XMLException e) {
			log.log(Level.WARNING, "Problem on counting", e);
		}
		execute(r);
	}

	protected abstract void onStreamError(StreamError condition, Throwable caught) throws JaxmppException;

	protected abstract void onStreamResumed(Long h, String previd) throws JaxmppException;

	protected abstract void onStreamTerminated() throws JaxmppException;

	protected void onUnacknowledged(List<Element> elements) throws JaxmppException {
		for (Element e : elements) {
			if (e == null)
				continue;
			String to = e.getAttribute("to");
			String from = e.getAttribute("from");

			e.setAttribute("type", "error");
			e.setAttribute("to", from);
			e.setAttribute("from", to);

			Element error = ElementFactory.create("error");
			error.setAttribute("type", "wait");
			error.addChild(ElementFactory.create("recipient-unavailable", null, "urn:ietf:params:xml:ns:xmpp-stanzas"));

			e.addChild(error);

			final Runnable r = processor.process(e);
			if (r != null)
				execute(r);
		}
	}

	/**
	 * Sends IQ <code>type='get'</code> stanza to XMPP Server in current
	 * connection.
	 *
	 * @param stanza
	 *            IQ stanza to send.
	 * @param asyncCallback
	 *            callback to handle response for sent IQ stanza.
	 */
	public void send(IQ stanza, AsyncCallback asyncCallback) throws JaxmppException {
		this.writer.write(stanza, asyncCallback);
	}

	/**
	 * Sends IQ <code>type='get'</code> stanza to XMPP Server in current
	 * connection.
	 *
	 * @param stanza
	 *            IQ stanza to send.
	 * @param timeout
	 *            maximum time to wait for response in miliseconds.
	 * @param asyncCallback
	 *            asyncCallback callback to handle response for sent IQ stanza.
	 */
	public void send(IQ stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		this.writer.write(stanza, timeout, asyncCallback);
	}

	/**
	 * Sends stanza to XMPP Server in current connection.
	 *
	 * @param stanza
	 *            stanza to send.
	 */
	public void send(Stanza stanza) throws JaxmppException {
		this.writer.write(stanza);
	}

	public <T extends Property> T set(T property) {
		return (T) this.properties.put((Class<Property>) property.getPropertyClass(), property);
	}

	/**
	 * Implemented by handlers of {@linkplain LoggedInEvent LoggedInEvent}.
	 */
	public interface LoggedInHandler extends EventHandler {

		/**
		 * Called when {@linkplain LoggedInEvent LoggedInEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onLoggedIn(SessionObject sessionObject);

		/**
		 * Fired when connection is fully established.
		 */
		class LoggedInEvent extends JaxmppEvent<LoggedInHandler> {

			public LoggedInEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(LoggedInHandler handler) {
				handler.onLoggedIn(sessionObject);
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain LoggedOutEvent LoggedOutEvent}.
	 *
	 */
	public interface LoggedOutHandler extends EventHandler {

		/**
		 * Called when {@linkplain LoggedOutEvent LoggedOutEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onLoggedOut(SessionObject sessionObject);

		/**
		 * Fired when jaxmpp is disconnected.
		 */
		class LoggedOutEvent extends JaxmppEvent<LoggedOutHandler> {

			public LoggedOutEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(LoggedOutHandler handler) {
				handler.onLoggedOut(sessionObject);
			}

		}
	}
	// /**
	// * Sends Message stanza to recipient.
	// *
	// * @param toJID
	// * recipient.
	// * @param subject
	// * subject of Message.
	// * @param message
	// * content of Message.
	// */
	// public void sendMessage(JID toJID, String subject, String message) throws
	// JaxmppException {
	// (this.modulesManager.getModule(MessageModule.class)).sendMessage(toJID,
	// subject, message);
	// }

}