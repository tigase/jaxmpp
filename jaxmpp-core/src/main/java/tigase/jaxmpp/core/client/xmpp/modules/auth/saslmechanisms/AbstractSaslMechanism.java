package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;

public abstract class AbstractSaslMechanism implements SaslMechanism {

	public static final String SASL_COMPLETE_KEY = "SASL_COMPLETE_KEY";

	@Override
	public boolean isComplete(SessionObject sessionObject) {
		Boolean b = sessionObject.getProperty(SASL_COMPLETE_KEY);
		return b == null ? false : b;
	}

	protected void setComplete(SessionObject sessionObject, boolean complete) {
		sessionObject.setProperty(Scope.session, SASL_COMPLETE_KEY, complete);
	}

}
