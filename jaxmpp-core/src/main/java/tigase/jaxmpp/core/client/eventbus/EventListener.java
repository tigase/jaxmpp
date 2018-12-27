/*
 * EventListener.java
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
package tigase.jaxmpp.core.client.eventbus;

/**
 * Interface for listeners.<br/>
 * Listener is special viariant of handler. Instead of invoking method
 * {@linkplain Event#dispatch(EventHandler) dispatch()},
 * {@linkplain EventListener#onEvent(Event) onEvent()} will be invoked.
 */
public interface EventListener
		extends EventHandler {

	/**
	 * Method called when event is fired.
	 *
	 * @param event fired event.
	 */
	void onEvent(Event<? extends EventHandler> event);

}
