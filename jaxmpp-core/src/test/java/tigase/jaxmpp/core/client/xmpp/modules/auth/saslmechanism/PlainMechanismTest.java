package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanism;

import org.junit.Assert;
import org.junit.Test;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.MockSessionObject;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.PlainMechanism;

public class PlainMechanismTest {

	private static final String NULL = String.valueOf((char) 0);
	
	@Test
	public void testForcingAuthzid_Unset() throws Exception {
		SessionObject sessionObject = new MockSessionObject(null);
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(AuthModule.LOGIN_USER_NAME_KEY, "some-username");

		PlainMechanism scram = new PlainMechanism();

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals(generateMessage("user@example.com", "some-username", "pencil"), firstClientMessage);
	}

	@Test
	public void testForcingAuthzid_TRUE() throws Exception {
		SessionObject sessionObject = new MockSessionObject(null);
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(SaslMechanism.FORCE_AUTHZID, true);

		PlainMechanism scram = new PlainMechanism();

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals(generateMessage("user@example.com", "user", "pencil"), firstClientMessage);
	}

	@Test
	public void testForcingAuthzid_FALSE() throws Exception {
		SessionObject sessionObject = new MockSessionObject(null);
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(SaslMechanism.FORCE_AUTHZID, false);
		sessionObject.setUserProperty(AuthModule.LOGIN_USER_NAME_KEY, "some-username");

		PlainMechanism scram = new PlainMechanism();

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals(generateMessage(null, "some-username", "pencil"), firstClientMessage);
	}

	private String generateMessage(String authzid, String authcid, String password) {
		StringBuilder sb = new StringBuilder();
		if (authzid != null) {
			sb.append(authzid);
		}
		sb.append(NULL);
		if (authcid != null) {
			sb.append(authcid);
		}
		sb.append(NULL);
		if (password != null) {
			sb.append(password);
		}
		return sb.toString();
	}
}
