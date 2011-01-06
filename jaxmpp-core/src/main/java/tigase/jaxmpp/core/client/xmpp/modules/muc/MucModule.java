package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class MucModule extends AbstractStanzaModule<Stanza> {

	public static class MucEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Message message;

		private String nickname;

		private Presence presence;

		private Room room;

		public MucEvent(EventType type) {
			super(type);
		}

		public Message getMessage() {
			return message;
		}

		public String getNickname() {
			return nickname;
		}

		public Presence getPresence() {
			return presence;
		}

		public Room getRoom() {
			return room;
		}

		public void setMessage(Message message) {
			this.message = message;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public void setPresence(Presence presence) {
			this.presence = presence;
		}

		public void setRoom(Room room) {
			this.room = room;
		}

	}

	public static final EventType MessageReceived = new EventType();

	public static final EventType OccupantChangedPresence = new EventType();

	public static final EventType OccupantComes = new EventType();

	public static final EventType OccupantLeaved = new EventType();

	public final Criteria crit;

	private final Observable observable = new Observable();

	private final Map<BareJID, Room> rooms = new HashMap<BareJID, Room>();

	public MucModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);

		this.crit = new Criteria() {

			@Override
			public Criteria add(Criteria criteria) {
				return null;
			}

			@Override
			public boolean match(Element element) throws XMLException {
				return checkElement(element);
			}
		};
	}

	public void addListener(EventType eventType, Listener<? extends MucEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends MucEvent> listener) {
		observable.addListener(listener);
	}

	protected boolean checkElement(Element element) throws XMLException {
		if (!element.getName().equals("message") && !element.getName().equals("presence"))
			return false;

		final String from = element.getAttribute("from");
		if (from == null)
			return false;

		final BareJID roomJid = BareJID.bareJIDInstance(from);

		return this.rooms.containsKey(roomJid);
	}

	@Override
	public Criteria getCriteria() {
		return crit;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public Room join(final String roomName, final String mucServer, final String nickname) throws XMLException, JaxmppException {
		final BareJID roomJid = BareJID.bareJIDInstance(roomName, mucServer);
		Room room = new Room(writer, roomJid, nickname);
		this.rooms.put(roomJid, room);

		Presence presence = Presence.create();
		presence.setTo(JID.jidInstance(roomJid, nickname));
		presence.addChild(new DefaultElement("x", null, "http://jabber.org/protocol/muc"));

		writer.write(presence);

		return room;
	}

	@Override
	public void process(Stanza element) throws XMPPException, XMLException {
		if (element instanceof Message)
			processMessage((Message) element);
		else if (element instanceof Presence)
			processPresence((Presence) element);
		else
			throw new RuntimeException("Stanza not handled");
	}

	protected void processMessage(Message element) throws XMPPException, XMLException {
		final JID from = element.getFrom();
		final BareJID roomJid = from.getBareJid();
		final String nickname = from.getResource();
		Room room = this.rooms.get(roomJid);
		if (room == null)
			throw new XMPPException(ErrorCondition.service_unavailable);

		MucEvent event = new MucEvent(MessageReceived);
		event.setMessage(element);
		event.setRoom(room);
		event.setNickname(nickname);

		observable.fireEvent(event);

	}

	protected void processPresence(Presence element) throws XMPPException, XMLException {
		final JID from = element.getFrom();
		final BareJID roomJid = from.getBareJid();
		final String nickname = from.getResource();
		Room room = this.rooms.get(roomJid);
		if (room == null)
			throw new XMPPException(ErrorCondition.service_unavailable);
		if (nickname == null)
			return;

		Presence presOld = room.getPresences().get(nickname);
		room.getPresences().put(nickname, element);
		Presence presNew = room.getPresences().get(nickname);

		MucEvent event;
		if ((presOld == null || presOld.getType() == StanzaType.unavailable) && presNew.getType() == null) {
			event = new MucEvent(OccupantComes);
		} else if ((presOld != null && presOld.getType() == null) && presNew.getType() == StanzaType.unavailable) {
			event = new MucEvent(OccupantLeaved);
		} else {
			event = new MucEvent(OccupantChangedPresence);
		}
		event.setNickname(nickname);
		event.setPresence(element);
		event.setRoom(room);
		observable.fireEvent(event);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
