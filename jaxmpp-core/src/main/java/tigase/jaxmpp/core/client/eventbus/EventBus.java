/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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
package tigase.jaxmpp.core.client.eventbus;

/**
 * Dispatches events to all registered handlers and listeners.
 */
public abstract class EventBus {

	/**
	 * Adds handler to receive given type of events.
	 *
	 * @param type    type of event.
	 * @param handler event handler
	 */
	public abstract <H extends EventHandler> void addHandler(Class<? extends Event<H>> type, H handler);

	/**
	 * Adds listener to receive given type of events.
	 *
	 * @param type     type of event.
	 * @param listener event listener.
	 */
	public abstract <H extends EventHandler> void addListener(Class<? extends Event<H>> type, EventListener listener);


	/**
	 * Adds listener to receive all types events.
	 *
	 * @param listener event listener.
	 */
	public abstract <H extends EventHandler> void addListener(EventListener listener);

	/**
	 * Fires event.
	 *
	 * @param e event to fire
	 */
	public abstract void fire(Event<?> e);


	/**
	 * Removes listener or handler of given type.
	 *
	 * @param type    type of event.
	 * @param handler handler or listener to remove from EventBus.
	 */
	public abstract void remove(Class<? extends Event<?>> type, EventHandler handler);


	/**
	 * Removed listener or handler.
	 *
	 * @param handler handler or listener to remove from EventBus.
	 */
	public abstract void remove(EventHandler handler);


}
