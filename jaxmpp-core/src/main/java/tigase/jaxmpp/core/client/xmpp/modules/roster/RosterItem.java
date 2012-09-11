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
package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.DataHolder;
import tigase.jaxmpp.core.client.SessionObject;

public class RosterItem {

	public static enum Subscription {
		both(true, true),
		from(true, false),
		none(false, false),
		remove(false, false),
		to(false, true);

		private final boolean sFrom;

		private final boolean sTo;

		private Subscription(boolean statusFrom, boolean statusTo) {
			this.sFrom = statusFrom;
			this.sTo = statusTo;
		}

		public boolean isFrom() {
			return this.sFrom;
		}

		public boolean isTo() {
			return this.sTo;
		}
	}

	public static final String ID_KEY = "id";

	private boolean ask;

	private final DataHolder dataHolder = new DataHolder();

	private final ArrayList<String> groups = new ArrayList<String>();

	private final BareJID jid;

	private String name;

	private final SessionObject sessionObject;

	private Subscription subscription;

	public RosterItem(BareJID jid, SessionObject sessionObject) {
		this.jid = jid;
		this.sessionObject = sessionObject;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RosterItem))
			return false;
		return ((RosterItem) obj).jid.equals(this.jid);
	}

	public <T> T getData(String key) {
		return dataHolder.getData(key);
	}

	public ArrayList<String> getGroups() {
		return groups;
	}

	public long getId() {
		Long x = getData(ID_KEY);
		return x == null ? 0 : x;
	}

	public BareJID getJid() {
		return jid;
	}

	public String getName() {
		return name;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	@Override
	public int hashCode() {
		return jid.hashCode();
	}

	public boolean isAsk() {
		return ask;
	}

	public <T> T removeData(String key) {
		return dataHolder.removeData(key);
	}

	public void setAsk(boolean ask) {
		this.ask = ask;
	}

	public void setData(String key, Object value) {
		dataHolder.setData(key, value);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

	@Override
	public String toString() {
		return "RosterItem [" + name + " <" + jid.toString() + ">]";
	}

}