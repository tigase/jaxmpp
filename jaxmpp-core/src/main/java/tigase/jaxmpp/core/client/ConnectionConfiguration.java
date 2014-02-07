/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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

import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.CredentialsCallback;

/**
 * Connection configuration object.
 * 
 * It is wrapper around {@linkplain SessionObject}.
 * 
 */
public abstract class ConnectionConfiguration {

	protected final SessionObject sessionObject;

	protected ConnectionConfiguration(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	/**
	 * Set credentials callback;
	 * 
	 * @param credentialsCallback
	 *            callback
	 */
	public void setCredentialsCallback(CredentialsCallback credentialsCallback) {
		sessionObject.setUserProperty(AuthModule.CREDENTIALS_CALLBACK, credentialsCallback);
	}

	/**
	 * Set logical name of XMPP server. Usually it is equals to hostname of
	 * users JID and is set automatically.
	 * 
	 * @param domainName
	 *            logical name of XMPP server.
	 */
	public void setDomain(String domainName) {
		sessionObject.setUserProperty(SessionObject.DOMAIN_NAME, domainName);
	}

	/**
	 * Set XMPP resource.
	 * 
	 * @param resource
	 *            resource
	 */
	public void setResource(String resource) {
		sessionObject.setUserProperty(SessionObject.RESOURCE, resource);
	}

	/**
	 * Set users JabberID.
	 * 
	 * @param jid
	 *            JabberID
	 */
	public void setUserJID(BareJID jid) {
		sessionObject.setUserProperty(SessionObject.USER_BARE_JID, jid);
	}

	/**
	 * Set users JabberID.
	 * 
	 * @param jid
	 *            JabberID
	 */
	public void setUserJID(String jid) {
		setUserJID(BareJID.bareJIDInstance(jid));
	}

	/**
	 * Set users password.
	 * 
	 * @param password
	 *            password. If <code>null</code> then ANONYMOUS authentication
	 *            will be used.
	 */
	public void setUserPassword(String password) {
		sessionObject.setUserProperty(SessionObject.PASSWORD, password);
	}

}