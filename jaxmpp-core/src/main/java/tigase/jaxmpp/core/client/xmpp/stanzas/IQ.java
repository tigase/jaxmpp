package tigase.jaxmpp.core.client.xmpp.stanzas;

import java.util.List;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class IQ extends Stanza {

	public static final IQ create() throws XMLException {
		return new IQ(new DefaultElement("iq"));
	}

	public IQ(Element element) throws XMLException {
		super(element);
		if (!"iq".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	public Element getQuery() throws XMLException {
		List<Element> q = this.getChildren("query");
		return q != null && q.size() > 0 ? q.get(0) : null;
	}

}
