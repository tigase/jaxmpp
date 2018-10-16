/*
 * XMucUserElement.java
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
package tigase.jaxmpp.core.client.xmpp.modules.muc;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XMucUserElement
		extends ElementWrapper {

	private final Set<Integer> statuses = new HashSet<Integer>();

	public static XMucUserElement extract(Stanza stanza) throws XMLException {
		final Element x = stanza == null ? null : stanza.getChildrenNS("x", "http://jabber.org/protocol/muc#user");
		if (x == null) {
			return null;
		}
		return new XMucUserElement(x);
	}

	private XMucUserElement(Element element) throws XMLException {
		super(element);
		fillStatuses();
	}

	private void fillStatuses() throws XMLException {
		List<Element> sts = getChildren("status");
		if (sts != null) {
			for (Element s : sts) {
				String v = s.getAttribute("code");
				if (v != null) {
					statuses.add(Integer.parseInt(v));
				}
			}
		}
	}

	public Affiliation getAffiliation() throws XMLException {
		Element item = getFirstChild("item");
		if (item == null) {
			return null;
		}
		String tmp = item.getAttribute("affiliation");
		return tmp == null ? null : Affiliation.valueOf(tmp);
	}

	public JID getJID() throws XMLException {
		Element item = getFirstChild("item");
		if (item == null) {
			return null;
		}
		String tmp = item.getAttribute("jid");
		return tmp == null ? null : JID.jidInstance(tmp);
	}

	public String getNick() throws XMLException {
		Element item = getFirstChild("item");
		if (item == null) {
			return null;
		}
		String tmp = item.getAttribute("nick");
		return tmp;
	}

	public Role getRole() throws XMLException {
		Element item = getFirstChild("item");
		if (item == null) {
			return null;
		}
		String tmp = item.getAttribute("role");
		return tmp == null ? null : Role.valueOf(tmp);
	}

	public Set<Integer> getStatuses() {
		return statuses;
	}

}