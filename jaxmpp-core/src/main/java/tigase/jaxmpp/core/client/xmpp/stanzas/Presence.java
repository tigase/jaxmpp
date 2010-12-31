package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Presence extends Stanza {

	public static enum Show {
		away,
		chat,
		dnd,
		online,
		xa
	}

	public static Presence create() throws XMLException {
		return new Presence(new DefaultElement("presence"));
	}

	public Presence(Element element) throws XMLException {
		super(element);
		if (!"presence".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	public String getNickname() throws XMLException {
		return getChildElementValue("nick", "http://jabber.org/protocol/nick");
	}

	public Integer getPriority() throws XMLException {
		String x = getChildElementValue("priority");
		if (x == null)
			return 0;
		return Integer.valueOf(x);
	}

	public Show getShow() throws XMLException {
		String x = getChildElementValue("show");
		if (x == null)
			return Show.online;
		return Show.valueOf(x);
	}

	public String getStatus() throws XMLException {
		return getChildElementValue("status");
	}

	public void setNickname(String value) throws XMLException {
		setChildElementValue("nick", "http://jabber.org/protocol/nick", value == null ? null : value.toString());
	}

	public void setPriority(Integer value) throws XMLException {
		setChildElementValue("priority", value == null ? null : value.toString());
	}

	public void setShow(Show value) throws XMLException {
		if (value == null || value == Show.online)
			setChildElementValue("show", null);
		else
			setChildElementValue("show", value.name());
	}

	public void setStatus(String value) throws XMLException {
		setChildElementValue("status", value);
	}

}
