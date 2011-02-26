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

		private Occupant occupant;

		private String oldNickname;

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

		public Occupant getOccupant() {
			return occupant;
		}

		public String getOldNickname() {
			return oldNickname;
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

		public void setOccupant(Occupant occupant) {
			this.occupant = occupant;
		}

		public void setOldNickname(String nickname2) {
			this.oldNickname = nickname2;
		}

		public void setPresence(Presence presence) {
			this.presence = presence;
		}

		public void setRoom(Room room) {
			this.room = room;
		}

	}

	public static final EventType MessageError = new EventType();

	public static final EventType MessageReceived = new EventType();

	public static final EventType NewRoomCreated = new EventType();

	public static final EventType OccupantChangedNick = new EventType();

	public static final EventType OccupantChangedPresence = new EventType();

	public static final EventType OccupantComes = new EventType();

	public static final EventType OccupantLeaved = new EventType();

	public static final EventType PresenceError = new EventType();

	/**
	 * Local instance of Chat Room was closed because of, for example, presence
	 * error.
	 */
	public static final EventType RoomClosed = new EventType();

	public static final Integer STATUS_NEW_NICKNAME = 303;

	public static final EventType YouJoined = new EventType();

	private final Criteria crit;

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

		final String type = element.getAttribute("type");
		if (type != null && type.equals("groupchat"))
			return true;

		final BareJID roomJid = BareJID.bareJIDInstance(from);

		boolean result = this.rooms.containsKey(roomJid);

		System.out.println("!!! " + roomJid + "  " + result);

		return result;
	}

	public void enable(Room room) {
		fireYouJoinedEvent(null, null, room, null);
	}

	private void fireMucEvent(MucEvent event, Presence element, String nickname, Room room, Occupant occupant) {
		if (event == null)
			return;
		event.setNickname(nickname);
		event.setPresence(element);
		event.setRoom(room);
		event.setOccupant(occupant);
		observable.fireEvent(event);
	}

	private void fireNewRoomCreatedEvent(Presence element, String nickname, Room room, Occupant occupant) {
		MucEvent event = new MucEvent(NewRoomCreated);
		fireMucEvent(event, element, nickname, room, occupant);
	}

	private void fireYouJoinedEvent(Presence element, String nickname, Room room, Occupant occupant) {
		MucEvent event = new MucEvent(YouJoined);
		fireMucEvent(event, element, nickname, room, occupant);
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
		return join(roomName, mucServer, nickname, null);
	}

	public Room join(final String roomName, final String mucServer, final String nickname, final String password)
			throws XMLException, JaxmppException {
		final BareJID roomJid = BareJID.bareJIDInstance(roomName, mucServer);
		if (this.rooms.containsKey(roomJid))
			return this.rooms.get(roomJid);

		Room room = new Room(writer, roomJid, nickname);
		register(room);

		Presence presence = Presence.create();
		presence.setTo(JID.jidInstance(roomJid, nickname));
		final DefaultElement x = new DefaultElement("x", null, "http://jabber.org/protocol/muc");
		presence.addChild(x);

		if (password != null) {
			x.addChild(new DefaultElement("password", password, null));
		}

		writer.write(presence);

		return room;
	}

	public void leave(Room room) throws XMLException, JaxmppException {
		if (!room.isLeaved()) {
			room.setLeaved(true);
			Presence presence = Presence.create();
			presence.setType(StanzaType.unavailable);
			presence.setTo(JID.jidInstance(room.getRoomJid(), room.getNickname()));
			writer.write(presence);
		}
		this.rooms.remove(room.getRoomJid());
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
			return;
		// throw new XMPPException(ErrorCondition.service_unavailable);

		MucEvent event;
		if (element.getType() == StanzaType.error) {
			event = new MucEvent(MessageError);
		} else {
			event = new MucEvent(MessageReceived);
		}

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

		if (element.getType() == StanzaType.error && !room.isJoined() && nickname == null) {
			room.setLeaved(true);
			this.rooms.remove(room.getRoomJid());
			MucEvent event = new MucEvent(RoomClosed);
			event.setNickname(nickname);
			event.setPresence(element);
			event.setRoom(room);
			observable.fireEvent(event);
		} else if (element.getType() == StanzaType.error) {
			MucEvent event = new MucEvent(PresenceError);
			event.setNickname(nickname);
			event.setPresence(element);
			event.setRoom(room);
			observable.fireEvent(event);
			return;
		}

		if (nickname == null)
			return;

		if (element.getType() == StanzaType.unavailable && nickname.equals(room.getNickname())) {
			room.setLeaved(true);
			this.rooms.remove(room.getRoomJid());
		}

		final XMucUserElement xUser = XMucUserElement.extract(element);

		Occupant occupant = room.getPresences().get(nickname);

		Presence presOld;
		if (occupant == null) {
			presOld = null;
			occupant = new Occupant();
		} else {
			presOld = occupant.getPresence();
		}
		Presence presNew = element;

		MucEvent event;
		if ((presOld != null && presOld.getType() == null) && presNew.getType() == StanzaType.unavailable && xUser != null
				&& xUser.getStatuses().contains(STATUS_NEW_NICKNAME)) {
			event = null;
			String newNickName = xUser.getNick();
			room.remove(occupant);
			room.getTempOccupants().put(newNickName, occupant);
			log.finer(element.getFrom() + " wants to change nickname to " + newNickName);
		} else if ((presOld == null || presOld.getType() == StanzaType.unavailable) && presNew.getType() == null) {
			Occupant tmp = room.getTempOccupants().remove(nickname);
			if (tmp != null) {
				log.finer(element.getFrom() + " successfully changed nickname ");
				event = new MucEvent(OccupantChangedNick);
				event.setOldNickname(tmp.getNickname());
				occupant = tmp;
			} else {
				event = new MucEvent(OccupantComes);
			}
			occupant.setPresence(element);
			room.add(occupant);
			if (!room.isJoined() && xUser != null && xUser.getStatuses().contains(110)) {
				room.setJoined(true);
				fireYouJoinedEvent(element, nickname, room, occupant);
			}
		} else if ((presOld != null && presOld.getType() == null) && presNew.getType() == StanzaType.unavailable) {
			occupant.setPresence(element);
			room.remove(occupant);
			event = new MucEvent(OccupantLeaved);
		} else {
			occupant.setPresence(element);
			event = new MucEvent(OccupantChangedPresence);
		}

		fireMucEvent(event, element, nickname, room, occupant);

		if (xUser != null && xUser.getStatuses().contains(201)) {
			fireNewRoomCreatedEvent(element, nickname, room, occupant);
		}

	}

	public void register(Room room) {
		this.rooms.put(room.getRoomJid(), room);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
