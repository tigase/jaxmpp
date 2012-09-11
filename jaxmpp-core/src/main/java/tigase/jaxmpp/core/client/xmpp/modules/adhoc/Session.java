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