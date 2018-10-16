/*
 * Occupant.java
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

import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

public class Occupant {

	private static long counter = 0;

	private Affiliation cacheAffiliation;

	private Role cacheRole;

	private long id;

	private Presence presence;

	public Occupant() {
		id = ++counter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Occupant)) {
			return false;
		}
		return ((Occupant) obj).id == id;
	}

	public Affiliation getAffiliation() {
		try {
			if (cacheAffiliation == null) {
				final XMucUserElement xUser = XMucUserElement.extract(presence);
				if (xUser != null) {
					cacheAffiliation = xUser.getAffiliation();
				}
			}
			return cacheAffiliation == null ? Affiliation.none : cacheAffiliation;
		} catch (XMLException e) {
			return Affiliation.none;
		}
	}

	public String getNickname() throws XMLException {
		return this.presence.getFrom().getResource();
	}

	public Presence getPresence() {
		return presence;
	}

	public void setPresence(Presence presence) {
		cacheAffiliation = null;
		cacheRole = null;
		this.presence = presence;
	}

	public Role getRole() {
		try {
			if (cacheRole == null) {
				final XMucUserElement xUser = XMucUserElement.extract(presence);
				if (xUser != null) {
					cacheRole = xUser.getRole();
				}
			}
			return cacheRole == null ? Role.none : cacheRole;
		} catch (XMLException e) {
			return Role.none;
		}
	}

	@Override
	public int hashCode() {
		return ("occupant" + id).hashCode();
	}

}