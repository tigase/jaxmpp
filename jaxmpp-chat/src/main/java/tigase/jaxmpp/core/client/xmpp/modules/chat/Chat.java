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

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Representation chat with specified interlocutor.
 */
public class Chat {

	private final Context context;

	private final long id;

	private JID jid;

	private MessageModule messageModule;

	private String threadId;

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
	public Chat(long id, Context context) {
		this.id = id;
		this.context = context;
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
	 * Sets interlocutor JID.
	 *
	 * @param jid
	 *            interlocutor JID.
	 */
	public void setJid(JID jid) {
		this.jid = jid;
	}

	/**
	 * Return {@linkplain SessionObject} related to this chat.
	 *
	 * @return {@linkplain SessionObject} related to this chat.
	 */
	public SessionObject getSessionObject() {
		return context.getSessionObject();
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
	 * Sets thread-id.
	 *
	 * @param threadId
	 *            thread-id.
	 */
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	/**
	 * Sends message in current chat. It uses correct interlocutor JID and
	 * thread-id.
	 *
	 * @param body
	 *            message to send.
	 * @return
	 */
	public Message createMessage(String body) throws JaxmppException {
		Message msg = Message.create();
		msg.setTo(jid);
		msg.setType(StanzaType.chat);
		msg.setThread(threadId);
		msg.setBody(body);
		msg.setId(UIDGenerator.next());

		// this would make it impossible for Extensions to process this message!
		// this.context.getWriter().write(msg);
		return msg;
	}

	public void setMessageModule(MessageModule module) {
		this.messageModule = module;
	}

}