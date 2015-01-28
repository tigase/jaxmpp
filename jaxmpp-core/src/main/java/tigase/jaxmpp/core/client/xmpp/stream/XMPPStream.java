package tigase.jaxmpp.core.client.xmpp.stream;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

public interface XMPPStream {

	Element getFeatures();

	void setFeatures(Element children);

	public void write(final Element stanza) throws JaxmppException;

}
