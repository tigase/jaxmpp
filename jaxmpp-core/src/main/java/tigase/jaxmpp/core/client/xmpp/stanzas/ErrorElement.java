package tigase.jaxmpp.core.client.xmpp.stanzas;

import java.util.List;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

public class ErrorElement extends ElementWrapper {

	public static ErrorElement extract(Element stanza) throws XMLException {
		final List<Element> xs = stanza.getChildren("error");
		if (xs == null || xs.size() == 0)
			return null;
		return new ErrorElement(xs.get(0));
	}

	private ErrorElement(Element element) throws XMLException {
		super(element);
	}

	public String getCode() throws XMLException {
		return getAttribute("code");
	}

	public ErrorCondition getCondition() throws XMLException {
		List<Element> cs = getChildrenNS("urn:ietf:params:xml:ns:xmpp-stanzas");
		for (Element element : cs) {
			ErrorCondition r = ErrorCondition.getByElementName(element.getName());
			if (r != null)
				return r;
		}
		return null;
	}

	public String getText() throws XMLException {
		Element e = getChildrenNS("text", "urn:ietf:params:xml:ns:xmpp-stanzas");
		if (e != null)
			return e.getValue();
		else
			return null;
	}

	public String getType() throws XMLException {
		return getAttribute("type");
	}
}
