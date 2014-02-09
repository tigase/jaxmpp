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

/**
 * Interface for implement right {@linkplain Chat} based on sender JID and
 * thread id.
 */
public interface ChatSelector {

	/**
	 * Selects chat from list based on specified JID and thread id.
	 * 
	 * @param chats
	 *            list of chats.
	 * @param jid
	 *            sender JID
	 * @param threadId
	 *            thread-id
	 * @return chat or <code>null</code> if not matched.
	 */
	Chat getChat(List<Chat> chats, JID jid, String threadId);

}