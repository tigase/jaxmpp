package tigase.jaxmpp.core.client.xmpp.modules.chat;

/**
 * @author bmalkow
 * 
 */
public enum Affiliation {
	admin(30, true, true, true, true, true, true, true, false, false, false, false, true),
	member(20, true, true, true, true, false, false, false, false, false, false, false, false),
	none(10, true, true, false, false, false, false, false, false, false, false, false, false),
	outcast(0, false, false, false, false, false, false, false, false, false, false, false, false),
	owner(40, true, true, true, true, true, true, true, true, true, true, true, true);

	private final boolean banMembersAndUnaffiliatedUsers;

	private final boolean changeRoomDefinition;

	private final boolean destroyRoom;

	private final boolean editAdminList;

	private final boolean editMemberList;

	private final boolean editModeratorList;

	private final boolean editOwnerList;

	private final boolean enterMembersOnlyRoom;

	private final boolean enterOpenRoom;

	private final boolean registerWithOpenRoom;

	private final boolean retrieveMemberList;

	private final boolean viewOccupantsJid;

	private final int weight;

	private Affiliation(int weight, boolean enterOpenRoom, boolean registerWithOpenRoom, boolean retrieveMemberList,
			boolean enterMembersOnlyRoom, boolean banMembersAndUnaffiliatedUsers, boolean editMemberList,
			boolean editModeratorList, boolean editAdminList, boolean editOwnerList, boolean changeRoomDefinition,
			boolean destroyRoom, boolean viewOccupantsJid) {
		this.weight = weight;
		this.enterOpenRoom = enterOpenRoom;
		this.registerWithOpenRoom = registerWithOpenRoom;
		this.retrieveMemberList = retrieveMemberList;
		this.enterMembersOnlyRoom = enterMembersOnlyRoom;
		this.banMembersAndUnaffiliatedUsers = banMembersAndUnaffiliatedUsers;
		this.editMemberList = editMemberList;
		this.editModeratorList = editModeratorList;
		this.editAdminList = editAdminList;
		this.editOwnerList = editOwnerList;
		this.changeRoomDefinition = changeRoomDefinition;
		this.destroyRoom = destroyRoom;
		this.viewOccupantsJid = viewOccupantsJid;
	}

	public int getWeight() {
		return weight;
	}

	public boolean isBanMembersAndUnaffiliatedUsers() {
		return banMembersAndUnaffiliatedUsers;
	}

	public boolean isChangeRoomDefinition() {
		return changeRoomDefinition;
	}

	public boolean isDestroyRoom() {
		return destroyRoom;
	}

	public boolean isEditAdminList() {
		return editAdminList;
	}

	public boolean isEditMemberList() {
		return editMemberList;
	}

	public boolean isEditModeratorList() {
		return editModeratorList;
	}

	public boolean isEditOwnerList() {
		return editOwnerList;
	}

	public boolean isEnterMembersOnlyRoom() {
		return enterMembersOnlyRoom;
	}

	public boolean isEnterOpenRoom() {
		return enterOpenRoom;
	}

	public boolean isRegisterWithOpenRoom() {
		return registerWithOpenRoom;
	}

	public boolean isRetrieveMemberList() {
		return retrieveMemberList;
	}

	public boolean isViewOccupantsJid() {
		return viewOccupantsJid;
	}
}
