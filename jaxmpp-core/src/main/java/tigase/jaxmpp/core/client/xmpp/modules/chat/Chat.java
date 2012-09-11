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
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Chat {

	public static final EventType MessageReceived = new EventType();

	private boolean closed = false;

	private final long id;

	private JID jid;

	private final SessionObject sessionObject;

	private String threadId;

	private final PacketWriter writer;

	public Chat(long id, PacketWriter packetWriter, SessionObject sessionObject) {
		this.id = id;
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public long getId() {
		return id;
	}

	public JID getJid() {
		return jid;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
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

	public void setJid(JID jid) {
		this.jid = jid;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

}