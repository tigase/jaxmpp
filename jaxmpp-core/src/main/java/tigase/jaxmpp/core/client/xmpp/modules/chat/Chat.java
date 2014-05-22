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

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Representation chat with specified interlocutor.
 */
public class Chat {

	public static final EventType MessageReceived = new EventType();

	private ChatState chatState = null;

	private final long id;

	private JID jid;

	private ChatState localChatState = null;

	private boolean messageDeliveryReceiptsEnabled = false;

	private final SessionObject sessionObject;

	private String threadId;

	private final PacketWriter writer;

	/**
	 * Creates new chat representation object.
	 * 
	 * @param id
	 *            internal object identifier.
	 * @param packetWriter
	 *            packet writer related to chat.
	 * @param sessionObject
	 *            session object related to chat.
	 */
	public Chat(long id, PacketWriter packetWriter, SessionObject sessionObject) {
		this.id = id;
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public ChatState getChatState() {
		return chatState;
	}

	/**
	 * Return internal identifier of chat object.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns interlocutor JID.
	 * 
	 * @return interlocutor JID.
	 */
	public JID getJid() {
		return jid;
	}

	protected ChatState getLocalChatState() {
		return localChatState;
	}

	/**
	 * Return {@linkplain SessionObject} related to this chat.
	 * 
	 * @return {@linkplain SessionObject} related to this chat.
	 */
	public SessionObject getSessionObject() {
		return sessionObject;
	}

	/**
	 * Returns thread-id.
	 * 
	 * @return thread-id or <code>null</code> if not present.
	 */
	public String getThreadId() {
		return threadId;
	}

	/**
	 * @return the messageDeliveryReceiptsEnabled
	 */
	public boolean isMessageDeliveryReceiptsEnabled() {
		return messageDeliveryReceiptsEnabled;
	}

	private void sendChatState(ChatState state) throws XMLException, JaxmppException {
		// we need to check if recipient is online as there is no point in
		// sending
		// state change notifications to offline users
		PresenceStore presenceStore = sessionObject.getPresence();
		if (presenceStore == null || !presenceStore.isAvailable(jid.getBareJid()))
			return;
		Message msg = Message.create();
		msg.setTo(jid);
		msg.setType(StanzaType.chat);
		msg.addChild(state.toElement());

		this.writer.write(msg);
	}

	/**
	 * Sends message in current chat. It uses correct interlocutor JID and
	 * thread-id.
	 * 
	 * @param body
	 *            message to send.
	 */
	public Message sendMessage(String body) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setTo(jid);
		msg.setType(StanzaType.chat);
		msg.setThread(threadId);
		msg.setId(UIDGenerator.next());
		msg.setBody(body);

		if (localChatState != null) {
			msg.addChild(ChatState.active.toElement());
			localChatState = ChatState.active;
		}

		if (messageDeliveryReceiptsEnabled) {
			msg.addChild(new DefaultElement("request", null, MessageModule.RECEIPTS_XMLNS));
		}

		this.writer.write(msg);
		return msg;
	}

	protected void setChatState(ChatState state) {
		this.chatState = state;
	}

	/**
	 * Sets interlocutor JID.
	 * 
	 * @param jid
	 *            interlocutor JID.
	 */
	public void setJid(JID jid) {
		this.jid = jid;
	}

	public void setLocalChatState(ChatState state) throws XMLException, JaxmppException {
		if (ChatState.isChatStateDisabled(sessionObject)) {
			if (localChatState != null) {
				sendChatState(ChatState.active);
				localChatState = null;
			}
			return;
		}
		if (state == null) {
			this.localChatState = null;
			return;
		}
		if (!state.equals(this.localChatState) && (localChatState != null || state != ChatState.gone)) {
			this.localChatState = state;
			sendChatState(state);
		}
	}

	/**
	 * @param messageDeliveryReceiptsEnabled
	 *            the messageDeliveryReceiptsEnabled to set
	 */
	public void setMessageDeliveryReceiptsEnabled(boolean messageDeliveryReceiptsEnabled) {
		this.messageDeliveryReceiptsEnabled = messageDeliveryReceiptsEnabled;
	}

	/**
	 * Sets thread-id.
	 * 
	 * @param threadId
	 *            thread-id.
	 */
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

}