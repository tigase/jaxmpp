package tigase.jaxmpp.j2se;

import java.io.IOException;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.Connector.Stage;
import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UserProperties;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.XmppSessionLogic.SessionListener;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
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
import tigase.jaxmpp.j2se.connectors.bosh.BoshConnector;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

public class Jaxmpp {

	public static class JaxmppEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public JaxmppEvent(EventType type) {
			super(type);
		}
	}

	public static final EventType CONNECTED = new EventType();

	public static final String CONNECTOR_TYPE = "connectorType";

	public static final EventType DISCONNECTED = new EventType();

	public static final String EXCEPTION_KEY = "jaxmpp#ThrowedException";

	public static final String SYNCHRONIZED_MODE = "jaxmpp#synchronized";

	private Connector connector;

	private final Logger log;

	private final XmppModulesManager modulesManager;

	protected final Observable observable = new Observable();

	private final Processor processor;

	private final Listener<ResourceBindEvent> resourceBindListener;

	private XmppSessionLogic sessionLogic;

	private SessionObject sessionObject;

	private final Listener<ConnectorEvent> stanzaReceivedListener;

	private final Listener<ConnectorEvent> streamErrorListener;

	private final Listener<ConnectorEvent> streamTerminateListener;

	private final PacketWriter writer;

	public Jaxmpp() {
		LoggerFactory.setLoggerSpiFactory(new DefaultLoggerSpi());

		this.log = LoggerFactory.getLogger(this.getClass().getName());

		this.writer = new PacketWriter() {

			@Override
			public void write(final Element stanza) throws JaxmppException {
				if (connector.getStage() != Connector.Stage.connected)
					throw new JaxmppException("Not connected!");
				try {
					connector.send(stanza);
				} catch (XMLException e) {
					throw new JaxmppException(e);
				}
			}
		};
		this.sessionObject = new DefaultSessionObject();
		this.modulesManager = new XmppModulesManager();
		this.processor = new Processor(this.modulesManager, this.sessionObject, this.writer);

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
		this.stanzaReceivedListener = new Listener<BoshConnector.ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) {
				if (be.getStanza() != null)
					onStanzaReceived(be.getStanza());
			}
		};

		modulesInit();

		ResourceBinderModule r = this.modulesManager.getModule(ResourceBinderModule.class);
		r.addListener(ResourceBinderModule.BIND_SUCCESSFULL, resourceBindListener);

	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	public Chat createChat(JID jid) {
		return (this.modulesManager.getModule(MessageModule.class)).getChatManager().createChat(jid);
	}

	public void disconnect() throws IOException, XMLException, InterruptedException, JaxmppException {
		this.connector.stop();
		if ((Boolean) this.sessionObject.getProperty(SYNCHRONIZED_MODE)) {
			synchronized (Jaxmpp.this) {
				// Jaxmpp.this.wait();
			}
		}
	}

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

	public boolean isConnected() {
		return this.connector != null && this.connector.getStage() == Stage.connected
				&& this.sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID) != null;
	}

	public void login() throws IOException, XMLException, InterruptedException, JaxmppException {
		login(true);
	}

	public void login(boolean sync) throws IOException, XMLException, InterruptedException, JaxmppException {
		this.sessionObject.clear();

		if (this.sessionLogic != null) {
			this.sessionLogic.unbind();
			this.sessionLogic = null;
		}
		if (this.connector != null) {
			this.connector.removeAllListeners();
			this.connector = null;
		}

		if (sessionObject.getProperty(CONNECTOR_TYPE) == null || "socket".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using SocketConnector");
			this.connector = new SocketConnector(this.sessionObject);
		} else if ("bosh".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using BOSHConnector");
			this.connector = new BoshConnector(this.sessionObject);
		} else
			throw new JaxmppException("Unknown connector type");

		this.connector.addListener(Connector.STANZA_RECEIVED, this.stanzaReceivedListener);
		connector.addListener(Connector.TERMINATE, this.streamTerminateListener);
		connector.addListener(Connector.ERROR, this.streamErrorListener);

		this.sessionLogic = connector.createSessionLogic(modulesManager, this.writer);
		this.sessionLogic.bind(new SessionListener() {

			@Override
			public void onException(JaxmppException e) {
				Jaxmpp.this.onException(e);
			}
		});

		this.connector.start();
		this.sessionObject.setProperty(SYNCHRONIZED_MODE, Boolean.valueOf(sync));
		if (sync)
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.wait();
				log.finest("Waked up");
			}
		if (sessionObject.getProperty(EXCEPTION_KEY) != null) {
			JaxmppException r = (JaxmppException) sessionObject.getProperty(EXCEPTION_KEY);
			JaxmppException e = new JaxmppException(r.getMessage(), r.getCause());
			throw e;
		}
	}

	private void modulesInit() {
		this.modulesManager.register(new MessageModule(sessionObject, writer));
		this.modulesManager.register(new PresenceModule(sessionObject, writer));

		this.modulesManager.register(new StreamFeaturesModule(sessionObject, writer));
		this.modulesManager.register(new SaslModule(sessionObject, writer));

		this.modulesManager.register(new PingModule(sessionObject, writer));
		this.modulesManager.register(new ResourceBinderModule(sessionObject, writer));

		this.modulesManager.register(new RosterModule(sessionObject, writer));

	}

	protected void onException(JaxmppException e) {
		log.log(LogLevel.FINE, "Catching exception", e);
		sessionObject.setProperty(EXCEPTION_KEY, e);
		try {
			connector.stop();
		} catch (Exception e1) {
			log.log(LogLevel.FINE, "Disconnecting error", e1);
		}
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(DISCONNECTED);
		observable.fireEvent(event);
	}

	protected void onResourceBinded(ResourceBindEvent be) {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(CONNECTED);
		observable.fireEvent(event);
	}

	protected void onStanzaReceived(Element stanza) {
		Runnable r = this.processor.process(stanza);
		if (r != null)
			(new Thread(r)).start();
	}

	protected void onStreamError(ConnectorEvent be) {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(DISCONNECTED);
		observable.fireEvent(event);
	}

	protected void onStreamTerminated(ConnectorEvent be) {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(DISCONNECTED);
		observable.fireEvent(event);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void send(Stanza stanza) throws XMLException, JaxmppException {
		this.writer.write(stanza);
	}

	public void send(Stanza stanza, AsyncCallback asyncCallback) throws XMLException, JaxmppException {
		this.sessionObject.registerResponseHandler(stanza, asyncCallback);
		this.writer.write(stanza);
	}

	public void sendMessage(JID toJID, String subject, String message) throws XMLException, JaxmppException {
		(this.modulesManager.getModule(MessageModule.class)).sendMessage(toJID, subject, message);
	}

}
