package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaHandler implements Runnable {

	protected final SessionObject sessionObject;

	protected final Stanza stanza;

	protected final PacketWriter writer;

	public AbstractStanzaHandler(Stanza stanza, PacketWriter writer, SessionObject sessionObject) {
		super();
		this.writer = writer;
		this.stanza = stanza;
		this.sessionObject = sessionObject;
	}

	protected abstract void process() throws XMLException, XMPPException, JaxmppException;

	@Override
	public void run() {
		try {
			try {
				process();
			} catch (Exception e) {
				Element errorResult = Processor.createError(stanza, e);
				if (errorResult != null)
					writer.write(errorResult);
			}
		} catch (JaxmppException e) {
			throw new RuntimeException(e);
			// TODO: handle exception
		}
	}

}
