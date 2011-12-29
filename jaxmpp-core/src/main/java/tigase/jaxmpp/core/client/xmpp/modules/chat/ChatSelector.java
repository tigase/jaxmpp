package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.List;

import tigase.jaxmpp.core.client.JID;

public interface ChatSelector {

	Chat getChat(List<Chat> chats, JID jid, String threadId);

}
