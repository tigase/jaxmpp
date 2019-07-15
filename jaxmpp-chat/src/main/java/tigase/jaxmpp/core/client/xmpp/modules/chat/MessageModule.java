/*
 * MessageModule.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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

import tigase.jaxmpp.core.client.BareJID;
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
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaExtendableModule;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Module to handle messages.
 */
public class MessageModule
		extends AbstractStanzaExtendableModule<Message> {

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

	private MucModule mucModule;

	private final static Message wrap(Element e) throws JaxmppException {
		if (e == null) {
			return null;
		} else if (e instanceof Message) {
			return (Message) e;
		} else {
			return (Message) Message.create(e);
		}
	}

	public MessageModule() {
		AbstractChatManager cm = UniversalFactory.createInstance(AbstractChatManager.class.getName());
		this.chatManager = cm != null ? cm : new DefaultChatManager();
	}

	public MessageModule(AbstractChatManager chatManager) {
		this.chatManager = chatManager;
	}

	@Override
	public void afterRegister() {
		super.afterRegister();
		this.mucModule = context.getModuleProvider().getModule(MucModule.class);
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
	 * @param chat chat object
	 */
	public void close(Chat chat) throws JaxmppException {
		chatManager.close(chat);
	}

	/**
	 * Creates new chat object.
	 *
	 * @param jid destination JID
	 *
	 * @return chat object
	 */
	public Chat createChat(JID jid) throws JaxmppException {
		return this.chatManager.createChat(jid, generateThreadID());
	}

	public Chat createChatInstance(Message message, final JID interlocutorJid) throws JaxmppException {
		Chat chat = chatManager.createChat(interlocutorJid, message.getThread());
		fireEvent(new ChatCreatedHandler.ChatCreatedEvent(context.getSessionObject(), chat, message));
		return chat;
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
		return getFeaturesWithExtensions(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Message message) throws JaxmppException {
		final JID interlocutorJid = message.getFrom();
		if (isMessageHandledByMUC(interlocutorJid)) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Message from " + interlocutorJid + " skipped, because this is MUC Room");
			}
			return;
		}
		process(message, interlocutorJid, true);
	}

	/**
	 * Sends message in passed chat. It uses correct interlocutor JID and
	 * thread-id.
	 *
	 * @param body message to send.
	 *
	 * @return
	 */
	public Message sendMessage(Chat chat, String body) throws JaxmppException {
		Message msg = chat.createMessage(body);
		return wrap(write(msg));
	}

	/**
	 * Sends message in passed chat.
	 *
	 * @param msg message stanza to send.
	 *
	 * @return
	 */
	public Message sendMessage(Message msg) throws JaxmppException {
		return wrap(write(msg));
	}

	/**
	 * Sends message in passed chat. It uses correct interlocutor JID and
	 * thread-id.
	 *
	 * @param body message to send.
	 *
	 * @return
	 */
	public Message sendMessage(Chat chat, String body, List<? extends Element> additionalElems) throws JaxmppException {
		Message msg = chat.createMessage(body);
		if (additionalElems != null) {
			for (Element child : additionalElems) {
				msg.addChild(child);
			}
		}
		return wrap(write(msg));
	}

	/**
	 * Sends message. It does not create chat object.
	 *
	 * @param toJID recipient's JID
	 * @param subject subject of message
	 * @param message message
	 */
	public Message sendMessage(JID toJID, String subject, String message) throws JaxmppException {
		Message msg = Message.create();
		msg.setSubject(subject);
		msg.setBody(message);
		msg.setTo(toJID);
		msg.setId(UIDGenerator.next());

		return wrap(write(msg));
	}

	public Message writeMessage(Message msg) throws JaxmppException {
		return wrap(write(msg));	}

	Chat process(Message message, final JID interlocutorJid, final boolean fireReceivedEvent) throws JaxmppException {
		if (message.getType() != StanzaType.chat && message.getType() != StanzaType.error &&
				message.getType() != StanzaType.headline) {
			if (message.getType() == StanzaType.groupchat) {
				return null;
			}
			message = executeBeforeMessageProcess(message, null);
			if (message != null && fireReceivedEvent) {
				fireEvent(new MessageReceivedHandler.MessageReceivedEvent(context.getSessionObject(), message, null));
			}
			return null;
		}

		final String threadId = message.getThread();

		Chat chat = chatManager.getChat(interlocutorJid, threadId);

		if (chat == null && message.getBody() == null) {
			// no chat, not body. Lets skip it.
			if (fireReceivedEvent) {
				fireEvent(new MessageReceivedHandler.MessageReceivedEvent(context.getSessionObject(), message, null));
			}
			return null;
		}

		if (chat == null) {
			chat = chatManager.createChat(interlocutorJid, threadId);
			chat.setMessageModule(this);
			fireEvent(new ChatCreatedHandler.ChatCreatedEvent(context.getSessionObject(), chat, message));
		} else {
			update(chat, interlocutorJid, threadId);
		}

		message = executeBeforeMessageProcess(message, chat);

		if (message != null && fireReceivedEvent) {
			fireEvent(new MessageReceivedHandler.MessageReceivedEvent(context.getSessionObject(), message, chat));
		}

		return chat;
	}

	protected Message executeBeforeMessageProcess(final Message element, Chat chat) {
		Iterator<Extension> it = getExtensionChain().getExtension().iterator();
		Message e = element;
		while (it.hasNext() && e != null) {
			Extension x = it.next();
			if (x instanceof MessageModuleExtension) {
				try {
					e = ((MessageModuleExtension) x).beforeMessageProcess(element, chat);
				} catch (Exception ex) {
					log.warning("Problem on calling executeBeforeMessageProcess: " + ex.getMessage());
				}
			}
		}
		return e;
	}

	protected String generateThreadID() {
		return UIDGenerator.next();
	}

	protected boolean isMessageHandledByMUC(JID from) {
		if (this.mucModule == null || from == null) {
			return false;
		}

		final BareJID roomJid = from.getBareJid();
		boolean result = mucModule.isRoomRegistered(roomJid);
		return result;

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
			ChatUpdatedHandler.ChatUpdatedEvent event = new ChatUpdatedHandler.ChatUpdatedEvent(
					context.getSessionObject(), chat);
			context.getEventBus().fire(event);
		}

		return changed;
	}

	public interface ChatClosedHandler
			extends EventHandler {

		void onChatClosed(SessionObject sessionObject, Chat chat);

		class ChatClosedEvent
				extends JaxmppEvent<ChatClosedHandler> {

			private Chat chat;

			public ChatClosedEvent(SessionObject sessionObject, Chat chat) {
				super(sessionObject);
				this.chat = chat;
			}

			@Override
			public void dispatch(ChatClosedHandler handler) {
				handler.onChatClosed(sessionObject, chat);
			}

			public Chat getChat() {
				return chat;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

		}
	}

	public interface ChatCreatedHandler
			extends EventHandler {

		void onChatCreated(SessionObject sessionObject, Chat chat, Message message);

		class ChatCreatedEvent
				extends JaxmppEvent<ChatCreatedHandler> {

			private Chat chat;

			private Message message;

			public ChatCreatedEvent(SessionObject sessionObject, Chat chat, Message message) {
				super(sessionObject);
				this.chat = chat;
				this.message = message;
			}

			@Override
			public void dispatch(ChatCreatedHandler handler) {
				handler.onChatCreated(sessionObject, chat, message);
			}

			public Chat getChat() {
				return chat;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

			public Message getMessage() {
				return message;
			}

			public void setMessage(Message message) {
				this.message = message;
			}

		}
	}

	public interface ChatUpdatedHandler
			extends EventHandler {

		void onChatUpdated(SessionObject sessionObject, Chat chat);

		class ChatUpdatedEvent
				extends JaxmppEvent<ChatUpdatedHandler> {

			private Chat chat;

			public ChatUpdatedEvent(SessionObject sessionObject, Chat chat) {
				super(sessionObject);
				this.chat = chat;
			}

			@Override
			public void dispatch(ChatUpdatedHandler handler) {
				handler.onChatUpdated(sessionObject, chat);
			}

			public Chat getChat() {
				return chat;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

		}
	}

	public interface MessageReceivedHandler
			extends EventHandler {

		void onMessageReceived(SessionObject sessionObject, Chat chat, Message stanza);

		class MessageReceivedEvent
				extends JaxmppEvent<MessageReceivedHandler> {

			private final Chat chat;

			private final Message stanza;

			public MessageReceivedEvent(SessionObject sessionObject, Message stanza, Chat chat) {
				super(sessionObject);
				this.stanza = stanza;
				this.chat = chat;
			}

			@Override
			public void dispatch(MessageReceivedHandler handler) {
				handler.onMessageReceived(sessionObject, chat, stanza);
			}

			public Chat getChat() {
				return chat;
			}

			public Message getStanza() {
				return stanza;
			}

		}
	}

}