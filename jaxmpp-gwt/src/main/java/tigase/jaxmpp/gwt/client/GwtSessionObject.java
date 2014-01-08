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
package tigase.jaxmpp.gwt.client;

import java.util.HashMap;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.JID;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class GwtSessionObject extends AbstractSessionObject {

	public static final class RestoringSessionException extends Exception {
		public RestoringSessionException(String message) {
			super(message);
		}
	}

	private static JID getJID(JSONObject object, String key) {
		try {
			String v = getString(object, key);
			if (v != null)
				return JID.jidInstance(v);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Long getLong(JSONObject object, String key) {
		try {
			String v = getString(object, key);
			if (v != null)
				return Long.valueOf(v);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getString(JSONObject object, String key) {
		try {
			if (object.containsKey(key)) {
				String v = object.get(key).isString().stringValue();
				return v;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public GwtSessionObject() {
		properties = new HashMap<String, Entry>();
	}

	private CharSequence makeEntry(String jsonKey, String propsKey) {
		StringBuilder sb = new StringBuilder();

		Object v = properties.get(propsKey);

		sb.append("\"").append(jsonKey).append("\"").append(":").append("\"").append(v == null ? "" : v).append("\"");

		return sb;
	}

	public void restore(final JSONValue value) throws RestoringSessionException {
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();

		sb.append("{");

		sb.append("}");

		return sb.toString();
	}
}