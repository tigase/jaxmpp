package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class Room {

	private final long id;

	private boolean joined;

	private Date lastMessageDate;

	private boolean leaved;

	private final Logger log = Logger.getLogger(this.getClass().getName());

	private String nickname;

	private String password;

	private final Map<String, Occupant> presences = new HashMap<String, Occupant>();

	private final BareJID roomJid;

	private final SessionObject sessionObject;

	private final Map<String, Occupant> tempOccupants = new HashMap<String, Occupant>();

	private final PacketWriter writer;

	public Room(long id, PacketWriter writer, BareJID roomJid, String nickname, SessionObject sessionObject) {
		this.id = id;
		this.sessionObject = sessionObject;
		this.roomJid = roomJid;
		this.nickname = nickname;
		this.writer = writer;
		log.fine("Room " + roomJid + " is created");
	}

	public void add(Occupant occupant) throws XMLException {
		this.presences.put(occupant.getNickname(), occupant);
	}

	public long getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public Map<String, Occupant> getPresences() {
		return presences;
	}

	public BareJID getRoomJid() {
		return roomJid;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public Map<String, Occupant> getTempOccupants() {
		return tempOccupants;
	}

	public boolean isJoined() {
		return joined;
	}

	public boolean isLeaved() {
		return leaved;
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

		writer.write(presence);

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

		this.writer.write(msg);
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
	}

	void setLastMessageDate(Date date) {
		if (lastMessageDate == null || date == null || lastMessageDate.getTime() < date.getTime()) {
			this.lastMessageDate = date;
		}
	}

	public void setLeaved(boolean b) {
		this.leaved = b;
	}

	void setPassword(String password) {
		this.password = password;
	}

}
