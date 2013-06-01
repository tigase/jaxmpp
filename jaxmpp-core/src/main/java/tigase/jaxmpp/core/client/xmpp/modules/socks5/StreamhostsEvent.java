/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.modules.socks5;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;

public class StreamhostsEvent extends BaseEvent {

	private JID from;
	private List<Streamhost> hosts;
	private String id;
	private String sid;

	public StreamhostsEvent(EventType eventType, SessionObject sessionObject) {
		super(eventType, sessionObject);

		hosts = new ArrayList<Streamhost>();
	}

	public JID getFrom() {
		return from;
	}

	public List<Streamhost> getHosts() {
		return hosts;
	}

	public String getId() {
		return id;
	}

	public String getSid() {
		return sid;
	}

	public void setFrom(JID from) {
		this.from = from;
	}

	public void setHosts(List<Streamhost> hosts) {
		this.hosts = hosts;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}
}