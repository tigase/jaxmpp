/*
 * Subscription.java
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
package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

public enum Subscription {
	/** The node MUST NOT send event notifications or payloads to the Entity. */
	none,
	/**
	 * An entity has requested to subscribe to a node and the request has not
	 * yet been approved by a node owner. The node MUST NOT send event
	 * notifications or payloads to the entity while it is in this state.
	 */
	pending,
	/**
	 * An entity is subscribed to a node. The node MUST send all event
	 * notifications (and, if configured, payloads) to the entity while it is in
	 * this state (subject to subscriber configuration and content filtering).
	 */
	subscribed,
	/**
	 * An entity has subscribed but its subscription options have not yet been
	 * configured. The node MAY send event notifications or payloads to the
	 * entity while it is in this state. The service MAY timeout unconfigured
	 * subscriptions.
	 */
	unconfigured,
}