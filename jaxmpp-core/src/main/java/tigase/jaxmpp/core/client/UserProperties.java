/*
 * UserProperties.java
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
package tigase.jaxmpp.core.client;

/**
 * Interface to keep user properties.
 * <p>
 * User properties are not removed on state reset.
 *
 * @author bmalkow
 */
public interface UserProperties {

	/**
	 * Get user property.
	 *
	 * @param key property name
	 *
	 * @return property or <code>null</code> if property isn't set.
	 */
	<T> T getUserProperty(String key);

	/**
	 * Set user property.
	 *
	 * @param key property name
	 * @param value property value. <code>null</code> to unset property
	 *
	 * @return instance of <code>this</code> {@linkplain UserProperties}
	 */
	UserProperties setUserProperty(String key, Object value);

}