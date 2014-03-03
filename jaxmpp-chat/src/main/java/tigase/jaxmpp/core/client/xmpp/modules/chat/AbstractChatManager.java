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

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public abstract class AbstractChatManager {

	protected Context context;

	protected AbstractChatManager() {
	}

	public abstract boolean close(Chat chat) throws JaxmppException;

	public abstract Chat createChat(final JID fromJid, final String threadId) throws JaxmppException;

	public abstract Chat getChat(JID jid, String threadId);

	public abstract List<Chat> getChats();

	public Context getContext() {
		return context;
	}

	protected void initialize() {
	}

	public abstract boolean isChatOpenFor(final BareJID jid);

	public void setContext(Context context) {
		this.context = context;
	}

}