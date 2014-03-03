package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

public interface MessageModuleExtension {

	Message beforeMessageProcess(Message message, Chat chat) throws JaxmppException;

}
