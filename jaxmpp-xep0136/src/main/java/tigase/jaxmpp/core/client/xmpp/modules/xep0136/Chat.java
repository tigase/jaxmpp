/*
 * Chat.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

import java.util.Date;

public class Chat {

	private Element elem;
	private Date start;
	private String subject;
	private JID withJid;

	public Element getChat() {
		return elem;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public JID getWithJid() {
		return withJid;
	}

	public void setWithJid(JID withJid) {
		this.withJid = withJid;
	}

	void process(Element chat, DateTimeFormat df1) throws XMLException {
		this.elem = chat;
		setWithJid(JID.jidInstance(chat.getAttribute("with")));
		setStart(df1.parse(chat.getAttribute("start")));
		setSubject(chat.getAttribute("subject"));
	}

	@Override
	public String toString() {
		return "Chat{" + "start=" + start + ", subject=" + subject + ", withJid=" + withJid + '}';
	}
}
