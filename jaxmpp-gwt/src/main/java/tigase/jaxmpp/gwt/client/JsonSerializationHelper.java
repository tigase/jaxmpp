/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 "Tigase, Inc."
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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule;
//import static tigase.jaxmpp.gwt.client.GwtSessionObject.log;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

/**
 *
 * @author andrzej
 */
public class JsonSerializationHelper {
	
	public interface Serializable {
		
		String getJsonType();
		
		JavaScriptObject toJSON() throws XMLException;
		
		Object fromJSON(JsonSerializationHelper helper, JavaScriptObject obj) throws JaxmppException;
		
	}
	
	private static final long INT_SPLIT = ((long) Integer.MAX_VALUE) + 1;
	
	private final Serializable[] serializers;
	private final SessionObject sessionObject;
	
	public JsonSerializationHelper(SessionObject sessionObject, Map<String, AbstractSessionObject.Entry> properties) {
		this.sessionObject = sessionObject;
		
		List<Serializable> list = new ArrayList<Serializable>();
		for (AbstractSessionObject.Entry entry : properties.values()) {
			if (entry != null && entry.value instanceof Serializable)
				list.add((Serializable) entry.value);
		}
		
		this.serializers = list.toArray(new Serializable[list.size()]);
	}
	
	public JsonSerializationHelper(SessionObject sessionObject, Serializable[] serializers) {
		this.sessionObject = sessionObject;
		this.serializers = serializers;
	}
	
	public SessionObject getSessionObject() {
		return sessionObject;
	}
	
	public static JavaScriptObject toJSON(Object val) throws XMLException {
		if (val instanceof Boolean) {
			return toJSON(((Boolean) val).booleanValue());
		}
		else if (val instanceof Integer) {
			return toJSON((Integer) val);
		} else if (val instanceof Long) {
			long v = (Long) val;
			int hi = (int)(v / INT_SPLIT);
			int lo = (int)(v % INT_SPLIT);
			return toJSONLong(hi, lo);
		} else if (val instanceof String) {
			return toJSON((String) val);
		} else if (val instanceof Element) {
			return toJSON("element", ((Element) val).getAsString());
		} else if (val instanceof Map) {
			JavaScriptObject jsMap = JavaScriptObject.createObject();
			Map<String,Object> map = (Map<String,Object>) val;
			for (String key : map.keySet()) {
				if (Connector.CONNECTOR_STAGE_KEY.equals(key))
					continue;
				
				Object v = map.get(key);
				JavaScriptObject e = toJSON(v);
				if (e != null) {
					putToObject(jsMap, key, e);
				}
			}
			return toJSON("map", jsMap);
		} else if (val instanceof Collection) {
			String type = null;
			if (val instanceof Set) 
				type = "set";
			else if (val instanceof List)
				type = "list";
			JavaScriptObject jsArr = JavaScriptObject.createArray();
			for (Object v : ((Collection) val)) {
				JavaScriptObject e = toJSON(v);
				addToArray(jsArr, e);
			}
			return toJSON(type, jsArr);
		} else if (val instanceof AbstractSessionObject.Entry) {
			AbstractSessionObject.Entry e = (AbstractSessionObject.Entry) val;
			if (e.scope != SessionObject.Scope.stream) {
				JavaScriptObject v = toJSON(e.value);
				if (v != null) {
					JavaScriptObject obj = toJSON("entry", v);//JavaScriptObject.createObject();
					putToObject(obj, "scope", e.scope.name());
					return obj;
				}
			}
			return null;
		} else if (val instanceof JID) {
			return toJSON("jid", ((JID) val).toString());
		} else if (val instanceof BareJID) {
			return toJSON("barejid", ((BareJID) val).toString());
		} else if (val instanceof StreamManagementModule.MutableLong) {
			StreamManagementModule.MutableLong v = (StreamManagementModule.MutableLong) val;
			int hi = (int)(v.longValue() / INT_SPLIT);
			int lo = (int)(v.longValue() % INT_SPLIT);
			JavaScriptObject js = toJSONLong(hi, lo);
			putToObject(js, "type", "mutablelong");
			return js;
		} else if (val instanceof JsonSerializationHelper.Serializable) {
			return ((Serializable) val).toJSON();
		} else {
			//log(val != null ? val.getClass().toString() : "null", val != null ? val.toString() : "null");
			return null;
		}
	}
	
	private static native JavaScriptObject toJSON(boolean value) /*-{
			return { type: 'bool', value:value };
	}-*/;
	
	private static native JavaScriptObject toJSON(int value) /*-{
			return { type: 'int', value:value };
	}-*/;
	
	private static native JavaScriptObject toJSONLong(int hi, int lo) /*-{
			return { type: 'long', hi:hi, lo:lo };
	}-*/;	
	
