/*
 * DefaultRosterStore.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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

package tigase.jaxmpp.core.client.xmpp.modules.roster;

import tigase.jaxmpp.core.client.BareJID;

import java.util.*;

public class DefaultRosterStore
		extends RosterStore {

	protected final Set<String> groups = new HashSet<String>();

	protected final Map<BareJID, RosterItem> roster = new HashMap<BareJID, RosterItem>();

	@Override
	protected Set<String> addItem(RosterItem item) {
		item.setData(RosterItem.ID_KEY, createItemId(item.getJid()));
		synchronized (this.roster) {
			this.roster.put(item.getJid(), item);
		}

		final HashSet<String> addedGroups = new HashSet<String>();
		synchronized (this.groups) {
			for (String g : item.getGroups()) {
				if (!this.groups.contains(g)) {
					addedGroups.add(g);
				}
			}
			this.groups.addAll(addedGroups);
		}
		return addedGroups;
	}

	@Override
	protected Set<String> calculateModifiedGroups(final HashSet<String> groupsOld) {
		reloadGroups();
		HashSet<String> modifiedGroups = new HashSet<String>();

		Iterator<String> e = groupsOld.iterator();
		while (e.hasNext()) {
			String gg = e.next();
			if (!groups.contains(gg)) {
				modifiedGroups.add(gg);
			}
		}
		e = groups.iterator();
		while (e.hasNext()) {
			String gg = e.next();
			if (!groupsOld.contains(gg)) {
				modifiedGroups.add(gg);
			}
		}

		return modifiedGroups;
	}

	protected int createItemId(BareJID jid) {
		int id = (sessionObject.getUserBareJid() + "::" + jid).hashCode();
		return Math.abs(id);
	}

	/**
	 * Returns {@linkplain RosterItem} of given bare JID.
	 *
	 * @param jid bare JID.
	 *
	 * @return roster item.
	 */
	@Override
	public RosterItem get(BareJID jid) {
		synchronized (this.roster) {
			return this.roster.get(jid);
		}
	}

	/**
	 * Returns all roster items selected by selector.
	 *
	 * @param predicate selector.
	 *
	 * @return all matched roster items.
	 */
	@Override
	public List<RosterItem> getAll(final Predicate predicate) {
		ArrayList<RosterItem> result = new ArrayList<RosterItem>();
		synchronized (this.roster) {
			if (predicate == null) {
				result.addAll(this.roster.values());
			} else {
				for (RosterItem i : this.roster.values()) {
					if (predicate.match(i)) {
						result.add(i);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns number of roster items in storage.
	 *
	 * @return number of roster items in storage.
	 */
	@Override
	public int getCount() {
		return roster.size();
	}

	/**
	 * Get all known groups of buddies.
	 *
	 * @return collection of group names.
	 */
	@Override
	public Collection<? extends String> getGroups() {
		return Collections.unmodifiableCollection(this.groups);
	}

	void reloadGroups() {
		synchronized (groups) {
			groups.clear();
			for (RosterItem i : this.roster.values()) {
				groups.addAll(i.getGroups());
			}
		}
	}

	public void removeAll() {
		synchronized (this.roster) {
			roster.clear();
		}
		synchronized (this.groups) {
			groups.clear();
		}
	}

	@Override
	protected void removeItem(BareJID jid) {
		synchronized (this.roster) {
			this.roster.remove(jid);
		}
	}
}
