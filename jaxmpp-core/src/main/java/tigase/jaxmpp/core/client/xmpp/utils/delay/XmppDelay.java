package tigase.jaxmpp.core.client.xmpp.utils.delay;

import java.util.Date;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class XmppDelay extends ElementWrapper {

	public static XmppDelay extract(Stanza stanza) throws XMLException {
		final Element x = stanza.getChildrenNS("delay", "urn:xmpp:delay");
		if (x == null)
			return null;
		return new XmppDelay(x);
	}

	private XmppDelay(Element element) throws XMLException {
		super(element);
	}

	public JID getFrom() throws XMLException {
		String tmp = getAttribute("from");
		return tmp == null ? null : JID.jidInstance(tmp);
	}

	public Date getStamp() throws XMLException {
		String tmp = getAttribute("stamp");
		DateTimeFormat dtf = new DateTimeFormat();
		return dtf.parse(tmp);
	}
}
