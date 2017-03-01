/*
 * AbstractRoomsManager.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.muc;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRoomsManager {

	protected final Map<BareJID, Room> rooms = new HashMap<BareJID, Room>();
	protected Context context;
	protected SessionObject sessionObject;

	public boolean contains(BareJID roomJid) {
		return this.rooms.containsKey(roomJid);
	}

	protected abstract Room createRoomInstance(final BareJID roomJid, final String nickname, final String password);

	public Room get(BareJID roomJid) {
		return this.rooms.get(roomJid);
	}

	public Collection<Room> getRooms() {
		return this.rooms.values();
	}

	SessionObject getSessionObject() {
		return sessionObject;
	}

	void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	protected void initialize() {

	}

	public void register(Room room) {
		this.rooms.put(room.getRoomJid(), room);
	}

	public boolean remove(Room room) {
		return this.rooms.remove(room.getRoomJid()) != null;
	}

	public void setContext(Context context) {
		this.context = context;
		this.setSessionObject(context.getSessionObject());
	}
}