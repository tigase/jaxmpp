/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Module to handle messages.
 */
public class MessageModule extends AbstractStanzaModule<Message> {

	public interface ChatClosedHandler extends EventHandler {

		public static class ChatClosedEvent extends JaxmppEvent<ChatClosedHandler> {

			private Chat chat;

			public ChatClosedEvent(SessionObject sessionObject, Chat chat) {
				super(sessionObject);
				this.chat = chat;
			}

			@Override
			protected void dispatch(ChatClosedHandler handler) {
				handler.onChatClosed(sessionObject, chat);
			}

			public Chat getChat() {
				return chat;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

		}

		void onChatClosed(SessionObject sessionObject, Chat chat);
	}

	public interface ChatCreatedHandler extends EventHandler {

		public static class ChatCreatedEvent extends JaxmppEvent<ChatCreatedHandler> {

			private Chat chat;

			private Message message;

			public ChatCreatedEvent(SessionObject sessionObject, Chat chat, Message message) {
				super(sessionObject);
				this.chat = chat;
				this.message = message;
			}

			@Override
			protected void dispatch(ChatCreatedHandler handler) {
				handler.onChatCreated(sessionObject, chat, message);
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

		void onChatCreated(SessionObject sessionObject, Chat chat, Message message);
	}

	public interface ChatUpdatedHandler extends EventHandler {

		public static class ChatUpdatedEvent extends JaxmppEvent<ChatUpdatedHandler> {

			private Chat chat;

			public ChatUpdatedEvent(SessionObject sessionObject, Chat chat) {
				super(sessionObject);
				this.chat = chat;
			}

			@Override
			protected void dispatch(ChatUpdatedHandler handler) {
				handler.onChatUpdated(sessionObject, chat);
			}

			public Chat getChat() {
				return chat;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

		}

		void onChatUpdated(SessionObject sessionObject, Chat chat);
	}

	public interface MessageReceivedHandler extends EventHandler {

		public static class MessageReceivedEvent extends JaxmppEvent<MessageReceivedHandler> {

			private final Chat chat;

			private final Message stanza;

			public MessageReceivedEvent(SessionObject sessionObject, Message stanza, Chat chat) {
				super(sessionObject);
				this.stanza = stanza;
				this.chat = chat;
			}

			@Override
			protected void dispatch(MessageReceivedHandler handler) {
				handler.onMessageReceived(sessionObject, chat, stanza);
			}

			public Chat getChat() {
				return chat;
			}

			public Message getStanza() {
				return stanza;
			}

		}

		void onMessageReceived(SessionObject sessionObject, Chat chat, Message stanza);
	}

	private static final Criteria CRIT = new Criteria() {

		@Override
		public Criteria add(Criteria criteria) {
			return null;
		}

		@Override
		public boolean match(Element element) throws XMLException {
			final String type = element.getAttribute("type");
			if ("message".equals(element.getName()) && (type == null || !type.equals("groupchat"))) {
				List<Element> l = element.getChildrenNS(MessageCarbonsModule.XMLNS_MC);
				return l == null || l.isEmpty();
			}
			return false;
		}
	};

	private final AbstractChatManager chatManager;

	public MessageModule() {
		AbstractChatManager cm = UniversalFactory.createInstance(AbstractChatManager.class.getName());
		this.chatManager = cm != null ? cm : new DefaultChatManager();
	}

	@Override
	public void beforeRegister() {
		// TODO Auto-generated method stub
		super.beforeRegister();

		this.chatManager.setContext(context);
		this.chatManager.initialize();
	}

	/**
	 * Destroy chat object.
	 * 
	 * @param chat
	 *            chat object
	 */
	public void close(Chat chat) throws JaxmppException {
		chatManager.close(chat);
	}

	/**
	 * Creates new chat object.
	 * 
	 * @param jid
	 *            destination JID
	 * @return chat object
	 */
	public Chat createChat(JID jid) throws JaxmppException {
		return this.chatManager.createChat(jid, generateThreadID());
	}

	protected String generateThreadID() {
		return UIDGenerator.next() + UIDGenerator.next() + UIDGenerator.next();
	}

	public AbstractChatManager getChatManager() {
		return chatManager;
	}

	/**
	 * Returns all chat objects.
	 * 
	 * @return collection of chat objects.
	 */
	public List<Chat> getChats() {
		return this.chatManager.getChats();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFeatures() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Message message) throws JaxmppException {
		final JID interlocutorJid = message.getFrom();
		process(message, interlocutorJid, true);
	}

	Chat process(final Message message, final JID interlocutorJid, final boolean fireReceivedEvent) throws JaxmppException {
		if (message.getType() != StanzaType.chat && message.getType() != StanzaType.error
				&& message.getType() != StanzaType.headline)
			return null;

		final String threadId = message.getThread();

		Chat chat = chatManager.getChat(interlocutorJid, threadId);

		if (chat == null && message.getBody() == null) {
			// no chat, not body. Lets skip it.
			return null;
		}

		if (chat == null) {
			chat = chatManager.createChat(interlocutorJid, threadId);
			fireEvent(new ChatCreatedHandler.ChatCreatedEvent(context.getSessionObject(), chat, message));
		} else {
			update(chat, interlocutorJid, threadId);
		}

		if (fireReceivedEvent)
			fireEvent(new MessageReceivedHandler.MessageReceivedEvent(context.getSessionObject(), message, chat));

		return chat;
	}

	/**
	 * Sends message. It does not create chat object.
	 * 
	 * @param toJID
	 *            recipient's JID
	 * @param subject
	 *            subject of message
	 * @param message
	 *            message
	 */
	public void sendMessage(JID toJID, String subject, String message) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setSubject(subject);
		msg.setBody(message);
		msg.setTo(toJID);
		msg.setId(UIDGenerator.next());

		write(msg);
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
			ChatUpdatedHandler.ChatUpdatedEvent event = new ChatUpdatedHandler.ChatUpdatedEvent(context.getSessionObject(),
					chat);
			context.getEventBus().fire(event);
		}

		return changed;
	}

}