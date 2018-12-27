/*
 * Affiliation.java
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

public enum Affiliation {
	/** */
	member,
	/** */
	none,
	/** An entity that is disallowed from subscribing or publishing to a node. */
	outcast,
	/**
	 * The manager of a node, of which there may be more than one; often but not
	 * necessarily the node creator.
	 */
	owner,
	/** An entity that is allowed to publish items to a node. */
	publisher;
}
