package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;

import java.nio.charset.Charset;

public abstract class AbstractSaslMechanism implements SaslMechanism {

	protected final static Charset UTF_CHARSET = Charset.forName("UTF-8");

	public static final String SASL_COMPLETE_KEY = "SASL_COMPLETE_KEY";

	@Override
	public boolean isComplete(SessionObject sessionObject) {
		Boolean b = sessionObject.getProperty(SASL_COMPLETE_KEY);
		return b == null ? false : b;
	}

	protected void setComplete(SessionObject sessionObject, boolean complete) {
		sessionObject.setProperty(Scope.stream, SASL_COMPLETE_KEY, complete);
	}

}
