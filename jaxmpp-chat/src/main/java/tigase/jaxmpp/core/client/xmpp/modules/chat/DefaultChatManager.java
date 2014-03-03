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

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

class DefaultChatManager extends AbstractChatManager {

	private static long chatIds = 1;

	protected final ArrayList<Chat> chats = new ArrayList<Chat>();

	protected Context context;

	DefaultChatManager() {
		super();
	}

	@Override
	public boolean close(Chat chat) throws JaxmppException {
		boolean x = this.chats.remove(chat);
		if (x) {
			MessageModule.ChatClosedHandler.ChatClosedEvent event = new MessageModule.ChatClosedHandler.ChatClosedEvent(
					context.getSessionObject(), chat);
			context.getEventBus().fire(event);
		}
		return x;
	}

	@Override
	public Chat createChat(JID jid, String threadId) throws JaxmppException {
		Chat chat = new Chat(++chatIds, context);
		chat.setJid(jid);
		chat.setThreadId(threadId);

		this.chats.add(chat);

		MessageModule.ChatCreatedHandler.ChatCreatedEvent event = new MessageModule.ChatCreatedHandler.ChatCreatedEvent(
				context.getSessionObject(), chat, null);

		context.getEventBus().fire(event);

		return chat;
	}

	protected Chat findChat(final JID jid, final String threadId) {
		Chat chat = null;

		BareJID bareJID = jid.getBareJid();

		for (Chat c : chats) {
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

	protected Chat findChatByBareJID(final JID jid, final String threadId) {
		Chat chat = null;

		BareJID bareJID = jid.getBareJid();

		for (Chat c : chats) {
			if (c.getJid().getBareJid().equals(bareJID)) {
				chat = c;
				break;
			}
		}
		return chat;
	}

	@Override
	public Chat getChat(JID jid, String threadId) {
		return findChat(jid, threadId);
	}

	@Override
	public List<Chat> getChats() {
		return this.chats;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	protected void initialize() {
	}

	@Override
	public boolean isChatOpenFor(final BareJID jid) {
		for (Chat chat : this.chats) {
			if (chat.getJid().getBareJid().equals(jid))
				return true;
		}
		return false;
	}

}