package tigase.jaxmpp.core.client.xmpp.modules.auth;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public class ClientSaslException extends JaxmppException {

	public ClientSaslException() {
		super();
	}

	public ClientSaslException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientSaslException(String message) {
		super(message);
	}

	public ClientSaslException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
