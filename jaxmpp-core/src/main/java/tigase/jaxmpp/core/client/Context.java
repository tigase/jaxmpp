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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.xmpp.modules.ModuleProvider;

/**
 * Interface for information about XMPP connection context. It provides access
 * to {@link SessionObject}, {@link EventBus} and {@link PacketWriter}.
 */
public interface Context {

	/**
	 * Returns {@link EventBus}.
	 * 
	 * @return {@link EventBus} instance.
	 */
	EventBus getEventBus();

	/**
	 * Returns {@link SessionObject}.
	 * 
	 * @return {@link SessionObject} instance.
	 */
	SessionObject getSessionObject();

	/**
	 * Returns {@link PacketWriter}
	 * 
	 * @return {@link PacketWriter} instance.
	 */
	PacketWriter getWriter();

	
	/**
	 * Returns {@link ModuleProvider}
	 * 
	 * @return {@link ModuleProvider} instance.
	 */
	ModuleProvider getModuleProvider();
}
