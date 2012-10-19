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

import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public abstract class AbstractIQModule implements XmppModule {

	protected final Logger log;

	protected final Observable observable;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public AbstractIQModule(Observable observable, SessionObject sessionObject, PacketWriter packetWriter) {
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
	public void process(Element $element) throws JaxmppException {
		final Stanza stanza = Stanza.create($element);
		final StanzaType type = stanza.getType();

		if (stanza instanceof IQ && type == StanzaType.set)
			processSet((IQ) stanza);
		else if (stanza instanceof IQ && type == StanzaType.get)
			processGet((IQ) stanza);
		else {
			log.log(Level.WARNING, "Unhandled stanza " + $element.getName() + ", type=" + $element.getAttribute("type")
					+ ", id=" + $element.getAttribute("id"));
			throw new XMPPException(ErrorCondition.bad_request);
		}
	}

	protected abstract void processGet(IQ element) throws JaxmppException;

	protected abstract void processSet(IQ element) throws JaxmppException;

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