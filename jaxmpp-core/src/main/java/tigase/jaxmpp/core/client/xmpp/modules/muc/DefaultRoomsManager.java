package tigase.jaxmpp.core.client.xmpp.modules.muc;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.observer.Observable;

public class DefaultRoomsManager extends AbstractRoomsManager {

	protected static long chatIds = 1;

	@Override
	public Room createRoomInstance(Observable observable, BareJID roomJid, String nickname, String password) {
		Room room = new Room(chatIds++, packetWriter, roomJid, nickname, sessionObject);
		room.setPassword(password);
		room.setObservable(observable);

		return room;
	}

}
