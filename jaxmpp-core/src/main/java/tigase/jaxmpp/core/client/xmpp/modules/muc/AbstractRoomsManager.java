package tigase.jaxmpp.core.client.xmpp.modules.muc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.observer.Observable;

public abstract class AbstractRoomsManager {

	protected Observable observable;

	protected PacketWriter packetWriter;

	protected final Map<BareJID, Room> rooms = new HashMap<BareJID, Room>();

	protected SessionObject sessionObject;

	public boolean contains(BareJID roomJid) {
		return this.rooms.containsKey(roomJid);
	}

	protected abstract Room createRoomInstance(final BareJID roomJid, final String nickname, final String password);

	public Room get(BareJID roomJid) {
		return this.rooms.get(roomJid);
	}

	Observable getObservable() {
		return observable;
	}

	PacketWriter getPacketWriter() {
		return packetWriter;
	}

	public Collection<Room> getRooms() {
		return this.rooms.values();
	}

	SessionObject getSessionObject() {
		return sessionObject;
	}

	protected void initialize() {

	}

	public void register(Room room) {
		this.rooms.put(room.getRoomJid(), room);
	}

	public boolean remove(Room room) {
		return this.rooms.remove(room.getRoomJid()) != null;
	}

	void setObservable(Observable observable) {
		this.observable = observable;
	}

	void setPacketWriter(PacketWriter packetWriter) {
		this.packetWriter = packetWriter;
	}

	void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}
}
