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
package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.List;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.eventbus.Event;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventListener;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaModule<T extends Stanza> implements XmppModule, InitializingModule {

	protected static Element getFirstChild(Element element, String elementName) throws XMLException {
		List<Element> elements = element.getChildren(elementName);
		return elements == null || elements.size() == 0 ? null : elements.get(0);
	}

	protected Context context;

	protected final Logger log;

	public AbstractStanzaModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	public AbstractStanzaModule(Context context) {
		log = Logger.getLogger(this.getClass().getName());
		this.context = context;
	}

	public <H extends EventHandler> void addListener(Class<? extends Event<H>> type, EventListener listener) {
		context.getEventBus().addListener(type, listener);
	}

	public <H extends EventHandler> void addListener(EventListener listener) {
		context.getEventBus().addListener(listener);
	}

	@Override
	public void afterRegister() {
	}

	@Override
	public void beforeRegister() {
		if (context == null)
			throw new RuntimeException("Context cannot be null");
	}

	@Override
	public void beforeUnregister() {
	}

	protected void fireEvent(Event<?> event) {
		context.getEventBus().fire(event, this);
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
	 * @param element
	 *            incoming stanza
	 */
	public abstract void process(T stanza) throws JaxmppException;

	public void remove(Class<? extends Event<?>> type, EventHandler handler) {
		context.getEventBus().remove(type, handler);
	}

	public void remove(EventHandler handler) {
		context.getEventBus().remove(handler);
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