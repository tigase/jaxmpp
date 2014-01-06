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
package tigase.jaxmpp.j2se.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.Event;
import tigase.jaxmpp.core.client.eventbus.EventHandler;

public class ThreadSafeEventBus extends DefaultEventBus {

	@Override
	protected List<EventHandler> createHandlersArray() {
		// CopyOnWriteArrayList ??
		return new ArrayList<EventHandler>();
	}

	@Override
	protected Map<Object, Map<Class<? extends Event<?>>, List<EventHandler>>> createMainHandlersMap() {
		return new ConcurrentHashMap<Object, Map<Class<? extends Event<?>>, List<EventHandler>>>();
	}

	@Override
	protected Map<Class<? extends Event<?>>, List<EventHandler>> createTypeHandlersMap() {
		return new ConcurrentHashMap<Class<? extends Event<?>>, List<EventHandler>>();
	}

}