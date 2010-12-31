package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.logger.LoggerSpiFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class JaxmppCore {

	public static class JaxmppEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Throwable caught;

		public JaxmppEvent(EventType type) {
			super(type);
		}

		public Throwable getCaught() {
			return caught;
		}

		public void setCaught(Throwable throwable) {
			this.caught = throwable;
		}
	}

	public static final EventType Connected = new EventType();

	public static final EventType Disconnected = new EventType();

	protected Connector connector;

	protected final Logger log;

	protected final XmppModulesManager modulesManager;

	protected final Observable observable = new Observable();

	protected Processor processor;

	protected final Listener<ResourceBindEvent> resourceBindListener;

	protected XmppSessionLogic sessionLogic;

	protected SessionObject sessionObject;

	protected final Listener<ConnectorEvent> stanzaReceivedListener;

	protected final Listener<ConnectorEvent> streamErrorListener;

	protected final Listener<ConnectorEvent> streamTerminateListener;

	protected final PacketWriter writer = new PacketWriter() {

		@Override
		public void write(final Element stanza) throws JaxmppException {
			if (connector.getState() != Connector.State.connected)
				throw new JaxmppException("Not connected!");
			try {
				connector.send(stanza);
			} catch (XMLException e) {
				throw new JaxmppException(e);
			}
		}
	};

	public JaxmppCore(LoggerSpiFactory defaultLoggerSpi) {
		LoggerFactory.setLoggerSpiFactory(defaultLoggerSpi);
		this.log = LoggerFactory.getLogger(this.getClass().getName());

		modulesManager = new XmppModulesManager();

		this.resourceBindListener = new Listener<ResourceBindEvent>() {

			@Override
			public void handleEvent(ResourceBindEvent be) {
				onResourceBinded(be);
			}
		};
		this.streamTerminateListener = new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) {
				onStreamTerminated(be);
			}
		};
		this.streamErrorListener = new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) {
				onStreamError(be);
			}
		};
		this.stanzaReceivedListener = new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) {
				if (be.getStanza() != null)
					onStanzaReceived(be.getStanza());
			}
		};

	}

	public void addListener(EventType eventType, Listener<JaxmppEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<JaxmppEvent> listener) {
		observable.addListener(listener);
	}

	public Chat createChat(JID jid) {
		return (this.modulesManager.getModule(MessageModule.class)).getChatManager().createChat(jid);
	}

	public abstract void disconnect() throws JaxmppException;

	public Connector getConnector() {
		return connector;
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

	public abstract void login() throws JaxmppException;

	protected void modulesInit() {
		this.modulesManager.register(new MessageModule(sessionObject, writer));
		this.modulesManager.register(new PresenceModule(sessionObject, writer));

		this.modulesManager.register(new StreamFeaturesModule(sessionObject, writer));
		this.modulesManager.register(new SaslModule(sessionObject, writer));

		this.modulesManager.register(new PingModule(sessionObject, writer));
		this.modulesManager.register(new ResourceBinderModule(sessionObject, writer));

		this.modulesManager.register(new RosterModule(sessionObject, writer));

	}

	protected abstract void onException(JaxmppException e);

	protected abstract void onResourceBinded(ResourceBindEvent be);

	protected abstract void onStanzaReceived(Element stanza);

	protected abstract void onStreamError(ConnectorEvent be);

	protected abstract void onStreamTerminated(ConnectorEvent be);

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<JaxmppEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public abstract void send(Stanza stanza) throws XMLException, JaxmppException;

	public abstract void send(Stanza stanza, AsyncCallback asyncCallback) throws XMLException, JaxmppException;

	public void sendMessage(JID toJID, String subject, String message) throws XMLException, JaxmppException {
		(this.modulesManager.getModule(MessageModule.class)).sendMessage(toJID, subject, message);
	}

}
