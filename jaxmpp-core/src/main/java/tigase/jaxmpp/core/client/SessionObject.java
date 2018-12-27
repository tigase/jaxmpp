/*
 * SessionObject.java
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

import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

import java.util.Set;

/**
 * Session object is for store state of connection, modules etc.
 * <p>
 * All states will be removed on reset ({@linkplain SessionObject#clear()
 * clear()} method}).
 *
 * @author bmalkow
 */
public interface SessionObject
		extends UserProperties {

	/**
	 * Name of property used to keep logical name of XMPP server. Usually it is
	 * equals to hostname of users JID.
	 */
	String DOMAIN_NAME = "domainName";

	/**
	 * Name of property used to keep users nickname
	 */
	String NICKNAME = "nickname";

	/**
	 * Name of property used to keep users password
	 */
	String PASSWORD = "password";

	/**
	 * Name of property used to keep XMPP resource
	 */
	String RESOURCE = "resource";

	/**
	 * Name of property used to keep logical name of XMPP server. Usually it is
	 * equals to hostname of users JID.
	 */
	@Deprecated
	String SERVER_NAME = "domainName";

	/**
	 * Name of property used to keep users JID
	 */
	String USER_BARE_JID = "userBareJid";

	enum Scope {
		session,
		stream,
		user
	}

	/**
	 * Reset state. Clears all properties stored by modules, roster and presence
	 * store. Users properties are keeped.
	 *
	 * @throws JaxmppException
	 */
	void clear() throws JaxmppException;

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
	 * @param key property name
	 *
	 * @return property
	 */
	<T> T getProperty(String key);

	/**
	 * Returns users JID
	 *
	 * @return
	 */
	BareJID getUserBareJid();

	/**
	 * Set property in given scope.
	 *
	 * @param scope scope of property
	 * @param key property name
	 * @param value property value. <code>null</code> to unset property.
	 *
	 * @return instance of <code>this</code> {@linkplain SessionObject}
	 */
	SessionObject setProperty(Scope scope, String key, Object value);

	/**
	 * Set property in {@linkplain Scope#session session} scope.
	 *
	 * @param key property name
	 * @param value property value. <code>null</code> to unset property.
	 *
	 * @return instance of <code>this</code> {@linkplain SessionObject}
	 */
	SessionObject setProperty(String key, Object value);

	/**
	 * Implemented by handlers of {@linkplain ClearedEvent}.
	 */
	interface ClearedHandler
			extends EventHandler {

		/**
		 * Called when {@link ClearedEvent} if fired.
		 *
		 * @param sessionObject cleared session object.
		 * @param scopes set cleared {@link Scope scopes}.
		 */
		void onCleared(SessionObject sessionObject, Set<Scope> scopes) throws JaxmppException;

		/**
		 * Fired when properties from {@link SessionObject} are cleared.
		 */
		class ClearedEvent
				extends JaxmppEvent<ClearedHandler> {

			private Set<Scope> scopes;

			public ClearedEvent(SessionObject sessionObject, Set<Scope> scopes) {
				super(sessionObject);
				this.scopes = scopes;
			}

			@Override
			public void dispatch(ClearedHandler handler) throws JaxmppException {
				handler.onCleared(sessionObject, scopes);
			}

			public Set<Scope> getScopes() {
				return scopes;
			}

			public void setScopes(Set<Scope> scopes) {
				this.scopes = scopes;
			}

		}
	}

}