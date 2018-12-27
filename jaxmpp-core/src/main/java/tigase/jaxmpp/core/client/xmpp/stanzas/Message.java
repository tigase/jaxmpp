/*
 * Message.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.EscapeUtils;

/**
 * Representation od Message stanza.
 */
public class Message
		extends Stanza {

	public static final Message create() throws JaxmppException {
		return createMessage();
	}

	protected Message(Element element) throws XMLException {
		super(element);
		if (!"message".equals(element.getName())) {
			throw new RuntimeException("Wrong element name: " + element.getName());
		}
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
	 * Sets body of message.
	 *
	 * @param body body of message
	 */
	public void setBody(String body) throws XMLException {
		setChildElementValue("body", body);
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
	 * Sets subject of message.
	 *
	 * @param subject subject of message
	 */
	public void setSubject(String subject) throws XMLException {
		setChildElementValue("subject", subject);
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
	 * Sets thread-id of message.
	 *
	 * @param thread thread-id
	 */
	public void setThread(String thread) throws XMLException {
		setChildElementValue("thread", thread);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StanzaType getType() throws XMLException {
		return super.getType(StanzaType.normal);
	}

}