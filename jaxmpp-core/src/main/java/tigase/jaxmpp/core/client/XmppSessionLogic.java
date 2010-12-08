package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public interface XmppSessionLogic {

	public static interface SessionListener {

		void onException(JaxmppException e);
	}

	public void bind(SessionListener listener) throws JaxmppException;

	public void unbind() throws JaxmppException;
}
