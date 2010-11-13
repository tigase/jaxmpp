package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractIQModule implements XmppModule {

	protected final Logger log;

	public AbstractIQModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	@Override
	public void process(Stanza element, SessionObject sessionObject, PacketWriter packetWriter) throws XMPPException,
			XMLException {
		final String type = element.getAttribute("type");

		if (element instanceof IQ && type != null && type.equals("set"))
			processSet((IQ) element, sessionObject, packetWriter);
		else if (element instanceof IQ && type != null && type.equals("get"))
			processGet((IQ) element, sessionObject, packetWriter);
		else {
			log.log(LogLevel.WARNING, "Unhandled stanza " + element.getName() + ", type=" + element.getAttribute("type")
					+ ", id=" + element.getAttribute("id"));
			throw new XMPPException(ErrorCondition.bad_request);
		}
	}

	protected abstract void processGet(IQ element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException;

	protected abstract void processSet(IQ element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException;
}
