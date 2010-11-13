package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Presence extends Stanza {

	public Presence(Element element) throws XMLException {
		super(element);
		if (!"presence".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

}
