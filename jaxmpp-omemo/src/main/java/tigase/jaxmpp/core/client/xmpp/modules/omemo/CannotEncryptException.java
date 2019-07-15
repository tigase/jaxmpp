package tigase.jaxmpp.core.client.xmpp.modules.omemo;

public class CannotEncryptException extends OMEMOException {

	public CannotEncryptException() {
	}

	public CannotEncryptException(String message) {
		super(message);
	}

	public CannotEncryptException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotEncryptException(Throwable cause) {
		super(cause);
	}
}
