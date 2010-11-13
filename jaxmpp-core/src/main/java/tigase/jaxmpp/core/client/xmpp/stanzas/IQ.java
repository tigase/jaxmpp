package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class IQ extends Stanza {

	public IQ(Element element) throws XMLException {
		super(element);
		if (!"iq".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

}
