package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

public class PingModule extends AbstractIQModule {

	private final String[] FEATURES = new String[] { "urn:xmpp:ping" };

	@Override
	public Criteria getCriteria() {
		return ElementCriteria.name("iq").add(ElementCriteria.name("ping", "urn:xmpp:ping"));
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	protected void processGet(IQ stanza, SessionObject sessionObject, PacketWriter packetWriter) throws XMPPException,
			XMLException {
		Element response = XmlTools.makeResult(stanza);

		packetWriter.write(response);
	}

	@Override
	protected void processSet(IQ stanza, SessionObject sessionObject, PacketWriter packetWriter) throws XMPPException,
			XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}
