package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.CredentialsCallback;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;

public class PlainMechanism implements SaslMechanism {

	private static final String NULL = String.valueOf((char) 0);

	public PlainMechanism() {
	}

	@Override
	public String evaluateChallenge(String input, SessionObject sessionObject) {
		if (input == null) {
			CredentialsCallback callback = sessionObject.getProperty(AuthModule.CREDENTIALS_CALLBACK);
			if (callback == null)
				callback = new AuthModule.DefaultCredentialsCallback(sessionObject);
			JID userJID = sessionObject.getProperty(SessionObject.USER_JID);
			String lreq = userJID.toString() + NULL + userJID.getLocalpart() + NULL + callback.getPassword();

			String base64 = Base64.encodeString(lreq);
			return base64;
		}
		return null;
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public String getStatusMessage() {
		return null;
	}

	@Override
	public boolean isAllowedToUse(final SessionObject sessionObject) {
		return sessionObject.getProperty(SessionObject.PASSWORD) != null
				&& sessionObject.getProperty(SessionObject.USER_JID) != null;
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public String name() {
		return "PLAIN";
	}

}
