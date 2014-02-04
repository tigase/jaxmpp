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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import java.util.Date;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class Chat {

	private Date start;
	private String subject;
	private JID withJid;

	public Date getStart() {
		return start;
	}

	public String getSubject() {
		return subject;
	}

	public JID getWithJid() {
		return withJid;
	}

	void process(Element chat, DateTimeFormat df1) throws XMLException {
		setWithJid(JID.jidInstance(chat.getAttribute("with")));
		setStart(df1.parse(chat.getAttribute("start")));
		setSubject(chat.getAttribute("subject"));
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setWithJid(JID withJid) {
		this.withJid = withJid;
	}
}
