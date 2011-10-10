package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;

public class RosterStore {

	static interface Handler {

		void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback) throws XMLException,
				JaxmppException;

		void remove(BareJID jid) throws XMLException, JaxmppException;

		void update(RosterItem item) throws XMLException, JaxmppException;
	}

	public static interface Predicate {
		boolean match(RosterItem item);
	}

	protected final Set<String> groups = new HashSet<String>();

	private Handler handler;

	protected final Map<BareJID, RosterItem> roster = new HashMap<BareJID, RosterItem>();

	public void add(BareJID jid, String name, AsyncCallback asyncCallback) throws XMLException, JaxmppException {
		add(jid, name, new ArrayList<String>(), asyncCallback);

	}

	public void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback) throws XMLException,
			JaxmppException {
		if (this.handler != null)
			this.handler.add(jid, name, groups, asyncCallback);
	}

	public void add(BareJID jid, String name, String[] groups, AsyncCallback asyncCallback) throws XMLException,
			JaxmppException {
		ArrayList<String> x = new ArrayList<String>();
		if (groups != null)
			for (String string : groups) {
				x.add(string);
			}
		add(jid, name, x, asyncCallback);
	}

	Set<String> addItem(RosterItem item) {
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

	Set<String> calculateModifiedGroups(final HashSet<String> groupsOld) {
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

	public void cler() {
		synchronized (this.roster) {
			roster.clear();
		}
	}

	public RosterItem get(BareJID jid) {
		synchronized (this.roster) {
			return this.roster.get(jid);
		}
	}

	public List<RosterItem> getAll() {
		return getAll(null);
	}

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

	public Collection<? extends String> getGroups() {
		return this.groups;
	}

	void reloadGroups() {
		groups.clear();
		for (RosterItem i : this.roster.values()) {
			groups.addAll(i.getGroups());
		}
	}

	public void remove(BareJID jid) throws XMLException, JaxmppException {
		if (handler != null)
			this.handler.remove(jid);
	}

	void removeItem(BareJID jid) {
		synchronized (this.roster) {
			this.roster.remove(jid);
		}
	}

	void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void update(RosterItem item) throws XMLException, JaxmppException {
		if (this.handler != null)
			this.handler.update(item);

	}

}
