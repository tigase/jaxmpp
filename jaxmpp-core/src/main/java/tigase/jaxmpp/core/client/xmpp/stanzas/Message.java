package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Message extends Stanza {

	public Message(Element element) throws XMLException {
		super(element);
		if (!"message".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

}
