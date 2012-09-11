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

/**
 * Base class for all events type.
 * 
 * @author bmalkow
 * 
 */
public class EventType implements Serializable {

	private static int counter = 0;

	private static final long serialVersionUID = 3511154964022649735L;

	private final int id;

	public EventType() {
		this.id = ++counter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof EventType))
			return false;
		return ((EventType) obj).id == id;
	}

	@Override
	public int hashCode() {
		return ("event" + id).hashCode();
	}

}