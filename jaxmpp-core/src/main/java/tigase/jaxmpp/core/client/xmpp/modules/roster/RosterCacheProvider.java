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

import java.util.Collection;

import tigase.jaxmpp.core.client.SessionObject;

/**
 * Interface for implement roster cache. For example to store roster on clients
 * machine.
 */
public interface RosterCacheProvider {

	/**
	 * Returns version of cached roster.
	 * 
	 * @param sessionObject
	 *            session object
	 * @return version id
	 */
	String getCachedVersion(SessionObject sessionObject);

	/**
	 * Loads cached roster.
	 * 
	 * @param sessionObject
	 * 
	 * @return collection of loaded roster items.
	 */
	Collection<RosterItem> loadCachedRoster(SessionObject sessionObject);

	/**
	 * Update roster cache. {@linkplain RosterStore} should be get from session
	 * object.
	 * 
	 * @param sessionObject
	 *            session object.
	 * @param ver
	 *            version of roster.
	 */
	void updateReceivedVersion(SessionObject sessionObject, String ver);

}