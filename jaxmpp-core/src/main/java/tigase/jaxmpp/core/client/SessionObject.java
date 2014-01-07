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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Session object is for store state of connection, modules etc.
 * 
 * All states will be removed on reset ({@linkplain SessionObject#clear()
 * clear()} method}).
 * 
 * @author bmalkow
 * 
 */
public interface SessionObject extends UserProperties {

	public static enum Scope {
		session,
		stream,
		user;
	}

	/**
	 * Name of property used to keep logical name of XMPP server. Usually it is
	 * equals to hostname of users JID.
	 */
	public static final String DOMAIN_NAME = "domainName";

	/**
	 * Name of property used to keep users nickname
	 */
	public static final String NICKNAME = "nickname";

	/**
	 * Name of property used to keep users password
	 */
	public static final String PASSWORD = "password";

	/**
	 * Name of property used to keep XMPP resource
	 */
	public static final String RESOURCE = "resource";

	/**
	 * Name of property used to keep logical name of XMPP server. Usually it is
	 * equals to hostname of users JID.
	 */
	@Deprecated
	public static final String SERVER_NAME = "domainName";

	/**
	 * Name of property used to keep users JID
	 */
	public static final String USER_BARE_JID = "userBareJid";

	/**
	 * Method for process <code><iq/></code> stanzas without response.
	 * 
	 * @throws JaxmppException
	 */
	public void checkHandlersTimeout() throws JaxmppException;

	/**
	 * Reset state. Clears all properties stored by modules, roster and presence
	 * store. Users properties are keeped.
	 * 
	 * @throws JaxmppException
	 */
	public void clear() throws JaxmppException;

	/**
	 * Reset state. Clears given properties stored by modules. Roster and
	 * presence store will be cleared if {@linkplain Scope#session} is in
	 * parameters.
	 * 
	 * @throws JaxmppException
	 */
	void clear(Scope... scopes) throws JaxmppException;

	/**
	 * Returns property
	 * 
	 * @param key
	 *            property name
	 * @return property
	 */
	public <T> T getProperty(String key);

	/**
	 * Returns users JID
	 * 
	 * @return
	 */
	public BareJID getUserBareJid();

	/**
	 * Set property in given scope.
	 * 
	 * @param scope
	 *            scope of property
	 * @param key
	 *            property name
	 * @param value
	 *            property value. <code>null</code> to unset property.
	 * @return instance of <code>this</code> {@linkplain SessionObject}
	 */
	public SessionObject setProperty(Scope scope, String key, Object value);

	/**
	 * Set property in {@linkplain Scope#session session} scope.
	 * 
	 * @param key
	 *            property name
	 * @param value
	 *            property value. <code>null</code> to unset property.
	 * 
	 * @return instance of <code>this</code> {@linkplain SessionObject}
	 */
	public SessionObject setProperty(String key, Object value);

}