/*
 * XMLException.java
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
package tigase.jaxmpp.core.client.xml;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Exception for XML errors.
 *
 * @author Mads Randstoft
 */
public class XMLException
		extends JaxmppException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of <code>XMLException</code> without detail
	 * message.
	 */
	public XMLException() {
	}

	/**
	 * Constructs an instance of <code>XMLException</code> with the specified
	 * detail message.
	 *
	 * @param msg the detail message.
	 */
	public XMLException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an instance of <code>XMLException</code> with the specified
	 * detail message and cause.
	 *
	 * @param msg the detail message.
	 * @param cause the cause.
	 */
	public XMLException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructs an instance of <code>XMLException</code> with the specified
	 * cause.
	 *
	 * @param cause the cause.
	 */
	public XMLException(Throwable cause) {
		super(cause);
	}
}