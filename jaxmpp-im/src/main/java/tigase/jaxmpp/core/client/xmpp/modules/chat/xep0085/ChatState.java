/*
 * ChatState.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.chat.xep0085;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * @author andrzej
 */
public enum ChatState {

	active,
	inactive,
	gone,
	composing,
	paused;

	public static String XMLNS = "http://jabber.org/protocol/chatstates";

	public static ChatState fromElement(Element elem) throws XMLException {
		if (!XMLNS.equals(elem.getXMLNS())) {
			return null;
		}
		return ChatState.valueOf(elem.getName());
	}

	public Element toElement() throws XMLException {
		Element elem = ElementFactory.create(name());
		elem.setXMLNS(XMLNS);
		return elem;
	}

}
