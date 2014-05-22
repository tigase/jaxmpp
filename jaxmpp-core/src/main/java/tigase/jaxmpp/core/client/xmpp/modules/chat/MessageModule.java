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

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

/**
 * Module to handle messages.
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>{@link MessageModule#ChatClosed ChatClosed}</b> : {@link MessageEvent
 * MessageEvent} ()<br>
 * <div>Fires when Chat object is destroyed (for example by calling method
 * {@linkplain MessageModule#close(Chat) close()})</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link MessageModule#ChatCreated ChatCreated}</b> :
 * {@link MessageEvent MessageEvent} ()<br>
 * <div>Fires when new Chat object is created. It will be called after receiving
 * new message from buddy and on execute
 * {@linkplain MessageModule#createChat(JID) createChat()}.</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link MessageModule#ChatUpdated ChatUpdated}</b> :
 * {@link MessageEvent MessageEvent} ()<br>
 * <div>Fires when some data in Chat object is changed. For example when JID is
 * changed (buddy changed resource) or threadid is setted.</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link MessageModule#MessageReceived MessageReceived}</b> :
 * {@link MessageEvent MessageEvent} ()<br>
 * <div>Fires when message is received.</div>
 * <ul>
 * </ul></dd>
 * 
 * </dl>
 * 
 * @author bmalkow
 * 
 */
public class MessageModule extends AbstractStanzaModule<Message> {

	public static abstract class AbstractMessageEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Message message;

		public AbstractMessageEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		/**
		 * Return received message.
		 * 
		 * @return message
		 */
		public Message getMessage() {
			return message;
		}

		public void setMessage(Message message) {
			this.message = message;
		}
	}

	public static class MessageEvent extends AbstractMessageEvent {

		private static final long serialVersionUID = 1L;

		private Chat chat;

		private String id;

		public MessageEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		/**
		 * Return chat related to message.
		 * 
		 * @return chat. May be <code>null</code>.
		 */
		public Chat getChat() {
			return chat;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		public void setChat(Chat chat) {
			this.chat = chat;
		}

		public void setId(String id) {
			this.id = id;
		}

	}

	public static final EventType ChatClosed = new EventType();

	public static final EventType ChatCreated = new EventType();

	public static final EventType ChatStateChanged = new EventType();

	public static final EventType ChatUpdated = new EventType();

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

	private static final String[] FEATURES = { ChatState.XMLNS, "urn:xmpp:receipts" };

	public static final EventType MessageReceived = new EventType();

	public static final EventType ReceiptReceivedMessage = new EventType();

	public static final String RECEIPTS_XMLNS = "urn:xmpp:receipts";

	private final AbstractChatManager chatManager;

	public MessageModule(Observable parentObservable, final SessionObject sessionObject, PacketWriter packetWriter) {
		super(ObservableFactory.instance(parentObservable), sessionObject, packetWriter);
		AbstractChatManager cm = UniversalFactory.createInstance(AbstractChatManager.class.getName());
		this.chatManager = cm != null ? cm : new DefaultChatManager();
		this.chatManager.setObservable(this.observable);
		this.chatManager.setPacketWriter(packetWriter);
		this.chatManager.setSessionObject(sessionObject);
		this.chatManager.initialize();
		if (this.sessionObject instanceof AbstractSessionObject) {
			((AbstractSessionObject) this.sessionObject).addListener(AbstractSessionObject.Cleared,
					new Listener<AbstractSessionObject.ClearedEvent>() {

						@Override
						public void handleEvent(AbstractSessionObject.ClearedEvent be) throws JaxmppException {
							chatManager.onSessionObjectCleared(be.getSessionObject(), be.getScopes());
						}
					});
		}
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
		return this.chatManager.createChat(jid);
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
		return ChatState.isChatStateDisabled(sessionObject) ? null : FEATURES;
	}

	Observable getObservable() {
		return observable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Message element) throws JaxmppException {
		processRequestReceipts(element);
		MessageEvent event = new MessageEvent(MessageReceived, sessionObject);
		event.setMessage(element);
		Chat chat = chatManager.process(element, observable);
		if (chat != null) {
			event.setChat(chat);
		}
		observable.fireEvent(event.getType(), event);
		processReceivedReceipts(element, chat);
	}

	private void processReceivedReceipts(final Message element, Chat chat) throws JaxmppException {
		Element receipt = element.getChildrenNS("received", MessageModule.RECEIPTS_XMLNS);
		if (receipt != null) {
			MessageEvent event = new MessageModule.MessageEvent(MessageModule.ReceiptReceivedMessage, sessionObject);
			event.setChat(chat);
			event.setMessage(element);
			event.setId(receipt.getAttribute("id"));
			observable.fireEvent(event.getType(), event);
		}
	}

	private void processRequestReceipts(final Message element) throws JaxmppException {
		if (element.getChildrenNS("request", MessageModule.RECEIPTS_XMLNS) != null) {
			Message msg = Message.create();
			msg.setId(UIDGenerator.next());
			msg.setTo(element.getFrom());

			Element received = new DefaultElement("received", null, MessageModule.RECEIPTS_XMLNS);
			if (element.getId() != null)
				received.setAttribute("id", element.getId());
			msg.addChild(received);

			writer.write(msg);
		}
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

		writer.write(msg);
	}

	public boolean setChatStateDisabled(boolean value) {
		if (value == ChatState.isChatStateDisabled(sessionObject))
			return false;
		sessionObject.setProperty(ChatState.CHAT_STATE_DISABLED_KEY, value);
		sessionObject.setProperty("XEP115VerificationString", null);
		if (value) {
			chatManager.resetChatStates();
		}
		return true;
	}

}