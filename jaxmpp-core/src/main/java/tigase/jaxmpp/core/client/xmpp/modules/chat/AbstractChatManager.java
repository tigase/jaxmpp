package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public abstract class AbstractChatManager {

	protected final ArrayList<Chat> chats = new ArrayList<Chat>();

	protected ChatSelector chatSelector;

	protected Observable observable;

	protected PacketWriter packetWriter;

	protected SessionObject sessionObject;

	protected AbstractChatManager() {
		ChatSelector x = UniversalFactory.createInstance(ChatSelector.class.getName());
		this.chatSelector = x == null ? new DefaultChatSelector() : x;
	}

	public boolean close(Chat chat) throws JaxmppException {
		boolean x = this.chats.remove(chat);
		if (x) {
			MessageModule.MessageEvent event = new MessageEvent(MessageModule.ChatClosed, sessionObject);
			event.setChat(chat);
			observable.fireEvent(event);
		}
		return x;
	}

	public Chat createChat(JID jid) throws JaxmppException {
		final String threadId = UIDGenerator.next();
		Chat chat = createChatInstance(jid, threadId);

		this.chats.add(chat);

		MessageEvent event = new MessageModule.MessageEvent(MessageModule.ChatCreated, sessionObject);
		event.setChat(chat);

		observable.fireEvent(event.getType(), event);

		return chat;
	}

	protected abstract Chat createChatInstance(final JID fromJid, final String threadId);

	protected Chat getChat(JID jid, String threadId) {
		return chatSelector.getChat(chats, jid, threadId);
	}

	public List<Chat> getChats() {
		return this.chats;
	}

	Observable getObservable() {
		return observable;
	}

	PacketWriter getPacketWriter() {
		return packetWriter;
	}

	SessionObject getSessionObject() {
		return sessionObject;
	}

	protected void initialize() {
	}

	public boolean isChatOpenFor(final BareJID jid) {
		for (Chat chat : this.chats) {
			if (chat.getJid().getBareJid().equals(jid))
				return true;
		}
		return false;
	}

	public Chat process(Message message, Observable observable) throws JaxmppException {
		if (message.getType() != StanzaType.chat && message.getType() != StanzaType.error 
                        && message.getType() != StanzaType.headline)
			return null;
		final JID fromJid = message.getFrom();
		final String threadId = message.getThread();

		Chat chat = getChat(fromJid, threadId);

                if (chat == null && message.getBody() == null) {
                        return null;
                }
                
		if (chat == null) {                        
			chat = createChatInstance(fromJid, threadId);
			chat.setJid(fromJid);
			chat.setThreadId(threadId);
			this.chats.add(chat);
			MessageEvent event = new MessageModule.MessageEvent(MessageModule.ChatCreated, sessionObject);
			event.setChat(chat);
			event.setMessage(message);

			observable.fireEvent(event.getType(), event);
		} else {
			update(chat, fromJid, threadId);
		}

		return chat;
	}

	void setObservable(Observable observable) {
		this.observable = observable;
	}

	void setPacketWriter(PacketWriter packetWriter) {
		this.packetWriter = packetWriter;
	}

	void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	protected boolean update(final Chat chat, final JID fromJid, final String threadId) throws JaxmppException {
		boolean changed = false;

		if (!chat.getJid().equals(fromJid)) {
			chat.setJid(fromJid);
			changed = true;
		}

		if (chat.getThreadId() == null && threadId != null) {
			chat.setThreadId(threadId);
			changed = true;
		}

		if (changed) {
			MessageEvent event = new MessageModule.MessageEvent(MessageModule.ChatUpdated, sessionObject);
			event.setChat(chat);
			observable.fireEvent(event.getType(), event);
		}

		return changed;
	}

}
