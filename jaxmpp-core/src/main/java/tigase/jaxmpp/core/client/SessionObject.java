package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public interface SessionObject extends UserProperties {

	public static final String NICKNAME = "nickname";

	public static final String PASSWORD = "password";

	public static final String RESOURCE = "resource";

	public static final String SERVER_NAME = "serverName";

	public static final String USER_BARE_JID = "userBareJid";

	public void checkHandlersTimeout() throws JaxmppException;

	public void clear() throws JaxmppException;

	public PresenceStore getPresence();

	public <T> T getProperty(String key);

	public Runnable getResponseHandler(final Element element, PacketWriter writer, SessionObject sessionObject)
			throws XMLException;

	public RosterStore getRoster();

	public Element getStreamFeatures();

	public BareJID getUserBareJid();

	public String registerResponseHandler(Element stanza, AsyncCallback callback) throws XMLException;

	public void setProperty(String key, Object value);

	public void setStreamFeatures(Element element);

}
