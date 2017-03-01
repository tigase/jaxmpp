/*
 * PresenceStore.java
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
package tigase.jaxmpp.core.client.xmpp.modules.presence;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.Property;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;

import java.util.Iterator;
import java.util.Map;

/**
 * Storage for keep received presences of buddies.
 */
public abstract class PresenceStore
		implements Property {

	protected Map<BareJID, Presence> bestPresence;
	protected Handler handler;
	protected Map<JID, Presence> presenceByJid;
	protected Map<BareJID, Map<String, Presence>> presencesMapByBareJid;

	/**
	 * Removes all known presence information.
	 */
	public void clear() throws JaxmppException {
		clear(true);
	}

	void clear(boolean notify) throws JaxmppException {
		presenceByJid.clear();

		if (notify) {
			Iterator<Presence> it = bestPresence.values().iterator();
			while (it.hasNext()) {
				Presence i = it.next();
				it.remove();
				handler.onOffline(i);
			}
		} else {
			bestPresence.clear();
		}
		presencesMapByBareJid.clear();
	}

	protected abstract Map<String, Presence> createResourcePresenceMap();

	/**
	 * Returns presence stanza with highest priority of goven bare JID.
	 *
	 * @param jid JID of sender
	 *
	 * @return {@linkplain Presence} stanza or <code>null</code> if not found.
	 */
	public Presence getBestPresence(final BareJID jid) throws XMLException {
		return this.bestPresence.get(jid);
	}

	/**
	 * Returns presence stanza of given JID.
	 *
	 * @param jid JID of sender
	 *
	 * @return {@linkplain Presence} stanza or <code>null</code> if not found.
	 */
	public Presence getPresence(final JID jid) {
		return this.presenceByJid.get(jid);
	}

	/**
	 * Returns map of all known resources and related presences stanza of given
	 * bare JID.
	 *
	 * @param jid basre JID of sender
	 *
	 * @return map contains resource (key) and related presence stanza (value).
	 */
	public Map<String, Presence> getPresences(BareJID jid) {
		return this.presencesMapByBareJid.get(jid);
	}

	@Override
	public Class<PresenceStore> getPropertyClass() {
		return PresenceStore.class;
	}

	private Presence intGetBestPresence(final BareJID jid) throws XMLException {
		Map<String, Presence> resourcesPresence = this.presencesMapByBareJid.get(jid);
		Presence result = null;
		if (resourcesPresence != null) {
			Iterator<Presence> it = resourcesPresence.values().iterator();
			while (it.hasNext()) {
				Presence x = it.next();
				Integer p = x.getPriority();
				if (result == null || p >= result.getPriority() && x.getType() == null) {
					result = x;
				}
			}
		}
		return result;
	}

	public boolean isAvailable(BareJID jid) throws XMLException {
		Map<String, Presence> resourcesPresence = this.presencesMapByBareJid.get(jid);
		boolean result = false;
		if (resourcesPresence != null) {
			Iterator<Presence> it = resourcesPresence.values().iterator();
			while (it.hasNext() && !result) {
				Presence x = it.next();
				result = result | x.getType() == null;
			}
		}
		return result;
	}

	void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void setPresence(Show show, String status, Integer priority) throws JaxmppException {
		this.handler.setPresence(show, status, priority);
	}

	protected void update(final Presence presence) throws XMLException {
		final JID from = presence.getFrom();
		if (from == null) {
			return;
		}
		final BareJID bareFrom = from.getBareJid();
		final String resource = from.getResource() == null ? "" : from.getResource();

		this.presenceByJid.put(from, presence);
		Map<String, Presence> m = this.presencesMapByBareJid.get(bareFrom);
		if (m == null) {
			m = createResourcePresenceMap();
			this.presencesMapByBareJid.put(bareFrom, m);
		}
		m.put(resource, presence);
		updateBestPresence(presence);
	}

	protected void updateBestPresence(final Presence presence) throws XMLException {
		final BareJID bareFrom = presence.getFrom().getBareJid();
		this.bestPresence.put(bareFrom, intGetBestPresence(bareFrom));

		// Presence x = this.bestPresence.get(bareFrom);
		// if (x == null) {
		// this.bestPresence.put(bareFrom, intGetBestPresence(bareFrom));
		// } else {
		// if (presence.getPriority() > x.getPriority() && presence.getType() ==
		// null) {
		// this.bestPresence.put(bareFrom, x);
		// } else {
		// this.bestPresence.put(bareFrom, intGetBestPresence(bareFrom));
		// }
		// }
	}

	interface Handler {

		void onOffline(Presence i) throws JaxmppException;

		void setPresence(Show show, String status, Integer priority) throws JaxmppException;

	}
}