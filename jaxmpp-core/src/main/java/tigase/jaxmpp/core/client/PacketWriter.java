package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

public interface PacketWriter {

	void write(Element stanza) throws JaxmppException;

	void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException;

	void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException;

}
