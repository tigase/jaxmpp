/*
 * AbstractIQModule.java
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

import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.logging.Level;

public abstract class AbstractIQModule
		extends AbstractStanzaExtendableModule<IQ> {

	@Override
	public void process(IQ stanza) throws JaxmppException {
		final StanzaType type = stanza.getType();

		if (stanza instanceof IQ && type == StanzaType.set) {
			processSet(stanza);
		} else if (stanza instanceof IQ && type == StanzaType.get) {
			processGet(stanza);
		} else {
			log.log(Level.WARNING,
					"Unhandled stanza " + stanza.getName() + ", type=" + stanza.getAttribute("type") + ", id=" +
							stanza.getAttribute("id"));
			throw new XMPPException(ErrorCondition.bad_request);
		}
	}

	/**
	 * Method for processing stanza <code>&lt;iq type='get'&gt;</code>.
	 *
	 * @param element incoming XMPP stanza
	 */
	protected abstract void processGet(IQ element) throws JaxmppException;

	/**
	 * Method for processing stanza <code>&lt;iq type='set'&gt;</code>.
	 *
	 * @param element incoming XMPP stanza
	 */
	protected abstract void processSet(IQ element) throws JaxmppException;

}