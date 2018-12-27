/*
 * GwtSessionObject.java
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
package tigase.jaxmpp.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GwtSessionObject
		extends AbstractSessionObject {

	private static Logger log = Logger.getLogger(GwtSessionObject.class.getName());

	private static native JavaScriptObject decodeJson(String str) /*-{
        try {
            return JSON.parse(str);
        } catch (ex) {
            return {};
        }
    }-*/;

	private static native String encodeJson(JavaScriptObject obj) /*-{
        return JSON.stringify(obj);
    }-*/;

	private static JID getJID(JSONObject object, String key) {
		try {
			String v = getString(object, key);
			if (v != null) {
				return JID.jidInstance(v);
			}
			return null;
		} catch (Exception e) {
			log.warning("Can't create JID instance: " + e.getMessage());
			return null;
		}
	}

	private static Long getLong(JSONObject object, String key) {
		try {
			String v = getString(object, key);
			if (v != null) {
				return Long.valueOf(v);
			}
			return null;
		} catch (Exception e) {
			log.warning("Can't create Long instance: " + e.getMessage());
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
			log.warning("Can't create String instance: " + e.getMessage());
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

	public void restore(final String value) throws RestoringSessionException {
		JavaScriptObject jsObject = decodeJson(value);
		JsonSerializationHelper helper = new JsonSerializationHelper(this, properties);
		try {
			Map<String, Entry> map = (Map) helper.fromJSON(jsObject);
			for (String key : map.keySet()) {
				properties.put(key, map.get(key));
			}
		} catch (JaxmppException ex) {
			Logger.getLogger(GwtSessionObject.class.getName()).log(Level.SEVERE, null, ex);
			throw new RestoringSessionException("Could not restore session: " + ex.getMessage());
		}
	}

	public String serialize() {
		try {
			JavaScriptObject jsObject = JsonSerializationHelper.toJSON(properties);
			// log("serialized object = ", jsObject);
			return encodeJson(jsObject);
		} catch (XMLException ex) {
			Logger.getLogger(GwtSessionObject.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	public void test() {
		try {
			JavaScriptObject jsObject = JsonSerializationHelper.toJSON(properties);
			// log("serialized object = ", jsObject);
			JsonSerializationHelper helper = new JsonSerializationHelper(this, properties);
			Map<String, Entry> map = (Map) helper.fromJSON(jsObject);

			Set<String> allKeys = new HashSet<String>(properties.keySet());
			allKeys.removeAll(map.keySet());

			// log("not serialized/deserialized keys", "" + allKeys.size());
			for (String key : allKeys) {
				Entry e = properties.get(key);
				if (e.scope == Scope.stream) {
					continue;
				}
				Object val = e.value;
				// log(key, e.scope.name(), val != null ?
				// val.getClass().toString() : "null", val != null ?
				// val.toString() : "null");
			}
		} catch (XMLException ex) {
			Logger.getLogger(GwtSessionObject.class.getName()).log(Level.SEVERE, null, ex);
		} catch (JaxmppException ex) {
			Logger.getLogger(GwtSessionObject.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static final class RestoringSessionException
			extends Exception {

		public RestoringSessionException(String message) {
			super(message);
		}
	}

	// public static native void log(String key, String scope, String cls,
	// String val) /*-{
	// console.log(key, scope, cls, val);
	// }-*/;
	//
	// public static native void log(String cls, String val) /*-{
	// console.log(cls, val);
	// }-*/;
	//
	// public static native void log(String cls, JavaScriptObject val) /*-{
	// console.log(cls, val);
	// }-*/;

}