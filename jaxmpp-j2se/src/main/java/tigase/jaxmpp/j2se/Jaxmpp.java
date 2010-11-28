package tigase.jaxmpp.j2se;

import java.io.IOException;

import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UserProperties;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.roster.Roster;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.j2se.BoshConnector.BoshConnectorEvent;

public class Jaxmpp {

	public static final String SYNCHRONIZED_MODE = "jaxmpp#synchronized";

	private BoshConnector connector;

	private final XmppModulesManager modulesManager;

	private final Processor processor;

	private final Listener<ResourceBindEvent> resourceBindListener;

	private final XmppSessionLogic sessionLogic;

	private SessionObject sessionObject;

	private final Listener<BoshConnectorEvent> streamTerminateListener;

	private final PacketWriter writer;

	public Jaxmpp() {
		Logger.setLoggerSpiFactory(new DefaultLoggerSpi());

		this.connector = new BoshConnector();

		this.writer = new PacketWriter() {

			@Override
			public void write(Element stanza) {
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
		this.sessionLogic = new XmppSessionLogic(connector, modulesManager, this.sessionObject, this.writer);

		this.connector.addListener(BoshConnector.STANZA_RECEIVED, new Listener<BoshConnector.BoshConnectorEvent>() {

			@Override
			public void handleEvent(BoshConnectorEvent be) {
				if (be.getStanza() != null)
					onStanzaReceived(be.getStanza());
			}
		});

		this.resourceBindListener = new Listener<ResourceBindEvent>() {

			@Override
			public void handleEvent(ResourceBindEvent be) {
				onResourceBinded(be);
			}
		};
		this.streamTerminateListener = new Listener<BoshConnectorEvent>() {

			@Override
			public void handleEvent(BoshConnectorEvent be) {
				onStreamTerminated(be);
			}
		};

		modulesInit();

		this.sessionLogic.init();

		ResourceBinderModule r = this.modulesManager.getModule(ResourceBinderModule.class);
		r.addListener(ResourceBinderModule.BIND_SUCCESSFULL, resourceBindListener);

		connector.addListener(BoshConnector.TERMINATE, this.streamTerminateListener);
	}

	public Chat createChat(JID jid) {
		return (this.modulesManager.getModule(MessageModule.class)).getChatManager().createChat(jid);
	}

	public void disconnect() throws IOException, XMLException, InterruptedException {
		this.connector.stop();
		if ((Boolean) this.sessionObject.getProperty(SYNCHRONIZED_MODE)) {
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.wait();
			}
		}
	}

	public BoshConnector getConnector() {
		return connector;
	}

	public XmppModulesManager getModulesManager() {
		return modulesManager;
	}

	public UserProperties getProperties() {
		return sessionObject;
	}

	public Roster getRoster() {
		return sessionObject.getRoster();
	}

	public void login() throws IOException, XMLException, InterruptedException {
		login(true);
	}

	public void login(boolean sync) throws IOException, XMLException, InterruptedException {
		this.sessionObject.clear();
		this.connector.start(this.sessionObject);
		this.sessionObject.setProperty(SYNCHRONIZED_MODE, Boolean.valueOf(sync));
		if (sync)
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.wait();
			}
	}

	private void modulesInit() {
		this.modulesManager.register(new MessageModule(sessionObject, writer));

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

	protected void onStreamError(BoshConnectorEvent be) {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
	}

	protected void onStreamTerminated(BoshConnectorEvent be) {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
	}

	public void sendMessage(JID toJID, String subject, String message) throws XMLException {
		(this.modulesManager.getModule(MessageModule.class)).sendMessage(toJID, subject, message);
	}

}
