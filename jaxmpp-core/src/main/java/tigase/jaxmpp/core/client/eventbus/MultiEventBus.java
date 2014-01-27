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

import java.util.ArrayList;

/**
 * Special implementation of {@link EventBus}. It collects all events from all
 * registered EventBuses.
 */
public class MultiEventBus extends DefaultEventBus {

	private final ArrayList<EventBus> buses = new ArrayList<EventBus>();

	private final EventListener commonListener;

	public MultiEventBus() {
		this.commonListener = new EventListener() {

			@Override
			public void onEvent(Event<? extends EventHandler> event) {
				fire(event, event.getSource());
			}
		};
	}

	/**
	 * Adds {@link EventBus} to collector. All events from this {@link EventBus}
	 * will be dispatched to handlers registered in this {@link MultiEventBus}
	 * instance.
	 * 
	 * @param eventBus
	 *            {@link EventBus} to register.
	 */
	public synchronized void addEventBus(EventBus eventBus) {
		this.buses.add(eventBus);
		eventBus.addListener(commonListener);
	}

	/**
	 * Removes {@link EventBus} from collector.
	 * 
	 * @param eventBus
	 *            {@link EventBus} to remove.
	 */
	public synchronized void removeEventBus(EventBus eventBus) {
		eventBus.remove(commonListener);
		this.buses.remove(eventBus);
	}

}
