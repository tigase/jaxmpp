package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.Collection;
import java.util.Date;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.AbstractChatManager;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.AbstractMessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room.State;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class MucModule extends AbstractStanzaModule<Stanza> {

	public static class MucEvent extends AbstractMessageEvent {

		private static final long serialVersionUID = 1L;

		private Date date;

		private String nickname;

		private Occupant occupant;

		private String oldNickname;

		private Presence presence;

		private Room room;

		private XMucUserElement xMucUserElement;

		public MucEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Date getDate() {
			return date;
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

		public XMucUserElement getxMucUserElement() {
			return xMucUserElement;
		}

		public void setDate(Date date) {
			this.date = date;
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

		public void setxMucUserElement(XMucUserElement xMucUserElement) {
			this.xMucUserElement = xMucUserElement;
		}

		public void setXMucUserElement(XMucUserElement xUser) {
			this.xMucUserElement = xUser;
		}

	}

	public static final EventType JoinRequested = new EventType();

	public static final EventType MessageError = new EventType();

	public static final EventType MucMessageReceived = new EventType();

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

	public static final EventType StateChange = new EventType();

	public static final Integer STATUS_NEW_NICKNAME = 303;

	public static final EventType YouJoined = new EventType();

	private AbstractChatManager chatManager;

	private final Criteria crit;

	private final DateTimeFormat dtf;

	private final Observable observable;

	private AbstractRoomsManager roomsManager;

	public MucModule(Observable parentObservable, final SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = ObservableFactory.instance(parentObservable);
		parentObservable.addListener(Connector.StateChanged, new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) throws JaxmppException {
				Connector.State s = sessionObject.getProperty(Connector.CONNECTOR_STAGE_KEY);
				if (s == null || s == Connector.State.disconnected || s == Connector.State.disconnecting) {
					onNetworkDisconnected();
				}
			}
		});

		AbstractRoomsManager cm = UniversalFactory.createInstance(AbstractRoomsManager.class.getName());
		this.roomsManager = cm != null ? cm : new DefaultRoomsManager();
		this.roomsManager.setObservable(this.observable);
		this.roomsManager.setPacketWriter(packetWriter);
		this.roomsManager.setSessionObject(sessionObject);
		this.roomsManager.initialize();

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
		dtf = new DateTimeFormat();

	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
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

		boolean result = this.roomsManager.contains(roomJid);

		return result;
	}

	public void enable(Room room) throws JaxmppException {
		fireYouJoinedEvent(null, null, room, null);
	}

	private void fireJoinRequestedEvent(Presence element, String nickname, Room room) throws JaxmppException {
		MucEvent event = new MucEvent(JoinRequested, sessionObject);
		fireMucEvent(event, element, nickname, room, null, null);
	}

	private void fireMucEvent(MucEvent event, Presence element, String nickname, Room room, Occupant occupant,
			XMucUserElement xUser) throws JaxmppException {
		if (event == null)
			return;
		event.setNickname(nickname);
		event.setPresence(element);
		event.setRoom(room);
		event.setOccupant(occupant);
		event.setXMucUserElement(xUser);
		observable.fireEvent(event);
	}

	private void fireNewRoomCreatedEvent(Presence element, String nickname, Room room, Occupant occupant)
			throws JaxmppException {
		MucEvent event = new MucEvent(NewRoomCreated, sessionObject);
		fireMucEvent(event, element, nickname, room, occupant, null);
	}

	private void fireYouJoinedEvent(Presence element, String nickname, Room room, Occupant occupant) throws JaxmppException {
		MucEvent event = new MucEvent(YouJoined, sessionObject);
		fireMucEvent(event, element, nickname, room, occupant, null);
	}

	public AbstractChatManager getChatManager() {
		return chatManager;
	}

	@Override
	public Criteria getCriteria() {
		return crit;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public Collection<Room> getRooms() {
		return this.roomsManager.getRooms();
	}

	public Room join(final String roomName, final String mucServer, final String nickname) throws XMLException, JaxmppException {
		return join(roomName, mucServer, nickname, null);
	}

	public Room join(final String roomName, final String mucServer, final String nickname, final String password)
			throws XMLException, JaxmppException {
		final BareJID roomJid = BareJID.bareJIDInstance(roomName, mucServer);
		if (this.roomsManager.contains(roomJid))
			return this.roomsManager.get(roomJid);

		Room room = this.roomsManager.createRoomInstance(roomJid, nickname, password);
		this.roomsManager.register(room);

		Presence presence = room.rejoin();

		fireJoinRequestedEvent(presence, nickname, room);
		return room;
	}

	public void leave(Room room) throws XMLException, JaxmppException {
		if (room.getState() == State.joined) {
			room.setState(State.not_joined);
			Presence presence = Presence.create();
			presence.setType(StanzaType.unavailable);
			presence.setTo(JID.jidInstance(room.getRoomJid(), room.getNickname()));
			writer.write(presence);
		}

		this.roomsManager.remove(room);

		MucEvent event = new MucEvent(RoomClosed, sessionObject);
		event.setRoom(room);
		observable.fireEvent(event);
	}

	protected void onNetworkDisconnected() {
		for (Room r : roomsManager.getRooms()) {
			r.setState(State.not_joined);
		}
	}

	@Override
	public void process(Stanza element) throws JaxmppException {
		if (element instanceof Message)
			processMessage((Message) element);
		else if (element instanceof Presence)
			processPresence((Presence) element);
		else
			throw new RuntimeException("Stanza not handled");
	}

	protected void processMessage(Message element) throws JaxmppException {
		final JID from = element.getFrom();
		final BareJID roomJid = from.getBareJid();
		final String nickname = from.getResource();
		Room room = this.roomsManager.get(roomJid);
		if (room == null)
			return;
		// throw new XMPPException(ErrorCondition.service_unavailable);

		if (element.getType() == StanzaType.chat && chatManager != null) {
			MessageEvent event = new MessageEvent(MessageModule.MessageReceived, sessionObject);
			event.setMessage(element);
			Chat chat = chatManager.process(element, observable);
			if (chat != null) {
				event.setChat(chat);
			}
			observable.fireEvent(event.getType(), event);
		} else {
			MucEvent event;
			if (element.getType() == StanzaType.error) {
				event = new MucEvent(MessageError, sessionObject);
			} else {
				if (room.getState() != State.joined) {
					room.setState(State.joined);
				}
				event = new MucEvent(MucMessageReceived, sessionObject);
			}

			final Element delay = element.getChildrenNS("delay", "urn:xmpp:delay");
			Date delayTime;
			if (delay != null && delay.getAttribute("stamp") != null) {
				delayTime = dtf.parse(delay.getAttribute("stamp"));
			} else {
				delayTime = null;
			}

			event.setMessage(element);
			event.setRoom(room);
			event.setNickname(nickname);
			event.setDate(delayTime == null ? new Date() : delayTime);
			room.setLastMessageDate(event.date);
			observable.fireEvent(event);
		}
	}

	protected void processPresence(Presence element) throws JaxmppException {
		final JID from = element.getFrom();
		final BareJID roomJid = from.getBareJid();
		final String nickname = from.getResource();
		Room room = this.roomsManager.get(roomJid);
		if (room == null)
			throw new XMPPException(ErrorCondition.service_unavailable);

		if (element.getType() == StanzaType.error && room.getState() != State.joined && nickname == null) {
			room.setState(State.not_joined);
			// this.rooms.remove(room.getRoomJid());
			MucEvent event = new MucEvent(RoomClosed, sessionObject);
			event.setNickname(nickname);
			event.setPresence(element);
			event.setRoom(room);
			observable.fireEvent(event);
		} else if (element.getType() == StanzaType.error) {
			MucEvent event = new MucEvent(PresenceError, sessionObject);
			event.setNickname(nickname);
			event.setPresence(element);
			event.setRoom(room);
			observable.fireEvent(event);
			return;
		}

		if (nickname == null)
			return;

		if (element.getType() == StanzaType.unavailable && nickname.equals(room.getNickname())) {
			room.setState(State.not_joined);
			// this.roomsManager.remove(room.getRoomJid());
		} else if (room.getState() != State.joined) {
			room.setState(State.joined);
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
		} else if (room.getState() != State.joined && xUser != null && xUser.getStatuses().contains(110)) {
			room.setState(State.joined);
			event = new MucEvent(YouJoined, sessionObject);
			occupant.setPresence(element);
			room.add(occupant);
		} else if ((presOld == null || presOld.getType() == StanzaType.unavailable) && presNew.getType() == null) {
			Occupant tmp = room.getTempOccupants().remove(nickname);
			if (tmp != null) {
				log.finer(element.getFrom() + " successfully changed nickname ");
				event = new MucEvent(OccupantChangedNick, sessionObject);
				event.setOldNickname(tmp.getNickname());
				occupant = tmp;
			} else {
				event = new MucEvent(OccupantComes, sessionObject);
			}
			occupant.setPresence(element);
			room.add(occupant);
		} else if ((presOld != null && presOld.getType() == null) && presNew.getType() == StanzaType.unavailable) {
			occupant.setPresence(element);
			room.remove(occupant);
			event = new MucEvent(OccupantLeaved, sessionObject);
		} else {
			occupant.setPresence(element);
			event = new MucEvent(OccupantChangedPresence, sessionObject);
		}

		fireMucEvent(event, element, nickname, room, occupant, xUser);

		if (xUser != null && xUser.getStatuses().contains(201)) {
			fireNewRoomCreatedEvent(element, nickname, room, occupant);
		}

	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void removeListener(Listener<MucEvent> mucListener) {
		this.observable.removeListener(mucListener);
	}

	public void setChatManager(AbstractChatManager chatManager) {
		this.chatManager = chatManager;
	}

}
