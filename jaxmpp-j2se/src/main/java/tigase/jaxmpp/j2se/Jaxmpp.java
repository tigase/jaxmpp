package tigase.jaxmpp.j2se;

import java.io.IOException;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UserProperties;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.observer.Listener;
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

	public static final String CONNECTOR_TYPE = "connectorType";

	public static final String SYNCHRONIZED_MODE = "jaxmpp#synchronized";

	private Connector connector;

	private final Logger log;

	private final XmppModulesManager modulesManager;

	private final Processor processor;

	private final Listener<ResourceBindEvent> resourceBindListener;

	private XmppSessionLogic sessionLogic;

	private SessionObject sessionObject;

	private final Listener<ConnectorEvent> stanzaReceivedListener;

	private final Listener<ConnectorEvent> streamErrorListener;

	private final Listener<ConnectorEvent> streamTerminateListener;

	private final PacketWriter writer;

	public Jaxmpp() {
		Logger.setLoggerSpiFactory(new DefaultLoggerSpi());

		this.log = Logger.getLogger(this.getClass().getName());

		this.writer = new PacketWriter() {

			@Override
			public void write(Element stanza) throws JaxmppException {
				try {
					connector.send(stanza);
				} catch (XMLException e) {
					throw new RuntimeException(e);
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

	public Chat createChat(JID jid) {
		return (this.modulesManager.getModule(MessageModule.class)).getChatManager().createChat(jid);
	}

	public void disconnect() throws IOException, XMLException, InterruptedException, JaxmppException {
		this.connector.stop();
		if ((Boolean) this.sessionObject.getProperty(SYNCHRONIZED_MODE)) {
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.wait();
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
		this.sessionLogic.bind();

		this.connector.start();
		this.sessionObject.setProperty(SYNCHRONIZED_MODE, Boolean.valueOf(sync));
		if (sync)
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.wait();
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

	protected void onResourceBinded(ResourceBindEvent be) {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
	}

	protected void onStanzaReceived(Element stanza) {
		Runnable r = this.processor.process(stanza);
		if (r != null)
			(new Thread(r)).start();
	}

	protected void onStreamError(ConnectorEvent be) {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
	}

	protected void onStreamTerminated(ConnectorEvent be) {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
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
