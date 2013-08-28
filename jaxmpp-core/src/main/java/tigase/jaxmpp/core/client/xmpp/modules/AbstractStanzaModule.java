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

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaModule<T extends Stanza> implements XmppModule {

	protected static Element getFirstChild(Element element, String elementName) throws XMLException {
		List<Element> elements = element.getChildren(elementName);
		return elements == null || elements.size() == 0 ? null : elements.get(0);
	}

	protected final Logger log;
	protected final Observable observable;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public AbstractStanzaModule(Observable observable, SessionObject sessionObject, PacketWriter packetWriter) {
		log = Logger.getLogger(this.getClass().getName());
		this.observable = observable;
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	/**
	 * Adds a listener bound by the given event type.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            the listener
	 */
	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	/**
	 * Add a listener bound by the all event types.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
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

	/**
	 * Removes all listeners.
	 */
	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	/**
	 * Removes a listener.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            listener
	 */
	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 *            listener
	 */
	public void removeListener(Listener<? extends BaseEvent> listener) {
		observable.removeListener(listener);
	}

}