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
package tigase.jaxmpp.gwt.client;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;

/**
 * Connection configuration object.
 */
public class ConnectionConfiguration extends tigase.jaxmpp.core.client.ConnectionConfiguration {

	ConnectionConfiguration(SessionObject sessionObject) {
		super(sessionObject);
	}

	/**
	 * Set BOSH Service URL.
	 * 
	 * @param boshService
	 *            BOSH service URL
	 */
	public void setBoshService(String boshService) {
		sessionObject.setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, boshService);

	}
}