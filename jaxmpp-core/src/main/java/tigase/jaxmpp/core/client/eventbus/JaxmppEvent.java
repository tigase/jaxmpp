/*
 * JaxmppEvent.java
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
package tigase.jaxmpp.core.client.eventbus;

import tigase.jaxmpp.core.client.SessionObject;

/**
 * Event object used by JaXMPP Library.
 *
 * @param <H> type of handler.
 */
public abstract class JaxmppEvent<H extends EventHandler>
		extends Event<H> {

	protected final SessionObject sessionObject;

	/**
	 * Constructs event object.
	 *
	 * @param sessionObject session object.
	 */
	protected JaxmppEvent(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	/**
	 * Return {@link SessionObject session object}.
	 *
	 * @return session object.
	 */
	public SessionObject getSessionObject() {
		return sessionObject;
	}

}
