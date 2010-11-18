package tigase.jaxmpp.core.client;

import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class DefaultSessionObject implements SessionObject {

	protected final Map<String, Object> properties = new HashMap<String, Object>();

	protected final ResponseManager responseManager = new ResponseManager();

	protected Element streamFeatures;

	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key) {
		return (T) this.properties.get(key);
	}

	@Override
	public Runnable getResponseHandler(Element element, PacketWriter writer, SessionObject sessionObject) throws XMLException {
		return responseManager.getResponseHandler(element, writer, sessionObject);
	}

	@Override
	public Element getStreamFeatures() {
		return this.streamFeatures;
	}

	@Override
	public String registerResponseHandler(Element stanza, AsyncCallback callback) throws XMLException {
		return responseManager.registerResponseHandler(stanza, callback);
	}

	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}

	@Override
	public void setStreamFeatures(Element element) {
		this.streamFeatures = element;
	}

}
