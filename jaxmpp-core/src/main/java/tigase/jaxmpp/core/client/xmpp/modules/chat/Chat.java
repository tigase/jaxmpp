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
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Representation chat with specified interlocutor.
 */
public class Chat {

	public static final EventType MessageReceived = new EventType();

	private final long id;

	private JID jid;

	private final SessionObject sessionObject;

	private String threadId;

	private final PacketWriter writer;
	
	private ChatState localState = null;

	private ChatState state = null;
	
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

	/**
	 * Return {@linkplain SessionObject} related to this chat.
	 * 
	 * @return {@linkplain SessionObject} related to this chat.
	 */
	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public ChatState getState() {
		return state;
	}
	
	protected void setState(ChatState state) {
		this.state = state;
	}
	
	protected ChatState getLocalState() {
		return localState;
	}
	
	public void setLocalState(ChatState state) throws XMLException, JaxmppException {
		if (state == null) {
			this.localState = null;
			return;
		}
		if (!state.equals(this.localState) && (localState != null || state != ChatState.gone)) {
			this.localState = state;
			sendState(state);
		}
	}
	
	private void sendState(ChatState state) throws XMLException, JaxmppException {
		// we need to check if recipient is online as there is no point in sending
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
	 * Returns thread-id.
	 * 
	 * @return thread-id or <code>null</code> if not present.
	 */
	public String getThreadId() {
		return threadId;
	}

	/**
	 * Sends message in current chat. It uses correct interlocutor JID and
	 * thread-id.
	 * 
	 * @param body
	 *            message to send.
	 */
	public void sendMessage(String body) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setTo(jid);
		msg.setType(StanzaType.chat);
		msg.setThread(threadId);
		msg.setBody(body);

		if (localState != null) {
			msg.addChild(ChatState.active.toElement());
			localState = ChatState.active;
		}
		
		this.writer.write(msg);
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