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
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.BooleanField;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.forms.XDataType;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.Item;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.InvitationDeclinedHandler.InvitationDeclinedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.InvitationReceivedHandler.InvitationReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.JoinRequestedHandler.JoinRequestedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MessageErrorHandler.MessageErrorEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucMessageReceivedHandler.MucMessageReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.NewRoomCreatedHandler.NewRoomCreatedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.OccupantChangedNickHandler.OccupantChangedNickEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.OccupantChangedPresenceHandler.OccupantChangedPresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.OccupantComesHandler.OccupantComesEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.OccupantLeavedHandler.OccupantLeavedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.PresenceErrorHandler.PresenceErrorEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.RoomClosedHandler.RoomClosedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.YouJoinedHandler.YouJoinedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room.State;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class MucModule extends AbstractStanzaModule<Stanza> {

	public static final String OWNER_XMLNS = "http://jabber.org/protocol/muc#owner";

	public final class DirectInvitation extends Invitation {

		private boolean continueFlag;

		private String threadID;

		DirectInvitation(SessionObject sessionObject) {
			super(sessionObject);
		}

		public String getThreadID() {
			return threadID;
		}

		public boolean isContinueFlag() {
			return continueFlag;
		}

		void setContinueFlag(boolean continueFlag) {
			this.continueFlag = continueFlag;
		}

		void setThreadID(String threadID) {
			this.threadID = threadID;
		}

	}

	public abstract class Invitation {

		private JID inviterJID;

		private String password;

		private String reason;

		private BareJID roomJID;

		protected final SessionObject sessionObject;

		protected Invitation(SessionObject sessionObject) {
			this.sessionObject = sessionObject;
		}

		public JID getInviterJID() {
			return inviterJID;
		}

		public String getPassword() {
			return password;
		}

		public String getReason() {
			return reason;
		}

		public BareJID getRoomJID() {
			return roomJID;
		}

		public SessionObject getSessionObject() {
			return sessionObject;
		}

		void setInviterJID(JID inviterJID) {
			this.inviterJID = inviterJID;
		}

		void setMessage(Message message) {

		}

		void setPassword(String password) {
			this.password = password;
		}

		void setReason(String reason) {
			this.reason = reason;
		}

		void setRoomJID(BareJID roomJID) {
			this.roomJID = roomJID;
		}
	}

	public interface InvitationDeclinedHandler extends EventHandler {

		public static class InvitationDeclinedEvent extends JaxmppEvent<InvitationDeclinedHandler> {

			private JID inviteeJID;

			private Message message;

			private String reason;

			private Room room;

			public InvitationDeclinedEvent(SessionObject sessionObject, Message message, Room room, JID inviteeJID,
					String reason) {
				super(sessionObject);
				this.message = message;
				this.room = room;
				this.inviteeJID = inviteeJID;
				this.reason = reason;
			}

			@Override
			protected void dispatch(InvitationDeclinedHandler handler) {
				handler.onInvitationDeclined(sessionObject, message, room, inviteeJID, reason);
			}

			public JID getInviteeJID() {
				return inviteeJID;
			}

			public Message getMessage() {
				return message;
			}

			public String getReason() {
				return reason;
			}

			public Room getRoom() {
				return room;
			}

			public void setInviteeJID(JID inviteeJID) {
				this.inviteeJID = inviteeJID;
			}

			public void setMessage(Message message) {
				this.message = message;
			}

			public void setReason(String reason) {
				this.reason = reason;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

		}

		void onInvitationDeclined(SessionObject sessionObject, Message message, Room room, JID inviteeJID, String reason);
	}

	public interface InvitationReceivedHandler extends EventHandler {

		public static class InvitationReceivedEvent extends JaxmppEvent<InvitationReceivedHandler> {

			private Invitation invitation;

			public InvitationReceivedEvent(SessionObject sessionObject, Invitation invitation) {
				super(sessionObject);
				this.invitation = invitation;
			}

			@Override
			protected void dispatch(InvitationReceivedHandler handler) {
				handler.onInvitationReceived(sessionObject, invitation, invitation.getInviterJID(), invitation.getRoomJID());
			}

			public Invitation getInvitation() {
				return invitation;
			}

			public void setInvitation(Invitation invitation) {
				this.invitation = invitation;
			}

		}

		void onInvitationReceived(SessionObject sessionObject, Invitation invitation, JID inviterJID, BareJID roomJID);
	}

	public interface JoinRequestedHandler extends EventHandler {

		public static class JoinRequestedEvent extends JaxmppEvent<JoinRequestedHandler> {

			private String nickname;
			private Room room;
			private Presence sentPresence;

			public JoinRequestedEvent(SessionObject sessionObject, Presence presence, String nickname, Room room) {
				super(sessionObject);
				this.sentPresence = presence;
				this.nickname = nickname;
				this.room = room;
			}

			@Override
			protected void dispatch(JoinRequestedHandler handler) {
				handler.onJoinRequested(sessionObject, room, nickname, sentPresence);
			}

			public String getNickname() {
				return nickname;
			}

			public Room getRoom() {
				return room;
			}

			public Presence getSentPresence() {
				return sentPresence;
			}

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

			public void setSentPresence(Presence sentPresence) {
				this.sentPresence = sentPresence;
			}

		}

		void onJoinRequested(SessionObject sessionObject, Room room, String nickname, Presence sentPresence);
	}

	public final class MediatedInvitation extends Invitation {

		MediatedInvitation(SessionObject sessionObject) {
			super(sessionObject);
		}

	}

	public interface MessageErrorHandler extends EventHandler {

		public static class MessageErrorEvent extends JaxmppEvent<MessageErrorHandler> {

			private Message message;

			private String nickname;

			private Room room;

			private Date timestamp;

			public MessageErrorEvent(SessionObject sessionObject, Message message, Room room, String nickname, Date delayTime) {
				super(sessionObject);
				this.room = room;
				this.message = message;
				this.nickname = nickname;
				this.timestamp = delayTime;
			}

			@Override
			protected void dispatch(MessageErrorHandler handler) {
				handler.onMessageError(sessionObject, message, room, nickname, timestamp);
			}

			public Message getMessage() {
				return message;
			}

			public String getNickname() {
				return nickname;
			}

			public Room getRoom() {
				return room;
			}

			public Date getTimestamp() {
				return timestamp;
			}

			public void setMessage(Message message) {
				this.message = message;
			}

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

			public void setTimestamp(Date timestamp) {
				this.timestamp = timestamp;
			}

		}

		void onMessageError(SessionObject sessionObject, Message message, Room room, String nickname, Date timestamp);
	}

	public interface MucMessageReceivedHandler extends EventHandler {

		public static class MucMessageReceivedEvent extends JaxmppEvent<MucMessageReceivedHandler> {

			private Message message;

			private String nickname;

			private Room room;

			private Date timestamp;

			public MucMessageReceivedEvent(SessionObject sessionObject, Message message, Room room, String nickname,
					Date delayTime) {
				super(sessionObject);
				this.message = message;
				this.room = room;
				this.nickname = nickname;
				this.timestamp = delayTime;
			}

			@Override
			protected void dispatch(MucMessageReceivedHandler handler) {
				handler.onMucMessageReceived(sessionObject, message, room, nickname, timestamp);
			}

			public Message getMessage() {
				return message;
			}

			public String getNickname() {
				return nickname;
			}

			public Room getRoom() {
				return room;
			}

			public Date getTimestamp() {
				return timestamp;
			}

			public void setMessage(Message message) {
				this.message = message;
			}

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

			public void setTimestamp(Date timestamp) {
				this.timestamp = timestamp;
			}

		}

		void onMucMessageReceived(SessionObject sessionObject, Message message, Room room, String nickname, Date timestamp);
	}

	public interface NewRoomCreatedHandler extends EventHandler {

		public static class NewRoomCreatedEvent extends JaxmppEvent<NewRoomCreatedHandler> {

			private Presence presence;

			private Room room;

			public NewRoomCreatedEvent(SessionObject sessionObject, Room room, Presence element) {
				super(sessionObject);
				this.room = room;
				this.presence = element;
			}

			@Override
			protected void dispatch(NewRoomCreatedHandler handler) {
				handler.onNewRoomCreated(sessionObject, room);
			}

			public Presence getPresence() {
				return presence;
			}

			public Room getRoom() {
				return room;
			}

			public void setPresence(Presence presence) {
				this.presence = presence;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

		}

		void onNewRoomCreated(SessionObject sessionObject, Room room);
	}

	public interface OccupantChangedNickHandler extends EventHandler {

		public static class OccupantChangedNickEvent extends JaxmppEvent<OccupantChangedNickHandler> {

			private String nickname;

			private Occupant occupant;

			private String oldNickname;

			private Presence presence;

			private Room room;

			public OccupantChangedNickEvent(SessionObject sessionObject, Presence element, Room room, Occupant occupant,
					String oldNickname, String nickname) {
				super(sessionObject);
				this.presence = element;
				this.room = room;
				this.occupant = occupant;
				this.oldNickname = oldNickname;
				this.nickname = nickname;

			}

			@Override
			protected void dispatch(OccupantChangedNickHandler handler) {
				handler.onOccupantChangedNick(sessionObject, room, occupant, oldNickname, nickname);
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

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public void setOccupant(Occupant occupant) {
				this.occupant = occupant;
			}

			public void setOldNickname(String oldNickname) {
				this.oldNickname = oldNickname;
			}

			public void setPresence(Presence presence) {
				this.presence = presence;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

		}

		void onOccupantChangedNick(SessionObject sessionObject, Room room, Occupant occupant, String oldNickname,
				String newNickname);
	}

	public interface OccupantChangedPresenceHandler extends EventHandler {

		public static class OccupantChangedPresenceEvent extends JaxmppEvent<OccupantChangedPresenceHandler> {

			private String nickname;

			private Occupant occupant;

			private Presence presence;

			private Room room;

			private XMucUserElement xUserElement;

			public OccupantChangedPresenceEvent(SessionObject sessionObject, Presence element, Room room, Occupant occupant,
					String nickname, XMucUserElement xUser) {
				super(sessionObject);
				this.presence = element;
				this.room = room;
				this.occupant = occupant;
				this.nickname = nickname;
				this.xUserElement = xUser;
			}

			@Override
			protected void dispatch(OccupantChangedPresenceHandler handler) {
				handler.onOccupantChangedPresence(sessionObject, room, occupant, presence);
			}

			public String getNickname() {
				return nickname;
			}

			public Occupant getOccupant() {
				return occupant;
			}

			public Presence getPresence() {
				return presence;
			}

			public Room getRoom() {
				return room;
			}

			public XMucUserElement getxUserElement() {
				return xUserElement;
			}

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public void setOccupant(Occupant occupant) {
				this.occupant = occupant;
			}

			public void setPresence(Presence presence) {
				this.presence = presence;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

			public void setxUserElement(XMucUserElement xUserElement) {
				this.xUserElement = xUserElement;
			}

		}

		void onOccupantChangedPresence(SessionObject sessionObject, Room room, Occupant occupant, Presence newPresence);
	}

	public interface OccupantComesHandler extends EventHandler {

		public static class OccupantComesEvent extends JaxmppEvent<OccupantComesHandler> {

			private String nickname;

			private Occupant occupant;

			private Presence presence;

			private Room room;

			private XMucUserElement xUserElement;

			public OccupantComesEvent(SessionObject sessionObject, Presence element, Room room, Occupant occupant,
					String nickname, XMucUserElement xUser) {
				super(sessionObject);
				this.presence = element;
				this.room = room;
				this.occupant = occupant;
				this.nickname = nickname;
				this.xUserElement = xUser;
			}

			@Override
			protected void dispatch(OccupantComesHandler handler) {
				handler.onOccupantComes(sessionObject, room, occupant, nickname);
			}

			public String getNickname() {
				return nickname;
			}

			public Occupant getOccupant() {
				return occupant;
			}

			public Presence getPresence() {
				return presence;
			}

			public Room getRoom() {
				return room;
			}

			public XMucUserElement getxUserElement() {
				return xUserElement;
			}

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public void setOccupant(Occupant occupant) {
				this.occupant = occupant;
			}

			public void setPresence(Presence presence) {
				this.presence = presence;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

			public void setxUserElement(XMucUserElement xUserElement) {
				this.xUserElement = xUserElement;
			}

		}

		void onOccupantComes(SessionObject sessionObject, Room room, Occupant occupant, String nickname);
	}

	public interface OccupantLeavedHandler extends EventHandler {

		public static class OccupantLeavedEvent extends JaxmppEvent<OccupantLeavedHandler> {

			private String nickname;

			private Occupant occupant;

			private Presence presence;

			private Room room;

			private XMucUserElement xUserElement;

			public OccupantLeavedEvent(SessionObject sessionObject, Occupant occupant, Room room) throws XMLException {
				super(sessionObject);
				this.occupant = occupant;
				this.room = room;
				this.nickname = occupant.getNickname();
			}

			public OccupantLeavedEvent(SessionObject sessionObject, Presence element, Room room, Occupant occupant,
					String nickname, XMucUserElement xUser) {
				super(sessionObject);
				this.occupant = occupant;
				this.room = room;
				this.presence = element;
				this.nickname = nickname;
				this.xUserElement = xUser;
			}

			@Override
			protected void dispatch(OccupantLeavedHandler handler) {
				handler.onOccupantLeaved(sessionObject, room, occupant);
			}

			public Occupant getOccupant() {
				return occupant;
			}

			public Room getRoom() {
				return room;
			}

			public void setOccupant(Occupant occupant) {
				this.occupant = occupant;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

		}

		void onOccupantLeaved(SessionObject sessionObject, Room room, Occupant occupant);
	}

	public interface PresenceErrorHandler extends EventHandler {

		public static class PresenceErrorEvent extends JaxmppEvent<PresenceErrorHandler> {

			private String nickname;

			private Presence presence;

			private Room room;

			public PresenceErrorEvent(SessionObject sessionObject, Presence element, Room room, String nickname) {
				super(sessionObject);
				this.presence = element;
				this.room = room;
				this.nickname = nickname;
			}

			@Override
			protected void dispatch(PresenceErrorHandler handler) {
				handler.onPresenceError(sessionObject, room, presence, nickname);
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

		void onPresenceError(SessionObject sessionObject, Room room, Presence presence, String nickname);
	}

	/**
	 * Local instance of Chat Room was closed because of, for example, presence
	 * error.
	 */
	public interface RoomClosedHandler extends EventHandler {

		public static class RoomClosedEvent extends JaxmppEvent<RoomClosedHandler> {

			private Presence presence;

			private Room room;

			public RoomClosedEvent(SessionObject sessionObject, Presence element, Room room) {
				super(sessionObject);
				this.room = room;
				this.presence = element;
			}

			@Override
			protected void dispatch(RoomClosedHandler handler) {
				handler.onRoomClosed(sessionObject, presence, room);
			}

			public Presence getPresence() {
				return presence;
			}

			public Room getRoom() {
				return room;
			}

			public void setPresence(Presence presence) {
				this.presence = presence;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

		}

		void onRoomClosed(SessionObject sessionObject, Presence presence, Room room);
	}

	public interface StateChangeHandler extends EventHandler {

		public static class StateChangeEvent extends JaxmppEvent<StateChangeHandler> {

			private State newState;

			private State oldState;

			private Room room;

			public StateChangeEvent(SessionObject sessionObject, Room room, State oldState, State newState) {
				super(sessionObject);
				this.room = room;
				this.oldState = oldState;
				this.newState = newState;
			}

			@Override
			protected void dispatch(StateChangeHandler handler) {
				handler.onStateChange(sessionObject, room, oldState, newState);
			}

		}

		void onStateChange(SessionObject sessionObject, Room room, State oldState, State newState);
	}

	public interface YouJoinedHandler extends EventHandler {

		public static class YouJoinedEvent extends JaxmppEvent<YouJoinedHandler> {

			private String nickname;

			private Occupant occupant;

			private Presence presence;

			private Room room;

			private XMucUserElement xUserElement;

			public YouJoinedEvent(SessionObject sessionObject, Presence element, Room room, Occupant occupant, String nickname,
					XMucUserElement xUser) {
				super(sessionObject);
				this.room = room;
				this.presence = element;
				this.occupant = occupant;
				this.nickname = nickname;
				this.xUserElement = xUser;
			}

			@Override
			protected void dispatch(YouJoinedHandler handler) {
				handler.onYouJoined(sessionObject, room, nickname);
			}

			public Room getRoom() {
				return room;
			}

			public void setRoom(Room room) {
				this.room = room;
			}

		}

		void onYouJoined(SessionObject sessionObject, Room room, String asNickname);
	}

	private final static Criteria DIRECT_INVITATION_CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("x", "jabber:x:conference"));

	private final static Criteria MEDIATED_INVITATION_CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("x", "http://jabber.org/protocol/muc#user")).add(ElementCriteria.name("invite"));

	private final static Criteria MEDIATED_INVITATION_DECLINED_CRIT = ElementCriteria.name("message").add(
			ElementCriteria.name("x", "http://jabber.org/protocol/muc#user")).add(ElementCriteria.name("decline"));

	public static final Integer STATUS_NEW_NICKNAME = 303;

	private final Criteria crit;

	private final DateTimeFormat dtf;

	private AbstractRoomsManager roomsManager;

	public MucModule() {
		AbstractRoomsManager cm = UniversalFactory.createInstance(AbstractRoomsManager.class.getName());
		if (cm == null) {
			cm = new DefaultRoomsManager();
		}
		roomsManager = cm;

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

	public MucModule(AbstractRoomsManager cm) {
		this();
		roomsManager = cm;
	}

	@Override
	public void beforeRegister() {
		super.beforeRegister();

		context.getEventBus().addHandler(Connector.StateChangedHandler.StateChangedEvent.class,
				new Connector.StateChangedHandler() {

					@Override
					public void onStateChanged(SessionObject sessionObject, tigase.jaxmpp.core.client.Connector.State oldState,
							tigase.jaxmpp.core.client.Connector.State newState) throws JaxmppException {
						onConnectorStateChanged(sessionObject, oldState, newState);
					}
				});

		this.roomsManager.setContext(this.context);
		this.roomsManager.initialize();

		this.context.getEventBus().addHandler(AbstractSessionObject.ClearedHandler.ClearedEvent.class,
				new AbstractSessionObject.ClearedHandler() {

					@Override
					public void onCleared(SessionObject sessionObject, Set<Scope> scopes) throws JaxmppException {
						onSessionObjectCleared(sessionObject, scopes);
					}
				});
	}

	// XXX What is it???
	// public void enable(Room room) throws JaxmppException {
	// YouJoinedEvent event = new YouJoinedEvent(context.getSessionObject(),
	// room);
	// fireEvent(event);
	// }

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

	public void declineInvitation(Invitation invitation, String reasonMsg) throws JaxmppException {
		if (invitation instanceof MediatedInvitation) {
			Message message = Message.create();
			message.setTo(JID.jidInstance(invitation.getRoomJID()));

			Element x = ElementFactory.create("x", null, "http://jabber.org/protocol/muc#user");
			message.addChild(x);

			Element decline = ElementFactory.create("decline");
			x.addChild(decline);
			if (reasonMsg != null) {
				Element reason = ElementFactory.create("reason", reasonMsg, null);
				decline.addChild(reason);
			}
			write(message);
		}

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

	public Room getRoom(BareJID roomJid) {
		return this.roomsManager.get(roomJid);
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

		Element x = message.addChild(ElementFactory.create("x", null, "http://jabber.org/protocol/muc#user"));
		Element invite = x.addChild(ElementFactory.create("invite"));
		invite.setAttribute("to", inviteeJID.toString());
		if (reason != null) {
			invite.addChild(ElementFactory.create("reason", reason, null));
		}

		write(message);
	}

	public void inviteDirectly(Room room, JID inviteeJID, String reason, String threadId) throws JaxmppException {
		Message message = Message.create();
		message.setTo(inviteeJID);

		Element x = message.addChild(ElementFactory.create("x", null, "jabber:x:conference"));
		x.setAttribute("jid", room.getRoomJid().toString());

		if (room.getPassword() != null)
			x.setAttribute("password", room.getPassword());

		if (reason != null)
			x.setAttribute("reason", reason);

		if (threadId != null) {
			x.setAttribute("thread", threadId);
			x.setAttribute("continue", "true");
		}

		write(message);
	}

	public Room join(final Invitation invitation, final String nickname) throws XMLException, JaxmppException {
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

		JoinRequestedEvent event = new JoinRequestedEvent(context.getSessionObject(), presence, nickname, room);
		fireEvent(event);

		return room;
	}

	public void leave(Room room) throws XMLException, JaxmppException {
		if (room.getState() == State.joined) {
			room.setState(State.not_joined);
			Presence presence = Presence.create();
			presence.setType(StanzaType.unavailable);
			presence.setTo(JID.jidInstance(room.getRoomJid(), room.getNickname()));
			write(presence);
		}

		this.roomsManager.remove(room);

		RoomClosedEvent event = new RoomClosedEvent(context.getSessionObject(), null, room);
		fireEvent(event);
	}

	protected void onConnectorStateChanged(SessionObject sessionObject, Connector.State oldState, Connector.State newState)
			throws XMLException, JaxmppException {
		if (newState == null || newState == Connector.State.disconnected || newState == Connector.State.disconnecting) {
			onNetworkDisconnected();
		}

	}

	protected void onNetworkDisconnected() throws XMLException, JaxmppException {
	}

	protected void onSessionObjectCleared(SessionObject sessionObject, Set<Scope> scopes) throws JaxmppException {
		if (scopes != null && scopes.contains(Scope.session)) {
			for (Room room : roomsManager.getRooms()) {
				room.setState(State.not_joined);

				ArrayList<Occupant> ocs = new ArrayList<Occupant>();
				ocs.addAll(room.getPresences().values());
				for (Occupant occupant : ocs) {
					OccupantLeavedEvent event = new OccupantLeavedEvent(context.getSessionObject(), occupant, room);
					room.remove(occupant);
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
		String cont = x.getAttribute("continue");

		DirectInvitation invitation = new DirectInvitation(context.getSessionObject());
		invitation.setMessage(message);
		invitation.setInviterJID(message.getFrom());
		invitation.setRoomJID(BareJID.bareJIDInstance(x.getAttribute("jid")));
		invitation.setPassword(x.getAttribute("password"));
		invitation.setReason(x.getAttribute("reason"));
		invitation.setThreadID(x.getAttribute("thread"));
		invitation.setContinueFlag(BooleanField.parse(cont));

		InvitationReceivedEvent event = new InvitationReceivedEvent(context.getSessionObject(), invitation);
		fireEvent(event);
	}

	protected void processInvitationDeclinedMessage(Message message) throws JaxmppException {
		final BareJID roomJid = message.getFrom().getBareJid();
		Room room = this.roomsManager.get(roomJid);
		if (room == null)
			return;

		final Element x = message.getChildrenNS("x", "http://jabber.org/protocol/muc#user");
		final Element decline = getFirstChild(x, "decline");
		final Element reason = getFirstChild(decline, "reason");

		InvitationDeclinedEvent event = new InvitationDeclinedEvent(context.getSessionObject(), message, room,
				decline.getAttribute("from") == null ? null : JID.jidInstance(decline.getAttribute("from")),
				reason == null ? null : reason.getValue());
		fireEvent(event);
	}

	protected void processMediatedInvitationMessage(Message message) throws JaxmppException {
		Element x = message.getChildrenNS("x", "http://jabber.org/protocol/muc#user");
		Element invite = getFirstChild(x, "invite");
		Element reason = getFirstChild(invite, "reason");
		Element password = getFirstChild(x, "password");
		String inviter = invite.getAttribute("from");

		MediatedInvitation invitation = new MediatedInvitation(context.getSessionObject());
		invitation.setMessage(message);
		invitation.setRoomJID(message.getFrom().getBareJid());

		if (inviter != null) {
			invitation.setInviterJID(JID.jidInstance(inviter));
		}

		if (reason != null) {
			invitation.setReason(reason.getValue());
		}

		if (password != null) {
			invitation.setPassword(password.getValue());
		}

		InvitationReceivedEvent event = new InvitationReceivedEvent(context.getSessionObject(), invitation);
		fireEvent(event);
	}

	protected void processMessage(Message message) throws JaxmppException {
		final JID from = message.getFrom();
		final BareJID roomJid = from.getBareJid();
		final String nickname = from.getResource();

		Room room = this.roomsManager.get(roomJid);
		if (room == null)
			return;
		// throw new XMPPException(ErrorCondition.service_unavailable);

		final Element delay = message.getChildrenNS("delay", "urn:xmpp:delay");
		Date delayTime;
		if (delay != null && delay.getAttribute("stamp") != null) {
			delayTime = dtf.parse(delay.getAttribute("stamp"));
		} else {
			delayTime = null;
		}
		delayTime = delayTime == null ? new Date() : delayTime;

		if (message.getType() == StanzaType.error) {
			MessageErrorEvent event = new MessageErrorEvent(context.getSessionObject(), message, room, nickname, delayTime);
			fireEvent(event);
		} else {
			// Disabled, because XEP says that status=110 must be sent with
			// presence
			// if (room.getState() != State.joined) {
			// room.setState(State.joined);
			// fireEvent(new YouJoinedEvent(context.getSessionObject(), element,
			// room, occupant, nickname, xUser));
			// }
			MucMessageReceivedEvent event = new MucMessageReceivedEvent(context.getSessionObject(), message, room, nickname,
					delayTime);
			fireEvent(event);
		}

		room.setLastMessageDate(delayTime);
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
			fireEvent(new RoomClosedEvent(context.getSessionObject(), element, room));
		} else if (element.getType() == StanzaType.error) {
			fireEvent(new PresenceErrorEvent(context.getSessionObject(), element, room, nickname));
			return;
		}

		if (nickname == null)
			return;

		if (element.getType() == StanzaType.unavailable && nickname.equals(room.getNickname())) {
			room.setState(State.not_joined);
			// this.roomsManager.remove(room.getRoomJid());
		} else if (room.getState() != State.joined) {
			// room.setState(State.joined);

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

		if ((presOld != null && presOld.getType() == null) && presNew.getType() == StanzaType.unavailable && xUser != null
				&& xUser.getStatuses().contains(STATUS_NEW_NICKNAME)) {
			String newNickName = xUser.getNick();
			room.remove(occupant);
			room.getTempOccupants().put(newNickName, occupant);
			log.finer(element.getFrom() + " wants to change nickname to " + newNickName);
		} else if (room.getState() != State.joined && xUser != null && xUser.getStatuses().contains(110)) {
			room.setState(State.joined);
			fireEvent(new YouJoinedEvent(context.getSessionObject(), element, room, occupant, nickname, xUser));
			occupant.setPresence(element);
			room.add(occupant);
		} else if ((presOld == null || presOld.getType() == StanzaType.unavailable) && presNew.getType() == null) {
			Occupant tmp = room.getTempOccupants().remove(nickname);
			if (tmp != null) {
				log.finer(element.getFrom() + " successfully changed nickname ");
				fireEvent(new OccupantChangedNickEvent(context.getSessionObject(), element, room, occupant, tmp.getNickname(),
						nickname));
				occupant = tmp;
			} else {
				fireEvent(new OccupantComesEvent(context.getSessionObject(), element, room, occupant, nickname, xUser));
			}
			occupant.setPresence(element);
			room.add(occupant);
		} else if ((presOld != null && presOld.getType() == null) && presNew.getType() == StanzaType.unavailable) {
			occupant.setPresence(element);
			room.remove(occupant);
			fireEvent(new OccupantLeavedEvent(context.getSessionObject(), element, room, occupant, nickname, xUser));
		} else {
			occupant.setPresence(element);
			fireEvent(new OccupantChangedPresenceEvent(context.getSessionObject(), element, room, occupant, nickname, xUser));
		}

		if (xUser != null && xUser.getStatuses().contains(201)) {
			fireEvent(new NewRoomCreatedEvent(context.getSessionObject(), room, element));
		}

	}

	public static abstract class RoomConfgurationAsyncCallback implements AsyncCallback {
		public abstract void onConfigurationReceived(JabberDataElement configuration) throws XMLException;

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			final Element query = responseStanza.getChildrenNS("query", OWNER_XMLNS);
			Element x = query.getChildrenNS("x", "jabber:x:data");

			JabberDataElement r = new JabberDataElement(x);

			onConfigurationReceived(r);
		}

	}

	public void getRoomConfiguration(Room room, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		iq.setTo(JID.jidInstance(room.getRoomJid()));

		iq.addChild(ElementFactory.create("query", null, OWNER_XMLNS));

		write(iq, asyncCallback);
	}

	public void setRoomConfiguration(Room room, JabberDataElement configuration, AsyncCallback asyncCallback)
			throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance(room.getRoomJid()));

		Element q = iq.addChild(ElementFactory.create("query", null, OWNER_XMLNS));

		if (configuration == null) {
			Element x = q.addChild(ElementFactory.create("x", null, "jabber:x:data"));
			x.setAttribute("type", "submit");
		} else {
			q.addChild(configuration.createSubmitableElement(XDataType.submit));
		}

		write(iq, asyncCallback);
	}
}