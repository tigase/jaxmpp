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
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

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
	 * Reset state. Clears all properties stored by modules. Users properties
	 * are keeped.
	 * 
	 * @throws JaxmppException
	 */
	public void clear() throws JaxmppException;

	/**
	 * Returns users JID binded on server.
	 * 
	 * @return Jabber ID
	 */
	public JID getBindedJid();

	/**
	 * Returns Store of known presences.
	 * 
	 * @return presence store
	 */
	public PresenceStore getPresence();

	/**
	 * Returns property
	 * 
	 * @param key
	 *            property name
	 * @return property
	 */
	public <T> T getProperty(String key);

	/**
	 * Returns roster store
	 * 
	 * @return roster
	 */
	public RosterStore getRoster();

	/**
	 * Returns XMPP Stream features
	 * 
	 * @return element with features
	 */
	public Element getStreamFeatures();

	/**
	 * Returns users JID
	 * 
	 * @return
	 */
	public BareJID getUserBareJid();

	/**
	 * Set property.
	 * 
	 * @param key
	 *            property name
	 * @param value
	 *            property value. <code>null</code> to unset property.
	 */
	public void setProperty(String key, Object value);

	/**
	 * Set XMPP Stream features
	 * 
	 * @param element
	 *            element contains features
	 */
	public void setStreamFeatures(Element element);

}