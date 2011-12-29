package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;

public class JidOnlyChatSelector implements ChatSelector {

	@Override
	public Chat getChat(final List<Chat> chats, final JID jid, final String threadId) {
		Chat chat = null;

		BareJID bareJID = jid.getBareJid();

		for (Chat c : chats) {
			if (c.getJid().getBareJid().equals(bareJID)) {
				chat = c;
				break;
			}
		}
		return chat;
	}

}
