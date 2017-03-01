/*
 * XmppDelay.java
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
package tigase.jaxmpp.core.client.xmpp.utils.delay;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

import java.util.Date;

/**
 * Wrapper for <code>urn:xmpp:delay</code> elements. See <a
 * href='http://xmpp.org/extensions/xep-0203.html'>XEP-203</a> for details.
 */
public class XmppDelay
		extends ElementWrapper {

	public static XmppDelay extract(Stanza stanza) throws XMLException {
		final Element x = stanza.getChildrenNS("delay", "urn:xmpp:delay");
		if (x == null) {
			return null;
		}
		return new XmppDelay(x);
	}

	private XmppDelay(Element element) throws XMLException {
		super(element);
	}

	public JID getFrom() throws XMLException {
		String tmp = getAttribute("from");
		return tmp == null ? null : JID.jidInstance(tmp);
	}

	public Date getStamp() throws XMLException {
		String tmp = getAttribute("stamp");
		DateTimeFormat dtf = new DateTimeFormat();
		return dtf.parse(tmp);
	}
}