package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaModule implements XmppModule {

	protected final Logger log;
	protected final SessionObject sessionObject;
	protected final PacketWriter writer;

	public AbstractStanzaModule(SessionObject sessionObject, PacketWriter packetWriter) {
		log = LoggerFactory.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException {
		final Stanza stanza = element instanceof Stanza ? (Stanza) element : Stanza.create(element);
		process(stanza);
	}

	public abstract void process(Stanza element) throws XMPPException, XMLException;

}
