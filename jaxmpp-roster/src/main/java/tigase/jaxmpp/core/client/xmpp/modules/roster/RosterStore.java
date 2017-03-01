/*
 * RosterStore.java
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
package tigase.jaxmpp.core.client.xmpp.modules.roster;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Property;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

import java.util.*;

/**
 * Storage for keeping roster.
 */
public abstract class RosterStore
		implements Property {

	protected SessionObject sessionObject;
	private Handler handler;

	/**
	 * Adds new contact to roster.
	 *
	 * @param jid JID of buddy
	 * @param name name of buddy
	 * @param asyncCallback callback
	 */
	public void add(BareJID jid, String name, AsyncCallback asyncCallback) throws JaxmppException {
		add(jid, name, new ArrayList<String>(), asyncCallback);
	}

	/**
	 * Adds new contact to roster.
	 *
	 * @param jid JID of buddy
	 * @param name name of buddy
	 * @param groups collection of groups name
	 * @param asyncCallback callback
	 */
	public void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback)
			throws JaxmppException {
		if (this.handler != null) {
			this.handler.add(jid, name, groups, asyncCallback);
		}
	}

	/**
	 * Adds new contact to roster.
	 *
	 * @param jid JID of buddy
	 * @param name name of buddy
	 * @param groups array of groups name
	 * @param asyncCallback callback
	 */
	public void add(BareJID jid, String name, String[] groups, AsyncCallback asyncCallback) throws JaxmppException {
		ArrayList<String> x = new ArrayList<String>();
		if (groups != null) {
			for (String string : groups) {
				x.add(string);
			}
		}
		add(jid, name, x, asyncCallback);
	}

	protected abstract Set<String> addItem(RosterItem item);

	protected abstract Set<String> calculateModifiedGroups(final HashSet<String> groupsOld);

	/**
	 * Clears storage.
	 */
	public void clear() {
		removeAll();
		if (this.handler != null) {
			handler.cleared();
		}
	}

	/**
	 * Returns {@linkplain RosterItem} of given bare JID.
	 *
	 * @param jid bare JID.
	 *
	 * @return roster item.
	 */
	public abstract RosterItem get(BareJID jid);

	/**
	 * Returns all buddies from roster.
	 *
	 * @return all roster items.
	 */
	public List<RosterItem> getAll() {
		return getAll(null);
	}

	/**
	 * Returns all roster items selected by selector.
	 *
	 * @param predicate selector.
	 *
	 * @return all matched roster items.
	 */
	public abstract List<RosterItem> getAll(final Predicate predicate);

	/**
	 * Returns number of roster items in storage.
	 *
	 * @return number of roster items in storage.
	 */
	public abstract int getCount();

	/**
	 * Get all known groups of buddies.
	 *
	 * @return collection of group names.
	 */
	public abstract Collection<? extends String> getGroups();

	@Override
	public Class<RosterStore> getPropertyClass() {
		return RosterStore.class;
	}

	/**
	 * Removes buddy from roster.
	 *
	 * @param jid jid of buddy to remove.
	 */
	public void remove(BareJID jid) throws JaxmppException {
		if (handler != null) {
			this.handler.remove(jid);
		}
	}

	public abstract void removeAll();

	protected abstract void removeItem(BareJID jid);

	void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	/**
	 * Sends changed RosterItem to server.
	 *
	 * @param item changed roster item.
	 */
	public void update(RosterItem item) throws JaxmppException {
		if (this.handler != null) {
			this.handler.update(item);
		}

	}

	interface Handler {

		void add(BareJID jid, String name, Collection<String> groups, AsyncCallback asyncCallback)
				throws JaxmppException;

		void cleared();

		void remove(BareJID jid) throws JaxmppException;

		void update(RosterItem item) throws JaxmppException;
	}

	public interface Predicate {

		boolean match(RosterItem item);
	}
}