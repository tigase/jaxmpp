package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class ChatManager {

	private static long chatIds = 1;

	private final ArrayList<Chat> chats = new ArrayList<Chat>();

	private final Observable observable;

	private final PacketWriter packetWriter;

	private final SessionObject sessionObject;

	public ChatManager(Observable observable, SessionObject sessionObject, PacketWriter packetWriter) {
		this.sessionObject = sessionObject;
		this.packetWriter = packetWriter;
		this.observable = observable;
	}

	public void close(Chat chat) throws JaxmppException {
		boolean x = this.chats.remove(chat);
		if (x) {
			MessageModule.MessageEvent event = new MessageEvent(MessageModule.ChatClosed);
			event.setChat(chat);
			observable.fireEvent(event);
		}
	}

	public Chat createChat(JID jid) throws JaxmppException {
		final String threadId = UIDGenerator.next();
		Chat chat = new Chat(++chatIds, packetWriter);
		chat.setThreadId(threadId);
		chat.setJid(jid);

		this.chats.add(chat);

		MessageEvent event = new MessageModule.MessageEvent(MessageModule.ChatCreated);
		event.setChat(chat);

		observable.fireEvent(event.getType(), event);

		return chat;
	}

	protected Chat getChat(JID jid, String threadId) {
		Chat chat = null;

		BareJID bareJID = jid.getBareJid();

		for (Chat c : this.chats) {
			if (!c.getJid().getBareJid().equals(bareJID)) {
				continue;
			}
			if (threadId != null && c.getThreadId() != null && threadId.equals(c.getThreadId())) {
				chat = c;
				break;
			}
			if (jid.getResource() != null && c.getJid().getResource() != null
					&& jid.getResource().equals(c.getJid().getResource())) {
				chat = c;
				break;
			}
			if (c.getJid().getResource() == null) {
				c.setJid(jid);
				chat = c;
				break;
			}

		}
		return chat;
	}

	public List<Chat> getChats() {
		return this.chats;
	}

	public Chat process(Message message, Observable observable) throws JaxmppException {
		if (message.getType() != StanzaType.chat)
			return null;
		final JID fromJid = message.getFrom();
		final String threadId = message.getThread();

		Chat chat = getChat(fromJid, threadId);

		if (chat == null) {
			chat = new Chat(++chatIds, packetWriter);
			chat.setJid(fromJid);
			chat.setThreadId(threadId);
			this.chats.add(chat);
			MessageEvent event = new MessageModule.MessageEvent(MessageModule.ChatCreated);
			event.setChat(chat);
			event.setMessage(message);

			observable.fireEvent(event.getType(), event);
		}

		return chat;
	}

}
