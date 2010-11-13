package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class DefaultSessionObject implements SessionObject {

	private final ResponseManager responseManager = new ResponseManager();

	@Override
	public Runnable getResponseHandler(Element element, PacketWriter writer, SessionObject sessionObject) throws XMLException {
		return responseManager.getResponseHandler(element, writer, sessionObject);
	}

	@Override
	public String registerResponseHandler(Element stanza, AsyncCallback callback) throws XMLException {
		return responseManager.registerResponseHandler(stanza, callback);
	}

}
