package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.JID;

class DefaultChatManager extends AbstractChatManager {

	private static long chatIds = 1;

	DefaultChatManager() {
		super();
	}

	@Override
	protected Chat createChatInstance(JID jid, String threadId) {
		Chat chat = new Chat(++chatIds, packetWriter);
		chat.setJid(jid);
		chat.setThreadId(threadId);
		return chat;
	}

}
