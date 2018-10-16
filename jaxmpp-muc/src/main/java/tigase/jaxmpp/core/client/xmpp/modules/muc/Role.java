/*
 * Role.java
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
package tigase.jaxmpp.core.client.xmpp.modules.muc;

/**
 * @author bmalkow
 */
public enum Role {

	moderator(3, true, true, true, true, true, true, true, true, true, true, true, true, true),
	none(0, false, false, false, false, false, false, false, false, false, false, false, false, false),
	participant(2, true, true, true, true, true, true, true, true, true, false, false, false, false),
	visitor(1, true, true, true, true, true, true, true, true, false, false, false, false, false);

	private final boolean changeAvailabilityStatus;

	private final boolean changeRoomNickname;

	private final boolean grantVoice;

	private final boolean inviteOtherUsers;

	private final boolean kickParticipantsAndVisitors;

	private final boolean modifySubject;

	private final boolean presenceBroadcastedToRoom;

	private final boolean presentInRoom;

	private final boolean receiveMessages;

	private final boolean receiveOccupantPresence;

	private final boolean revokeVoice;

	private final boolean sendMessagesToAll;

	private final boolean sendPrivateMessages;

	private final int weight;

	Role(int weight, boolean presentInRoom, boolean receiveMessages, boolean receiveOccupantPresence,
		 boolean presenceBroadcastedToRoom, boolean changeAvailabilityStatus, boolean changeRoomNickname,
		 boolean sendPrivateMessages, boolean inviteOtherUsers, boolean sendMessagesToAll, boolean modifySubject,
		 boolean kickParticipantsAndVisitors, boolean grantVoice, boolean revokeVoice) {
		this.weight = weight;
		this.presentInRoom = presentInRoom;
		this.receiveMessages = receiveMessages;
		this.receiveOccupantPresence = receiveOccupantPresence;
		this.presenceBroadcastedToRoom = presenceBroadcastedToRoom;
		this.changeAvailabilityStatus = changeAvailabilityStatus;
		this.changeRoomNickname = changeRoomNickname;
		this.sendPrivateMessages = sendPrivateMessages;
		this.inviteOtherUsers = inviteOtherUsers;
		this.sendMessagesToAll = sendMessagesToAll;
		this.modifySubject = modifySubject;
		this.kickParticipantsAndVisitors = kickParticipantsAndVisitors;
		this.grantVoice = grantVoice;
		this.revokeVoice = revokeVoice;
	}

	public int getWeight() {
		return weight;
	}

	public boolean isChangeAvailabilityStatus() {
		return changeAvailabilityStatus;
	}

	public boolean isChangeRoomNickname() {
		return changeRoomNickname;
	}

	public boolean isGrantVoice() {
		return grantVoice;
	}

	public boolean isInviteOtherUsers() {
		return inviteOtherUsers;
	}

	public boolean isKickParticipantsAndVisitors() {
		return kickParticipantsAndVisitors;
	}

	public boolean isModifySubject() {
		return modifySubject;
	}

	public boolean isPresenceBroadcastedToRoom() {
		return presenceBroadcastedToRoom;
	}

	public boolean isPresentInRoom() {
		return presentInRoom;
	}

	public boolean isReceiveMessages() {
		return receiveMessages;
	}

	public boolean isReceiveOccupantPresence() {
		return receiveOccupantPresence;
	}

	public boolean isRevokeVoice() {
		return revokeVoice;
	}

	public boolean isSendMessagesToAll() {
		return sendMessagesToAll;
	}

	public boolean isSendPrivateMessages() {
		return sendPrivateMessages;
	}
}