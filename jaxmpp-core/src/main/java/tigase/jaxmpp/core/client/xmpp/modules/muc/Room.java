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
package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.StateChangeHandler.StateChangeEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class Room {

	public static enum State {
		joined,
		not_joined,
		requested
	}

	private Context context;

	private final long id;

	private Date lastMessageDate;

	private final Logger log = Logger.getLogger(this.getClass().getName());

	private String nickname;

	private String password;

	private final Map<String, Occupant> presences = new HashMap<String, Occupant>();

	private final BareJID roomJid;

	private State state = State.not_joined;

	private final Map<String, Occupant> tempOccupants = new HashMap<String, Occupant>();

	public Room(long id, Context context, BareJID roomJid, String nickname) {
		this.id = id;
		this.context = context;
		this.roomJid = roomJid;
		this.nickname = nickname;
		log.fine("Room " + roomJid + " is created");
	}

	public void add(Occupant occupant) throws XMLException {
		this.presences.put(occupant.getNickname(), occupant);
	}

	public long getId() {
		return id;
	}

	public Date getLastMessageDate() {
		return lastMessageDate;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}

	public Map<String, Occupant> getPresences() {
		return presences;
	}

	public BareJID getRoomJid() {
		return roomJid;
	}

	public SessionObject getSessionObject() {
		return context.getSessionObject();
	}

	public State getState() {
		return state;
	}

	public Map<String, Occupant> getTempOccupants() {
		return tempOccupants;
	}

	public Presence rejoin() throws JaxmppException {
		Presence presence = Presence.create();
		presence.setTo(JID.jidInstance(roomJid, nickname));
		final DefaultElement x = new DefaultElement("x", null, "http://jabber.org/protocol/muc");
		presence.addChild(x);

		if (password != null) {
			x.addChild(new DefaultElement("password", password, null));
		}

		if (lastMessageDate != null) {
			DateTimeFormat dtf = new DateTimeFormat();
			DefaultElement history = new DefaultElement("history", null, null);
			history.setAttribute("since", dtf.format(lastMessageDate));
			x.addChild(history);
		}

		setState(State.requested);
		context.getWriter().write(presence);

		return presence;
	}

	public void remove(Occupant occupant) throws XMLException {
		this.presences.remove(occupant.getNickname());
	}

	public void sendMessage(String body) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setTo(JID.jidInstance(roomJid));
		msg.setType(StanzaType.groupchat);
		msg.setBody(body);

		this.context.getWriter().write(msg);
	}

	public void setLastMessageDate(Date date) {
		if (lastMessageDate == null || date == null || lastMessageDate.getTime() < date.getTime()) {
			this.lastMessageDate = date;
		}
	}

	public void setPassword(String password) {
		this.password = password;
	}

	void setState(State state) {
		State oldState = this.state;
		this.state = state;
		StateChangeEvent e = new StateChangeEvent(context.getSessionObject(), this, oldState, state);
		context.getEventBus().fire(e);
	}

}