package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Room {

	private boolean joined;

	private boolean leaved;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private String nickname;

	private final Map<String, Occupant> presences = new HashMap<String, Occupant>();

	private final BareJID roomJid;

	private final Map<String, Occupant> tempOccupants = new HashMap<String, Occupant>();

	private final PacketWriter writer;

	public Room(PacketWriter writer, BareJID roomJid, String nickname) {
		this.roomJid = roomJid;
		this.nickname = nickname;
		this.writer = writer;
		log.fine("Room " + roomJid + " is created");
	}

	public void add(Occupant occupant) throws XMLException {
		this.presences.put(occupant.getNickname(), occupant);
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

	public Map<String, Occupant> getTempOccupants() {
		return tempOccupants;
	}

	public boolean isJoined() {
		return joined;
	}

	public boolean isLeaved() {
		return leaved;
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

	public void setLeaved(boolean b) {
		this.leaved = b;
	}

}
