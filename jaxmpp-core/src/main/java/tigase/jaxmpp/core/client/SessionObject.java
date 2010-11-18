package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public interface SessionObject {

	public static final String PASSWORD = "password";

	public static final String RESOURCE = "resource";

	public static final String SERVER_NAME = "serverName";

	public static final String USER_JID = "userJid";

	public <T> T getProperty(String key);

	public Runnable getResponseHandler(final Element element, PacketWriter writer, SessionObject sessionObject)
			throws XMLException;

	public Element getStreamFeatures();

	public String registerResponseHandler(Element stanza, AsyncCallback callback) throws XMLException;

	public void setProperty(String key, Object value);

	public void setStreamFeatures(Element element);

}
