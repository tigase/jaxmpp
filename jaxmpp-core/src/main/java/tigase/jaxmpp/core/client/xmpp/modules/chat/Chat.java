package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Chat {

	public static final EventType MessageReceived = new EventType();

	private boolean closed = false;

	private final long id;

	private JID jid;

	private final SessionObject sessionObject;

	private String threadId;

	private final PacketWriter writer;

	public Chat(long id, PacketWriter packetWriter, SessionObject sessionObject) {
		this.id = id;
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public long getId() {
		return id;
	}

	public JID getJid() {
		return jid;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public String getThreadId() {
		return threadId;
	}

	public boolean isClosed() {
		return closed;
	}

	public void sendMessage(String body) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setTo(jid);
		msg.setType(StanzaType.chat);
		msg.setThread(threadId);
		msg.setBody(body);

		this.writer.write(msg);
	}

	public void setJid(JID jid) {
		this.jid = jid;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

}
