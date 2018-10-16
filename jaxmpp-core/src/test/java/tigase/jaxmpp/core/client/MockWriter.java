/*
 * MockWriter.java
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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

import java.util.ArrayList;

public class MockWriter
		implements PacketWriter {

	private final ArrayList<Element> elements = new ArrayList<Element>();

	private MockSessionObject sessionObject;

	public MockWriter(MockSessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	public Element poll() {
		if (elements.size() == 0) {
			return null;
		}
		return elements.remove(0);
	}

	@Override
	public void write(Element stanza) {
		elements.add(stanza);
	}

	@Override
	public void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException {
		ResponseManager.registerResponseHandler(sessionObject, stanza, null, asyncCallback);
		write(stanza);
	}

	@Override
	public void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		ResponseManager.registerResponseHandler(sessionObject, stanza, timeout, asyncCallback);
		write(stanza);
	}

}