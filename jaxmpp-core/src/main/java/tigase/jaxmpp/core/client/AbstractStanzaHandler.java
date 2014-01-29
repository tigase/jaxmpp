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
 * Abstract class for handling incoming XMPP Stanzas.
 */
public abstract class AbstractStanzaHandler implements Runnable {

	private final Context context;

	protected final Element element;

	public AbstractStanzaHandler(Element element, Context context) {
		super();
		this.element = element;
		this.context = context;
	}

	protected abstract void process() throws JaxmppException;

	@Override
	public void run() {
		try {
			process();
		} catch (Exception e) {
			Element errorResult = Processor.createError(element, e);
			if (errorResult != null)
				try {
					context.getWriter().write(errorResult);
				} catch (JaxmppException e1) {
					throw new RuntimeException("Can't send error stanza", e1);
				}
		}
	}
}