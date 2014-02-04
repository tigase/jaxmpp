/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Representation of IQ stanza.
 * 
 */
public class IQ extends Stanza {

	public static final IQ create() throws JaxmppException {
		return Stanza.createIQ();
	}

	IQ(Element element) throws XMLException {
		super(element);
		if (!"iq".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	/**
	 * Return &lt;query/&gt; child element.
	 * 
	 * @return <code>null</code> is &lt;query/&gt; doesn't exists
	 */
	public Element getQuery() throws XMLException {
		List<Element> q = this.getChildren("query");
		return q != null && q.size() > 0 ? q.get(0) : null;
	}

}