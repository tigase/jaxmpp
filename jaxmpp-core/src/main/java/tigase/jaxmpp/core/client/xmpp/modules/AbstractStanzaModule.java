package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaModule implements XmppModule {

	@Override
	public void process(Element element, SessionObject sessionObject, PacketWriter packetWriter) throws XMPPException,
			XMLException {
		final Stanza stanza = element instanceof Stanza ? (Stanza) element : Stanza.create(element);
		process(stanza, sessionObject, packetWriter);
	}

	public abstract void process(Stanza element, SessionObject sessionObject, PacketWriter packetWriter) throws XMPPException,
			XMLException;

}
