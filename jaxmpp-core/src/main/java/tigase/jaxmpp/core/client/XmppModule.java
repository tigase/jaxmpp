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

import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Main interface for implement modules. Module is stateless!. To store any
 * statefull data use {@linkplain SessionObject SessionObject}
 * 
 * @author bmalkow
 * 
 */
public interface XmppModule {

	/**
	 * Criteria
	 * 
	 * @return
	 */
	Criteria getCriteria();

	/**
	 * <p>
	 * Returns features what are implemented by Module.
	 * </p>
	 * <p>
	 * See <a href="http://xmpp.org/registrar/disco-features.html">Service
	 * Discovery Features</a>
	 * </p>
	 * 
	 * @return array of features
	 */
	String[] getFeatures();

	/**
	 * Main method of module. Module will process incoming stanza by call this
	 * method.
	 * 
	 * @param element
	 *            incoming XMPP stanza
	 * @param sessionObject
	 *            XMPP session object
	 * @param packetWriter
	 *            XML writer
	 */
	void process(Element element) throws XMPPException, XMLException, JaxmppException;

}