package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;

public class DefaultChatSelector implements ChatSelector {

	@Override
	public Chat getChat(final List<Chat> chats, final JID jid, final String threadId) {
		Chat chat = null;

		BareJID bareJID = jid.getBareJid();

		for (Chat c : chats) {
			if (!c.getJid().getBareJid().equals(bareJID)) {
				continue;
			}
			if (threadId != null && c.getThreadId() != null && threadId.equals(c.getThreadId())) {
				chat = c;
				break;
			}
			if (jid.getResource() != null && c.getJid().getResource() != null
					&& jid.getResource().equals(c.getJid().getResource())) {
				chat = c;
				break;
			}
			if (c.getJid().getResource() == null) {
				c.setJid(jid);
				chat = c;
				break;
			}

		}
		return chat;
	}

}
