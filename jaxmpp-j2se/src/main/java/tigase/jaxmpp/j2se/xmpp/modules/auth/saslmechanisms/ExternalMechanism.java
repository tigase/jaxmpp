package tigase.jaxmpp.j2se.xmpp.modules.auth.saslmechanisms;

import javax.net.ssl.KeyManager;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.AbstractSaslMechanism;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import java.nio.charset.Charset;

public class ExternalMechanism extends AbstractSaslMechanism {

	private final static Charset UTF_CHARSET = Charset.forName("UTF-8");

	@Override
	public String evaluateChallenge(String input, SessionObject sessionObject) {
		BareJID jid = sessionObject.getProperty(SessionObject.USER_BARE_JID);
		setComplete(sessionObject, true);
		if (jid == null) {
			return "=";
		} else {
			return Base64.encode(jid.toString().getBytes(UTF_CHARSET));
		}
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		KeyManager[] kms = sessionObject.getProperty(SocketConnector.KEY_MANAGERS_KEY);
		Boolean sext = sessionObject.getProperty(SocketConnector.SASL_EXTERNAL_ENABLED_KEY);
		return kms != null && sext != null && sext.booleanValue();
	}

	@Override
	public String name() {
		return "EXTERNAL";
	}

}
