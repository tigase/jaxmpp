package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.DefaultXmppModulesManager;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.xml.Element;

public class Jaxmpp {

	private final XmppModulesManager modulesManager;

	private final Processor processor;

	private SessionObject sessionObject;

	private final PacketWriter writer = new PacketWriter() {

		@Override
		public void write(Element stanza) {
			// TODO Auto-generated method stub

		}
	};

	public Jaxmpp() {
		Logger.setLoggerSpiFactory(new DefaultLoggerSpi());

		this.sessionObject = new DefaultSessionObject();
		this.modulesManager = new DefaultXmppModulesManager();
		this.processor = new Processor(this.modulesManager, this.sessionObject, this.writer);
	}

	public void login() {
	}

}
