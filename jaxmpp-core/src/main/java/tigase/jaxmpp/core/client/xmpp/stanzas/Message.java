package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.EscapeUtils;

public class Message extends Stanza {

	public static final Message create() throws XMLException {
		return new Message(new DefaultElement("message"));
	}

	public Message(Element element) throws XMLException {
		super(element);
		if (!"message".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	public String getBody() throws XMLException {
		return EscapeUtils.unescape(getChildElementValue("body"));
	}

	public String getSubject() throws XMLException {
		return getChildElementValue("subject");
	}

	public String getThread() throws XMLException {
		return getChildElementValue("thread");
	}

	@Override
	public StanzaType getType() throws XMLException {
		return super.getType(StanzaType.normal);
	}

	public void setBody(String body) throws XMLException {
		setChildElementValue("body", EscapeUtils.escape(body));
	}

	public void setSubject(String subject) throws XMLException {
		setChildElementValue("subject", subject);
	}

	public void setThread(String thread) throws XMLException {
		setChildElementValue("thread", thread);
	}

}
