package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public class OMEMOException extends JaxmppException {

	public OMEMOException() {
	}

	public OMEMOException(String message) {
		super(message);
	}

	public OMEMOException(String message, Throwable cause) {
		super(message, cause);
	}

	public OMEMOException(Throwable cause) {
		super(cause);
	}
}
