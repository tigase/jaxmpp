package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public abstract class AbstractIQModule implements XmppModule {

	protected final Logger log;

	public AbstractIQModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	@Override
	public void process(Element $element, SessionObject sessionObject, PacketWriter packetWriter) throws XMPPException,
			XMLException {
		final Stanza stanza = $element instanceof Stanza ? (Stanza) $element : Stanza.create($element);
		final StanzaType type = stanza.getType();

		if (stanza instanceof IQ && type == StanzaType.set)
			processSet((IQ) stanza, sessionObject, packetWriter);
		else if (stanza instanceof IQ && type == StanzaType.get)
			processGet((IQ) stanza, sessionObject, packetWriter);
		else {
			log.log(LogLevel.WARNING, "Unhandled stanza " + $element.getName() + ", type=" + $element.getAttribute("type")
					+ ", id=" + $element.getAttribute("id"));
			throw new XMPPException(ErrorCondition.bad_request);
		}
	}

	protected abstract void processGet(IQ element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException;

	protected abstract void processSet(IQ element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException;
}
