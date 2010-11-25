package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Chat {

	public static class ChatEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Chat chat;

		private Message message;

		public ChatEvent(EventType type) {
			super(type);
		}

		public Chat getChat() {
			return chat;
		}

		public Message getMessage() {
			return message;
		}

		public void setChat(Chat chat) {
			this.chat = chat;
		}

		public void setMessage(Message message) {
			this.message = message;
		}
	}

	public static final EventType CHAT_CLOSED = new EventType();

	public static final EventType MESSAGE_RECEIVED = new EventType();

	private boolean closed = false;

	private JID jid;

	private final Observable observable = new Observable();

	private String threadId;

	private final PacketWriter writer;

	public Chat(PacketWriter packetWriter) {
		this.writer = packetWriter;
	}

	public void addListener(EventType eventType, Listener<ChatEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void close() {
		closed = true;
		ChatEvent event = new ChatEvent(CHAT_CLOSED);
		event.setChat(this);
		observable.fireEvent(event.getType(), event);
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

	public void removeListener(EventType eventType, Listener<ChatEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void sendMessage(String body) throws XMLException {
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
