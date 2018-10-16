/*
 * SaveMode.java
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrzej
 */
public enum SaveMode {
	False,
	Body,
	Message,
	Stream;

	private static final Map<String, SaveMode> values = new HashMap<String, SaveMode>();

	static {
		values.put(False.toString(), False);
		values.put(Body.toString(), Body);
		values.put(Message.toString(), Message);
		values.put(Stream.toString(), Stream);
	}

	private final String value;

	public static SaveMode valueof(String v) {
		if (v == null || v.isEmpty()) {
			return False;
		}
		SaveMode result = values.get(v);
		if (result == null) {
			throw new IllegalArgumentException();
		}
		return result;
	}

	private SaveMode() {
		this.value = name().toLowerCase();
	}

	@Override
	public String toString() {
		return value;
	}

}
