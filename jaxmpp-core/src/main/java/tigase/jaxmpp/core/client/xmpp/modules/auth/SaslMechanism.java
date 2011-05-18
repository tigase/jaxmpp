package tigase.jaxmpp.core.client.xmpp.modules.auth;

import tigase.jaxmpp.core.client.SessionObject;

public interface SaslMechanism {

	public static enum Status {
		CONTINUE,
		ERROR,
		SUCCESS
	}

	String evaluateChallenge(String input, SessionObject sessionObject);

	Status getStatus();

	String getStatusMessage();

	boolean isAllowedToUse(SessionObject sessionObject);

	boolean isComplete();

	String name();

}
