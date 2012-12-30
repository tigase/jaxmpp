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
package tigase.jaxmpp.core.client.xmpp.modules.auth;

import tigase.jaxmpp.core.client.SessionObject;

/**
 * Interface for implementing SASL mechanisms.
 */
public interface SaslMechanism {

	/**
	 * Evaluating challenge received from server.
	 * 
	 * @param input
	 *            received data
	 * @param sessionObject
	 *            current {@linkplain SessionObject}
	 * @return calculated response
	 */
	String evaluateChallenge(String input, SessionObject sessionObject);

	/**
	 * This method is used to check if mechanism can be used with current
	 * session. For example if no username and passowrd is stored in
	 * sessionObject, then PlainMechanism can't be used.
	 * 
	 * @param sessionObject
	 *            current {@linkplain SessionObject}
	 * @return <code>true</code> if mechanism can be used it current XMPP
	 *         session.
	 */
	boolean isAllowedToUse(SessionObject sessionObject);

	/**
	 * Return mechanism name.
	 * 
	 * @return mechanism name.
	 */
	String name();

}