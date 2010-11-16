package tigase.jaxmpp.j2se;

import java.io.IOException;

import tigase.jaxmpp.core.client.DefaultXmppModulesManager;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.BoshConnector.BoshConnectorEvent;

public class Jaxmpp {

	private final BoshConnector connector;

	private final XmppModulesManager modulesManager;

	private final Processor processor;

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
		this.modulesManager = new DefaultXmppModulesManager();
		this.processor = new Processor(this.modulesManager, this.sessionObject, this.writer);

		this.connector.addListener(BoshConnector.STANZA_RECEIVED, new Listener<BoshConnector.BoshConnectorEvent>() {

			@Override
			public void handleEvent(BoshConnectorEvent be) {
				if (be.getStanza() != null)
					onStanzaReceived(be.getStanza());
			}
		});
	}

	public void disconnect() throws IOException, XMLException {
		this.connector.stop();
	}

	public BoshConnector getConnector() {
		return connector;
	}

	public void login() throws IOException, XMLException {
		this.connector.start();
	}

	protected void onStanzaReceived(Element stanza) {
		Runnable r = this.processor.process(stanza);
		if (r != null)
			(new Thread(r)).start();
	}

}
