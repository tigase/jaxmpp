/*
 * DataHolder.java
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
package tigase.jaxmpp.core.client;

import java.util.HashMap;

/**
 * Small class for storing all types of additional data.
 */
public class DataHolder {

	private final HashMap<String, Object> data = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		return (T) this.data.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T removeData(String key) {
		return (T) this.data.remove(key);
	}

	public void setData(String key, Object value) {
		this.data.put(key, value);
	}

}