package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.logging.Logger;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaModule<T extends Stanza> implements XmppModule {

	protected final Logger log;
	protected final SessionObject sessionObject;
	protected final PacketWriter writer;

	public AbstractStanzaModule(SessionObject sessionObject, PacketWriter packetWriter) {
		log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void process(Element element) throws JaxmppException {
		final T stanza = element instanceof Stanza ? (T) element : (T) Stanza.create(element);
		process(stanza);
	}

	public abstract void process(T element) throws JaxmppException;

}
