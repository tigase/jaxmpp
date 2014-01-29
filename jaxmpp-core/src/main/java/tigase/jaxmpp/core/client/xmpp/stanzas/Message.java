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

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.EscapeUtils;

/**
 * Representation od Message stanza.
 * 
 */
public class Message extends Stanza {

	public static final Message create() throws XMLException {
		return new Message(new DefaultElement("message"));
	}

	public Message(Element element) throws XMLException {
		super(element);
		if (!"message".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	/**
	 * Return message body.
	 * 
	 * @return message body.
	 */
	public String getBody() throws XMLException {
		return EscapeUtils.unescape(getChildElementValue("body"));
	}

	/**
	 * Returns subject of message.
	 * 
	 * @return subject of message
	 */
	public String getSubject() throws XMLException {
		return getChildElementValue("subject");
	}

	/**
	 * Returns thread-id.
	 * 
	 * @return thread-id
	 */
	public String getThread() throws XMLException {
		return getChildElementValue("thread");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StanzaType getType() throws XMLException {
		return super.getType(StanzaType.normal);
	}

	/**
	 * Sets body of message.
	 * 
	 * @param body
	 *            body of message
	 */
	public void setBody(String body) throws XMLException {
		setChildElementValue("body", body);
	}

	/**
	 * Sets subject of message.
	 * 
	 * @param subject
	 *            subject of message
	 */
	public void setSubject(String subject) throws XMLException {
		setChildElementValue("subject", subject);
	}

	/**
	 * Sets thread-id of message.
	 * 
	 * @param thread
	 *            thread-id
	 */
	public void setThread(String thread) throws XMLException {
		setChildElementValue("thread", thread);
	}

}