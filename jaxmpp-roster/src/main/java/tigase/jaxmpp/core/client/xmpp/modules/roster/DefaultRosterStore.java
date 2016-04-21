package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.*;

import tigase.jaxmpp.core.client.BareJID;

public class DefaultRosterStore extends RosterStore {

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
	 * @param jid
	 *            bare JID.
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
	 * @param predicate
	 *            selector.
	 * @return all matched roster items.
	 */
	@Override
	public List<RosterItem> getAll(final Predicate predicate) {
		ArrayList<RosterItem> result = new ArrayList<RosterItem>();
		synchronized (this.roster) {
			if (predicate == null)
				result.addAll(this.roster.values());
			else
				for (RosterItem i : this.roster.values()) {
					if (predicate.match(i))
						result.add(i);
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
