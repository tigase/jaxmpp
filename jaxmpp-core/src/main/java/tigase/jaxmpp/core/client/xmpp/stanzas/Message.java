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

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.EscapeUtils;

public class Message extends Stanza {

	public static final Message create() throws XMLException {
		return new Message(new DefaultElement("message"));
	}

	public Message(Element element) throws XMLException {
		super(element);
		if (!"message".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	public String getBody() throws XMLException {
		return EscapeUtils.unescape(getChildElementValue("body"));
	}

	public String getSubject() throws XMLException {
		return getChildElementValue("subject");
	}

	public String getThread() throws XMLException {
		return getChildElementValue("thread");
	}

	@Override
	public StanzaType getType() throws XMLException {
		return super.getType(StanzaType.normal);
	}

	public void setBody(String body) throws XMLException {
		setChildElementValue("body", body);
	}

	public void setSubject(String subject) throws XMLException {
		setChildElementValue("subject", subject);
	}

	public void setThread(String thread) throws XMLException {
		setChildElementValue("thread", thread);
	}

}