package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Chat {

	public static final EventType MessageReceived = new EventType();

	private boolean closed = false;

	private JID jid;

	private String threadId;

	private final PacketWriter writer;

	public Chat(PacketWriter packetWriter) {
		this.writer = packetWriter;
	}

	public JID getJid() {
		return jid;
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

	void setJid(JID jid) {
		this.jid = jid;
	}

	void setThreadId(String threadId) {
		this.threadId = threadId;
	}

}
