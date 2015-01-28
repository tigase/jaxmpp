package tigase.jaxmpp.core.client.xmpp.stream;

import java.util.HashMap;
import java.util.Set;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.ClearedHandler;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.DefaultXMPPStream;

public class XmppStreamsManager {

	public static final String DEFAULT_XMPP_STREAM_KEY = "DEFAULT_XMPP_STREAM_KEY";

	private static final String XMPP_STREAMS_MANAGER_KEY = "STREAMS_MANAGER_KEY";

	public static XmppStreamsManager getStreamsManager(SessionObject sessionObject) {
		return sessionObject.getProperty(XMPP_STREAMS_MANAGER_KEY);
	}

	public static void setStreamsManager(SessionObject sessionObject, XmppStreamsManager streamsManager) {
		sessionObject.setProperty(Scope.user, XMPP_STREAMS_MANAGER_KEY, streamsManager);
	}

	private final ClearedHandler clearedHandler = new ClearedHandler() {

		@Override
		public void onCleared(SessionObject sessionObject, Set<Scope> scopes) throws JaxmppException {
			if (scopes.contains(Scope.stream)) {
				defaultStream.setFeatures(null);
				registeredStreams.clear();
			}
		}
	};

	private Context context;

	private DefaultXMPPStream defaultStream;

	private final HashMap<JID, XMPPStream> registeredStreams = new HashMap<JID, XMPPStream>();

	public DefaultXMPPStream getDefaultStream() {
		return defaultStream;
	}

	public XMPPStream getXmppStream(JID jid) {
		return this.registeredStreams.get(jid);
	}

	public void registerXmppStream(JID jid, XMPPStream xmppStream) {
		this.registeredStreams.put(jid, xmppStream);
	}

	public void setContext(Context context) {
		if (this.context != null)
			this.context.getEventBus().remove(ClearedHandler.ClearedEvent.class, clearedHandler);
		this.context = context;
		if (this.context != null) {
			this.context.getEventBus().addHandler(ClearedHandler.ClearedEvent.class, clearedHandler);
			this.defaultStream = context.getSessionObject().getProperty(DEFAULT_XMPP_STREAM_KEY);
		}
	}

	public void unregisterXmppStream(JID jid) {
		this.registeredStreams.remove(jid);
	}

	public void writeToStream(Element stanza) throws JaxmppException {
		String to = stanza.getAttribute("to");
		XMPPStream outputStream = null;
		if (to != null) {
			outputStream = this.registeredStreams.get(JID.jidInstance(to));
		}
		if (outputStream == null)
			outputStream = defaultStream;
		outputStream.write(stanza);
	}

}
