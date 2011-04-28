package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Session {

	private final Map<String, Object> data = new HashMap<String, Object>();

	private Action defaultAction;

	private Date lastRequest;

	private final String sessionId;

	public Session(String sessionId) {
		this.sessionId = sessionId;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		return (T) this.data.get(key);
	}

	Action getDefaultAction() {
		return defaultAction == null ? Action.execute : defaultAction;
	}

	public Date getLastRequest() {
		return lastRequest;
	}

	public String getSessionId() {
		return sessionId;
	}

	public <T> void setData(String key, T data) {
		this.data.put(key, data);
	}

	void setDefaultAction(Action defaultAction) {
		this.defaultAction = defaultAction;
	}

	void setLastRequest(Date lastRequest) {
		this.lastRequest = lastRequest;
	}

}
