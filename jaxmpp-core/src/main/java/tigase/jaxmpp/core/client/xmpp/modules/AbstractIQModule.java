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

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.eventbus.Event;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public abstract class AbstractIQModule implements XmppModule {

	protected final Logger log;

	protected final Context context;

	protected void fireEvent(Event<?> event) {
		context.getEventBus().fire(event, this);
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

	public AbstractIQModule(Context context) {
		log = Logger.getLogger(this.getClass().getName());
		this.context = context;
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

	/**
	 * Method for processing stanza <code>&lt;iq type='get'&gt;</code>.
	 * 
	 * @param element
	 *            incoming XMPP stanza
	 */
	protected abstract void processGet(IQ element) throws JaxmppException;

	/**
	 * Method for processing stanza <code>&lt;iq type='set'&gt;</code>.
	 * 
	 * @param element
	 *            incoming XMPP stanza
	 */
	protected abstract void processSet(IQ element) throws JaxmppException;

}