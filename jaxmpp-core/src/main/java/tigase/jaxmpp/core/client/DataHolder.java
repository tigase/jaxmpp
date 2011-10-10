package tigase.jaxmpp.core.client;

import java.util.HashMap;

public class DataHolder {

	private final HashMap<String, Object> data = new HashMap<String, Object>();

	public <T> T getData(String key) {
		return (T) this.data.get(key);
	}

	public <T> T removeData(String key) {
		return (T) this.data.remove(key);
	}

	public void setData(String key, Object value) {
		this.data.put(key, value);
	}

}
