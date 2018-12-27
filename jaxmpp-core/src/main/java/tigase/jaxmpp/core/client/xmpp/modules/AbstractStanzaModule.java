/*
 * AbstractStanzaModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.eventbus.Event;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractStanzaModule<T extends Stanza>
		implements XmppModule, InitializingModule, ContextAware {

	protected final Logger log;
	protected Context context;

	protected static Element getFirstChild(Element element, String elementName) throws XMLException {
		List<Element> elements = element.getChildren(elementName);
		return elements == null || elements.size() == 0 ? null : elements.get(0);
	}

	public AbstractStanzaModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	@Override
	public void afterRegister() {
	}

	@Override
	public void beforeRegister() {
		if (context == null) {
			throw new RuntimeException("Context cannot be null");
		}
	}

	@Override
	public void beforeUnregister() {
	}

	protected void fireEvent(Event<?> event) {
		context.getEventBus().fire(event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void process(Element element) throws JaxmppException {
		final T stanza = (T) Stanza.create(element);
		process(stanza);
	}

	/**
	 * Method for processing incoming stanza.
	 *
	 * @param element incoming stanza
	 */
	public abstract void process(T stanza) throws JaxmppException;

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	protected void write(Element stanza) throws JaxmppException {
		context.getWriter().write(stanza);
	}

	protected void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException {
		context.getWriter().write(stanza, asyncCallback);
	}

	protected void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		context.getWriter().write(stanza, timeout, asyncCallback);
	}

}