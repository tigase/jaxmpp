package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xml.XMLException;

public interface ResponseHandler {

	void process() throws XMLException, XMPPException;

}
