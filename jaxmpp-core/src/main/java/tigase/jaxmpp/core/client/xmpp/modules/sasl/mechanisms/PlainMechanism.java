package tigase.jaxmpp.core.client.xmpp.modules.sasl.mechanisms;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.CredentialsCallback;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslMechanism;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;

public class PlainMechanism implements SaslMechanism {

	private static final String NULL = String.valueOf((char) 0);

	public PlainMechanism() {
	}

	public String evaluateChallenge(String input, SessionObject sessionObject) {
		if (input == null) {
			CredentialsCallback callback = sessionObject.getProperty(SaslModule.SASL_CREDENTIALS_CALLBACK);
			if (callback == null)
				callback = new SaslModule.DefaultCredentialsCallback(sessionObject);
			String lreq = NULL + callback.getUsername() + NULL + callback.getPassword();

			String base64 = Base64.encodeString(lreq);
			return base64;
		}
		return null;
	}

	public Status getStatus() {
		return null;
	}

	public String getStatusMessage() {
		return null;
	}

	public boolean isComplete() {
		return false;
	}

	public String name() {
		return "PLAIN";
	}

}
