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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.AbstractSessionObject.ClearedEvent;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.BooleanField;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.AbstractMessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.InvitationEvent.Type;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room.State;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class MucModule extends AbstractStanzaModule<Stanza> {

	public static class InvitationDeclinedEvent extends AbstractMessageEvent {

		private static final long serialVersionUID = 1L;

		private JID inviteeJID;

		private String reason;

		private Room room;

		private JID roomJID;

		public InvitationDeclinedEvent(SessionObject sessionObject) {
			super(InvitationDeclined, sessionObject);
		}

		public JID getInviteeJID() {
			return inviteeJID;
		}

		public String getReason() {
			return reason;
		}

		public Room getRoom() {
			return room;
		}

		public JID getRoomJID() {
			return roomJID;
		}

		public void setInviteeJID(JID inviteeJID) {
			this.inviteeJID = inviteeJID;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public void setRoom(Room room) {
			this.room = room;
		}

		public void setRoomJID(JID roomJID) {
			this.roomJID = roomJID;
		}
	}

	public static class InvitationEvent extends AbstractMessageEvent {

		public static enum Type {
			direct,
			mediated;
		}

		private static final long serialVersionUID = 1L;

		private boolean continueFlag;

		private Type invitationType;

		private JID inviterJID;

		private String password;

		private String reaseon;

		private BareJID roomJID;

		private String threadID;

		InvitationEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Type getInvitationType() {
			return invitationType;
		}

		public JID getInviterJID() {
			return inviterJID;
		}

		public String getPassword() {
			return password;
		}

		public String getReaseon() {
			return reaseon;
		}

		public BareJID getRoomJID() {
			return roomJID;
		}

		public String getThreadID() {
			return threadID;
		}

		public boolean isContinueFlag() {
			return continueFlag;
		}

		public void setContinueFlag(boolean b) {
			this.continueFlag = b;
		}

		public void setInvitationType(Type invitationType) {
			this.invitationType = invitationType;
		}

		public void setInviterJID(JID jidInstance) {
			this.inviterJID = jidInstance;
		}

		public void setPassword(String value) {
			this.password = value;
		}

		public void setReaseon(String reaseon) {
			this.reaseon = reaseon;
		}

		public void setReason(String value) {
			this.reaseon = value;
		}

		public void setRoomJID(BareJID from) {
			this.roomJID = from;
		}

		public void setThreadID(String attribute) {
			this.threadID = attribute;
		}
	}

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

	private final static Criteria DIRECT_INVITATION_CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("x", "jabber:x:conference"));

	public static final EventType InvitationDeclined = new EventType();

	public static final EventType InvitationReceived = new EventType();

	public static final EventType JoinRequested = new EventType();

	private final static Criteria MEDIATED_INVITATION_CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("x", "http://jabber.org/protocol/muc#user")).add(ElementCriteria.name("invite"));

	private final static Criteria MEDIATED_INVITATION_DECLINED_CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("x", "http://jabber.org/protocol/muc#user")).add(ElementCriteria.name("decline"));

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

	private final Criteria crit;

	private final DateTimeFormat dtf;

	private AbstractRoomsManager roomsManager;

	public MucModule(Observable parentObservable, final SessionObject sessionObject, PacketWriter packetWriter) {
		super(ObservableFactory.instance(parentObservable), sessionObject, packetWriter);
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

		if (this.sessionObject instanceof AbstractSessionObject) {
			((AbstractSessionObject) this.sessionObject).addListener(AbstractSessionObject.Cleared,
					new Listener<ClearedEvent>() {

						@Override
						public void handleEvent(ClearedEvent be) throws JaxmppException {
							onSessionObjectCleared(be.getSessionObject(), be.getScopes());
						}
					});
		}
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

	protected boolean checkElement(Element element) throws XMLException {
		final String from = element.getAttribute("from");
		if (from == null)
			return false;

		final String type = element.getAttribute("type");

		if (element.getName().equals("message") && type != null && type.equals("groupchat")) {
			return true;
		} else if (element.getName().equals("presence")) {
			final BareJID roomJid = BareJID.bareJIDInstance(from);
			boolean result = this.roomsManager.contains(roomJid);
			return result;
		} else if (MEDIATED_INVITATION_CRIT.match(element)) {
			return true;
		} else if (DIRECT_INVITATION_CRIT.match(element)) {
			return true;
		} else if (MEDIATED_INVITATION_DECLINED_CRIT.match(element)) {
			return true;
		} else
			return false;
	}

	public void declineInvitation(InvitationEvent invitation, String reasonMsg) throws JaxmppException {
		if (invitation.getInvitationType() == Type.mediated) {
			Message message = Message.create();
			message.setTo(JID.jidInstance(invitation.getRoomJID()));

			Element x = new DefaultElement("x", null, "http://jabber.org/protocol/muc#user");
			message.addChild(x);

			Element decline = new DefaultElement("decline");
			x.addChild(decline);
			if (reasonMsg != null) {
				Element reason = new DefaultElement("reason", reasonMsg, null);
				decline.addChild(reason);
			}
			writer.write(message);
		}

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

	/**
	 * Sends mediated invitation.
	 * 
	 * @param room
	 *            MUC room
	 * @param inviteeJID
	 *            invitee JabberID
	 * @param reason
	 *            reason description
	 */
	public void invite(Room room, JID inviteeJID, String reason) throws JaxmppException {
		Message message = Message.create();
		message.setTo(JID.jidInstance(room.getRoomJid()));

		Element x = message.addChild(new DefaultElement("x", null, "http://jabber.org/protocol/muc#user"));
		Element invite = x.addChild(new DefaultElement("invite"));
		invite.setAttribute("to", inviteeJID.toString());
		if (reason != null) {
			invite.addChild(new DefaultElement("reason", reason, null));
		}

		writer.write(message);
	}

	public void inviteDirectly(Room room, JID inviteeJID, String reason, String threadId) throws JaxmppException {
		Message message = Message.create();
		message.setTo(inviteeJID);

		Element x = message.addChild(new DefaultElement("x", null, "jabber:x:conference"));
		x.setAttribute("jid", room.getRoomJid().toString());

		if (room.getPassword() != null)
			x.setAttribute("password", room.getPassword());

		if (reason != null)
			x.setAttribute("reason", reason);

		if (threadId != null) {
			x.setAttribute("thread", threadId);
			x.setAttribute("continue", "true");
		}

		writer.write(message);
	}

	public Room join(final InvitationEvent invitation, final String nickname) throws XMLException, JaxmppException {
		return join(invitation.getRoomJID().getLocalpart(), invitation.getRoomJID().getDomain(), nickname,
				invitation.getPassword());
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

	protected void onNetworkDisconnected() throws XMLException, JaxmppException {
	}

	protected void onSessionObjectCleared(SessionObject sessionObject, Set<Scope> scopes) throws JaxmppException {
		if (scopes != null && scopes.contains(Scope.session)) {
			for (Room r : roomsManager.getRooms()) {
				r.setState(State.not_joined);

				ArrayList<Occupant> ocs = new ArrayList<Occupant>();
				ocs.addAll(r.getPresences().values());
				for (Occupant occupant : ocs) {
					MucEvent event = new MucEvent(OccupantLeaved, sessionObject);
					r.remove(occupant);
					fireMucEvent(event, null, occupant.getNickname(), r, occupant, null);
				}
			}
		}
	}

	@Override
	public void process(Stanza element) throws JaxmppException {
		if (element instanceof Message && MEDIATED_INVITATION_DECLINED_CRIT.match(element)) {
			processInvitationDeclinedMessage((Message) element);
		} else if (element instanceof Message && MEDIATED_INVITATION_CRIT.match(element)) {
			processMediatedInvitationMessage((Message) element);
		} else if (element instanceof Message && DIRECT_INVITATION_CRIT.match(element)) {
			processDirectInvitationMessage((Message) element);
		} else if (element instanceof Message)
			processMessage((Message) element);
		else if (element instanceof Presence)
			processPresence((Presence) element);
		else
			throw new RuntimeException("Stanza not handled");
	}

	protected void processDirectInvitationMessage(Message message) throws JaxmppException {
		Element x = message.getChildrenNS("x", "jabber:x:conference");

		InvitationEvent event = new InvitationEvent(InvitationReceived, sessionObject);
		event.setInvitationType(Type.direct);
		event.setMessage(message);

		event.setInviterJID(message.getFrom());
		event.setRoomJID(BareJID.bareJIDInstance(x.getAttribute("jid")));
		event.setPassword(x.getAttribute("password"));
		event.setReason(x.getAttribute("reason"));
		event.setThreadID(x.getAttribute("thread"));

		String cont = x.getAttribute("continue");
		event.setContinueFlag(BooleanField.parse(cont));

		observable.fireEvent(event);
	}

	protected void processInvitationDeclinedMessage(Message message) throws JaxmppException {
		final BareJID roomJid = message.getFrom().getBareJid();
		Room room = this.roomsManager.get(roomJid);
		if (room == null)
			return;

		final Element x = message.getChildrenNS("x", "http://jabber.org/protocol/muc#user");
		final Element decline = getFirstChild(x, "decline");
		final Element reason = getFirstChild(decline, "reason");

		InvitationDeclinedEvent event = new InvitationDeclinedEvent(sessionObject);
		event.setMessage(message);
		event.setRoomJID(message.getFrom());
		event.setRoom(room);
		if (decline.getAttribute("from") != null)
			event.setInviteeJID(JID.jidInstance(decline.getAttribute("from")));
		if (reason != null)
			event.setReason(reason.getValue());

		observable.fireEvent(event);
	}

	protected void processMediatedInvitationMessage(Message message) throws JaxmppException {
		Element x = message.getChildrenNS("x", "http://jabber.org/protocol/muc#user");
		Element invite = getFirstChild(x, "invite");
		Element reason = getFirstChild(invite, "reason");
		Element password = getFirstChild(x, "password");
		String inviter = invite.getAttribute("from");

		InvitationEvent event = new InvitationEvent(InvitationReceived, sessionObject);
		event.setInvitationType(Type.mediated);
		event.setMessage(message);
		event.setRoomJID(message.getFrom().getBareJid());

		if (inviter != null) {
			event.setInviterJID(JID.jidInstance(inviter));
		}

		if (reason != null) {
			event.setReason(reason.getValue());
		}

		if (password != null) {
			event.setPassword(password.getValue());
		}

		observable.fireEvent(event);
	}

	protected void processMessage(Message element) throws JaxmppException {
		final JID from = element.getFrom();
		final BareJID roomJid = from.getBareJid();
		final String nickname = from.getResource();

		Room room = this.roomsManager.get(roomJid);
		if (room == null)
			return;
		// throw new XMPPException(ErrorCondition.service_unavailable);

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

}