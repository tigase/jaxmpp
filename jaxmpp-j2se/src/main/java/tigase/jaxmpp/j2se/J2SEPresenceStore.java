/*
 * J2SEPresenceStore.java
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

package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class J2SEPresenceStore
		extends PresenceStore {

	public J2SEPresenceStore() {
		presencesMapByBareJid = new ConcurrentHashMap<BareJID, Map<String, Presence>>();
		presenceByJid = new ConcurrentHashMap<JID, Presence>();
		bestPresence = new ConcurrentHashMap<BareJID, Presence>();
	}

	@Override
	protected Map<String, Presence> createResourcePresenceMap() {
		return new ConcurrentHashMap<String, Presence>();
	}

}
