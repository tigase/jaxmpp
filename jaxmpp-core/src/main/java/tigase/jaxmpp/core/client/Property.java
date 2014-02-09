/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2014 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
 * Base interface for classes which may be stored in JaxmppCore as "properties"
 * by using methods set and get on JaxmppCore instance.
 * 
 * @author andrzej
 */
public interface Property {
	
	/**
	 * Method which returns class which implements this interface which 
	 * may be used as a key to store instance of this class in JaxmppCore.
	 * 
	 * @return 
	 */
	Class<? extends Property> getPropertyClass();
	
}
