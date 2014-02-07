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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

/**
 * Interface for writing to XMPP Stream.
 * 
 */
public interface PacketWriter {

	/**
	 * Write a element to Stream.
	 * 
	 * @param stanza
	 *            {@linkplain Element} to write
	 */
	void write(Element stanza) throws JaxmppException;

	/**
	 * Write a (IQ) stanza element to Stream and register callback with default
	 * timeout.
	 * 
	 * @param stanza
	 *            {@linkplain Element} to write
	 * @param asyncCallback
	 *            {@linkplain AsyncCallback} to register
	 */
	void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException;

	/**
	 * Write a (IQ) stanza element to Stream and register callback with given
	 * timeout.
	 * 
	 * @param stanza
	 *            stanza {@linkplain Element} to write
	 * @param timeout
	 *            time after which will be execute
	 *            {@linkplain AsyncCallback#onTimeout()}
	 * @param asyncCallback
	 *            asyncCallback {@linkplain AsyncCallback} to register
	 */
	void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException;

}