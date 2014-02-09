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
package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Base interface for implementing own ad-hoc commands.
 * 
 * @author bmalkow
 * 
 */
public interface AdHocCommand {

	/**
	 * Returns features what are implemented by command.
	 * 
	 * @return array of features
	 */
	String[] getFeatures();

	/**
	 * Return human readable name of command.
	 * 
	 * @return name of command
	 */
	String getName();

	/**
	 * Return node name of command.
	 * 
	 * @return node name
	 */
	String getNode();

	/**
	 * Main method to handle ad-hoc requests.
	 * 
	 * @param request
	 *            ad-hoc command request
	 * @param response
	 *            response
	 */
	void handle(AdHocRequest request, AdHocResponse response) throws JaxmppException;

	/**
	 * This method allows to authorization sender.
	 * 
	 * @param jid
	 *            JID of method caller.
	 * @return <code>true</code> if jid is allowed to call command.
	 */
	boolean isAllowed(JID jid);

}