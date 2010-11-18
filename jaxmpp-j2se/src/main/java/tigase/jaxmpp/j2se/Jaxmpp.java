package tigase.jaxmpp.j2se;

import java.io.IOException;

import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.j2se.BoshConnector.BoshConnectorEvent;

public class Jaxmpp {

	private BoshConnector connector;

	private final XmppModulesManager modulesManager;

	private final Processor processor;

	private final XmppSessionLogic sessionLogic;

	private SessionObject sessionObject;

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

		modulesInit();

		this.sessionLogic.init();
	}

	public void disconnect() throws IOException, XMLException {
		this.connector.stop();
	}

	public BoshConnector getConnector() {
		return connector;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public void login() throws IOException, XMLException {
		this.connector.start(this.sessionObject);
	}

	private void modulesInit() {
		this.modulesManager.register(new StreamFeaturesModule(sessionObject, writer));
		this.modulesManager.register(new SaslModule(sessionObject, writer));

		this.modulesManager.register(new PingModule(sessionObject, writer));
		this.modulesManager.register(new ResourceBinderModule(sessionObject, writer));

	}

	protected void onStanzaReceived(Element stanza) {
		Runnable r = this.processor.process(stanza);
		if (r != null)
			(new Thread(r)).start();
	}

}
