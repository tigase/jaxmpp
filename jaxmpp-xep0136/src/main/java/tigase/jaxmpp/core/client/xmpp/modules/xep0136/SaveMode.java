/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author andrzej
 */
public enum SaveMode {
	False,
	Body,
	Message,
	Stream;
	
	private final String value;
	
	private SaveMode() {
		this.value = name().toLowerCase();
	}
	
	private static final Map<String,SaveMode> values = new HashMap<String,SaveMode>();
	static {
		values.put(False.toString(), False);
		values.put(Body.toString(), Body);
		values.put(Message.toString(), Message);
		values.put(Stream.toString(), Stream);
	}
	
	public static SaveMode valueof(String v) {
		if (v == null || v.isEmpty()) {
			return False;
		}
		SaveMode result = values.get(v);
		if (result == null)
			throw new IllegalArgumentException();
		return result;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
}
