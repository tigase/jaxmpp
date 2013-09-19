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

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Object for registering {@linkplain Listener listeners} and fire
 * {@linkplain BaseEvent events}.
 * 
 * <pre>
 * Observable observable = ObservableFactory.instance(null);
 * observable.addListener(ResourceBinderModule.ResourceBindSuccess, new Listener&lt;ResourceBinderModule.ResourceBindEvent&gt;() {
 * 	public void handleEvent(ResourceBindEvent be) {
 * 	}
 * });
 * observable.fireEvent(new ResourceBinderModule.ResourceBindEvent(ResourceBinderModule.ResourceBindSuccess));
 * </pre>
 * 
 * @author bmalkow
 * 
 */
@Deprecated
public interface Observable {

	/**
	 * Adds a listener bound by the given event type.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            the listener
	 */
	public void addListener(final EventType eventType, Listener<? extends BaseEvent> listener);

	/**
	 * Add a listener bound by the all event types.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(Listener<? extends BaseEvent> listener);

	/**
	 * Fires an event.
	 * 
	 * @param event
	 *            event
	 * @throws JaxmppException
	 */
	public void fireEvent(BaseEvent event) throws JaxmppException;

	/**
	 * Fires an event.
	 * 
	 * @param eventType
	 *            type of event
	 * @param event
	 *            event
	 */
	public void fireEvent(final EventType eventType, final BaseEvent event) throws JaxmppException;

	/**
	 * Fires {@linkplain BaseEvent BaseEvent}.
	 * 
	 * @param eventType
	 * @throws JaxmppException
	 */
	public void fireEvent(final EventType eventType, final SessionObject sessionObject) throws JaxmppException;

	/**
	 * Removes all listeners.
	 */
	public void removeAllListeners();

	/**
	 * Removes a listener.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            listener
	 */
	public void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener);

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 *            listener
	 */
	public void removeListener(Listener<? extends BaseEvent> listener);
}