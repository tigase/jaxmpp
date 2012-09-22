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
package tigase.jaxmpp.core.client.xmpp.stanzas;

import java.util.List;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

public class ErrorElement extends ElementWrapper {

	public static ErrorElement extract(Element stanza) throws XMLException {
		final List<Element> xs = stanza.getChildren("error");
		if (xs == null || xs.size() == 0)
			return null;
		return new ErrorElement(xs.get(0));
	}

	private ErrorElement(Element element) throws XMLException {
		super(element);
	}

	public String getCode() throws XMLException {
		return getAttribute("code");
	}

	public ErrorCondition getCondition() throws XMLException {
		List<Element> cs = getChildrenNS("urn:ietf:params:xml:ns:xmpp-stanzas");
		for (Element element : cs) {
			ErrorCondition r = ErrorCondition.getByElementName(element.getName());
			if (r != null)
				return r;
		}
		return null;
	}

	public String getText() throws XMLException {
		Element e = getChildrenNS("text", "urn:ietf:params:xml:ns:xmpp-stanzas");
		if (e != null)
			return e.getValue();
		else
			return null;
	}

	public String getType() throws XMLException {
		return getAttribute("type");
	}
}