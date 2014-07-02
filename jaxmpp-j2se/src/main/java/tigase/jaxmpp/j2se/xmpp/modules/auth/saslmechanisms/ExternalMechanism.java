package tigase.jaxmpp.j2se.xmpp.modules.auth.saslmechanisms;

import javax.net.ssl.KeyManager;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

public class ExternalMechanism implements SaslMechanism {

	private boolean complete = false;

	@Override
	public String evaluateChallenge(String input, SessionObject sessionObject) {
		BareJID jid = sessionObject.getProperty(SessionObject.USER_BARE_JID);
		complete = true;
		if (jid == null) {
			return "=";
		} else {
			return Base64.encode(jid.toString().getBytes());
		}
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		KeyManager[] kms = sessionObject.getProperty(SocketConnector.KEY_MANAGERS_KEY);
		Boolean sext = sessionObject.getProperty(SocketConnector.SASL_EXTERNAL_ENABLED_KEY);
		return kms != null && sext != null && sext.booleanValue();
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public String name() {
		return "EXTERNAL";
	}

}
