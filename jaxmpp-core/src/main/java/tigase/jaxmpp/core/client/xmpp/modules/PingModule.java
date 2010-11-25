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

	private final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("ping", "urn:xmpp:ping"));

	private final String[] FEATURES = new String[] { "urn:xmpp:ping" };

	public PingModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	protected void processGet(IQ stanza) throws XMPPException, XMLException {
		Element response = XmlTools.makeResult(stanza);

		writer.write(response);
	}

	@Override
	protected void processSet(IQ stanza) throws XMPPException, XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}
