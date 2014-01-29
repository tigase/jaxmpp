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
package tigase.jaxmpp.core.client.xmpp.utils;

/**
 * Util class for escaping HTML entities.
 */
public class EscapeUtils {

	private static final String[][] ENTITIES = { { "&", "&amp;" }, { "<", "&lt;" }, { ">", "&gt;" }, { "\"", "&quot;" },
			{ "'", "&apos;" }, };

	public static String escape(String str) {
		if (str == null)
			return null;
		if (str.length() == 0)
			return str;
		for (int i = 0; i < ENTITIES.length; i++) {
			str = str.replace(ENTITIES[i][0], ENTITIES[i][1]);
		}
		return str;
	}

	public static String unescape(String str) {
		if (str == null)
			return null;
		if (str.length() == 0)
			return str;
		for (int i = ENTITIES.length - 1; i >= 0; i--) {
			str = str.replace(ENTITIES[i][1], ENTITIES[i][0]);
		}
		return str;
	}

	private EscapeUtils() {
	}
}