/*
 * PEPExtension.java
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
package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;

/**
 * @author Wojciech Kapcia <wojciech.kapcia@tigase.org>
 */
public class PEPExtension
		implements Extension, ContextAware {

	private static final String XMLNS_GEOLOC = "http://jabber.org/protocol/geoloc";
	private static final String XMLNS_MOOD = "http://jabber.org/protocol/mood";
	private static final String XMLNS_TUNE = "http://jabber.org/protocol/tune";
	private static final String XMLNS_NOTIFY = "+notify";
	private static final String[] FEATURES = {XMLNS_GEOLOC, XMLNS_GEOLOC + XMLNS_NOTIFY, XMLNS_MOOD,
											  XMLNS_MOOD + XMLNS_NOTIFY, XMLNS_TUNE, XMLNS_TUNE + XMLNS_NOTIFY,};

	@Override
	public Element afterReceive(Element received) throws JaxmppException {
		return received;
	}

	@Override
	public Element beforeSend(Element received) throws JaxmppException {
		return received;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	public void setContext(Context context) {
	}

}
