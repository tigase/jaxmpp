package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Room {

	private String nickname;

	private final Map<String, Presence> presences = new HashMap<String, Presence>();

	private final BareJID roomJid;

	private final PacketWriter writer;

	public Room(PacketWriter writer, BareJID roomJid, String nickname) {
		this.roomJid = roomJid;
		this.nickname = nickname;
		this.writer = writer;
	}

	public String getNickname() {
		return nickname;
	}

	public Map<String, Presence> getPresences() {
		return presences;
	}

	public void sendMessage(String body) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setTo(JID.jidInstance(roomJid));
		msg.setType(StanzaType.groupchat);
		msg.setBody(body);

		this.writer.write(msg);
	}

}
