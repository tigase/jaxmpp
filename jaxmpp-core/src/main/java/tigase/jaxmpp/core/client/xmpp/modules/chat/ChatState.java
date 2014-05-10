/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * 
 * @author andrzej
 */
public enum ChatState {
	
	active,
	inactive,
	gone,
	composing,
	paused;

	public static String XMLNS = "http://jabber.org/protocol/chatstates";	
	
	public Element toElement() throws XMLException	 {
		Element elem  = new DefaultElement(name());
		elem.setXMLNS(XMLNS);
		return elem;
	}
	
	public static ChatState fromElement(Element elem) throws XMLException {
		if (!XMLNS.equals(elem.getXMLNS()))
			return null;
		return ChatState.valueOf(elem.getName());
	} 
	
}
