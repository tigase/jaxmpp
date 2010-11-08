package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xml.Element;

public interface PacketWriter {

	void write(Element stanza);

}