	private static native JavaScriptObject toJSON(String value) /*-{
			return { type: 'string', value:value };
	}-*/;	
	
	private static native JavaScriptObject toJSON(String type, String value) /*-{
			return { type: type, value:value };
	}-*/;
	
	private static native JavaScriptObject toJSON(String type, JavaScriptObject value) /*-{
			return { type: type, value:value };
	}-*/;	

	public static native void putToObject(JavaScriptObject jsMap, String key, String val) /*-{
		jsMap[key] = val;
	}-*/;
	
	public static native void putToObject(JavaScriptObject jsMap, String key, JavaScriptObject val) /*-{
		jsMap[key] = val;
	}-*/;
	
	public static native void addToArray(JavaScriptObject jsArr, JavaScriptObject val) /*-{
		jsArr.push(val);
	}-*/;
	
	public Object fromJSON(JavaScriptObject obj) throws JaxmppException {
		String type = getStringFromObject(obj, "type");
		if ("bool".equals(type)) {
			return getBooleanFromObject(obj, "value");
		} else if ("int".equals(type)) {
			return getIntegerFromObject(obj, "value");
		} else if ("long".equals(type)) {
			long val = getIntegerFromObject(obj, "hi");
			val = val * INT_SPLIT;
			val += getIntegerFromObject(obj, "lo");
			return val;
		} else if ("string".equals(type)) {
			return getStringFromObject(obj, "value");
		} else if ("element".equals(type)) {
			String data = getStringFromObject(obj, "value");
			return GwtElement.parse(data);
		} else if ("map".equals(type)) {
			Map<String,Object> map = new HashMap<String,Object>();
			JavaScriptObject val = getObjectFromObject(obj, "value");
			JsArrayString jsKeys = getKeysFromObject(val);
			for (int i=0; i<jsKeys.length(); i++) { 
				String key = jsKeys.get(i);
				JavaScriptObject v = getObjectFromObject(val, key);
				//log("trying to decode for key = " + key, v);
				Object v1 = fromJSON(v);
				map.put(key, v1);
			}
			return map;
		} else if ("set".equals(type)) {
			Set set = new HashSet();
			JsArray<JavaScriptObject> array = (JsArray<JavaScriptObject>) getObjectFromObject(obj, "value");
			for (int i=0; i<array.length(); i++) {
				JavaScriptObject val = array.get(i);
				Object v = fromJSON(val);
				if (v != null) {
					set.add(v);
				}
			}
			return set;
		} else if ("list".equals(type)) {
			List list = new ArrayList();
			JsArray<JavaScriptObject> array = (JsArray<JavaScriptObject>) getObjectFromObject(obj, "value");
			for (int i=0; i<array.length(); i++) {
				JavaScriptObject val = array.get(i);
				Object v = fromJSON(val);
				if (v != null) {
					list.add(v);
				}
			}
			return list;
		} else if ("entry".equals(type)) {
			AbstractSessionObject.Entry e = new AbstractSessionObject.Entry();
			e.scope = SessionObject.Scope.valueOf(getStringFromObject(obj, "scope"));
			JavaScriptObject val = getObjectFromObject(obj, "value");
			e.value = fromJSON(val);
			return e;
		} else if ("jid".equals(type)) {
			return JID.jidInstance(getStringFromObject(obj, "value"));
		} else if ("barejid".equals(type)) {
			return BareJID.bareJIDInstance(getStringFromObject(obj, "value"));
		} else if ("mutablelong".equals(type)) {
			long val = getIntegerFromObject(obj, "hi");
			val = val * INT_SPLIT;
			val += getIntegerFromObject(obj, "lo");			
			StreamManagementModule.MutableLong v = new StreamManagementModule.MutableLong();
			v.setValue(val);
			return v;
		} else {
			if (serializers != null) {
				Object val = null;
				for (Serializable it : serializers) {
					if (it.getJsonType().equals(type)) {
						val = it.fromJSON(this, obj);
					}
				}
				if (val != null)
					return val;
			}
			//log("Unknown type of serialized object", obj);
			return null;
		}
	}
	
	public static native String getStringFromObject(JavaScriptObject obj, String key) /*-{
		return obj[key];
	}-*/;
	
	private static native boolean getBooleanFromObject(JavaScriptObject obj, String key) /*-{
		return obj[key];	
	}-*/;
	
	private static native int getIntegerFromObject(JavaScriptObject obj, String key) /*-{
		return obj[key];	
	}-*/;	
	
	public static native JavaScriptObject getObjectFromObject(JavaScriptObject obj, String key) /*-{
		return obj[key];	
	}-*/;
	
	public static native JsArrayString getKeysFromObject(JavaScriptObject obj) /*-{
		var result = new Array();
		for (var key in obj) {
			result.push(key);
		}
		return result;
	}-*/;
	
}
