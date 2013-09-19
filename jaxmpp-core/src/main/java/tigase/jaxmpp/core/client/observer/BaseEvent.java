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
package tigase.jaxmpp.core.client.observer;

import java.io.Serializable;

import tigase.jaxmpp.core.client.SessionObject;

/**
 * Base class for all events in Jaxmpp.
 * 
 * @author bmalkow
 * 
 */
@Deprecated
public class BaseEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean handled;

	private final SessionObject sessionObject;

	private final EventType type;

	public BaseEvent(EventType type, SessionObject sessionObject) {
		this.type = type;
		this.sessionObject = sessionObject;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	/**
	 * Returns the type of event.
	 * 
	 * @return the event type
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Returns <code>true</code> if event was handled by any listener.
	 * 
	 * @return <code>true</code> if event was handled
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * <code>true</code> if event was handled by listener. This method is called
	 * by {@linkplain Observable Observable}
	 * 
	 * @param b
	 */
	public void setHandled(boolean b) {
		this.handled = b;

	}

}