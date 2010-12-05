package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public interface XmppSessionLogic {

	public void bind() throws JaxmppException;

	public void unbind() throws JaxmppException;
}
