package tigase.jaxmpp.core.client.xmpp.modules.auth.scram;

import org.junit.Assert;
import org.junit.Test;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.j2se.J2SESessionObject;

public class ScramMechanismTest {


	@Test
	public void testMessages() throws Exception {
		SessionObject sessionObject = new J2SESessionObject();
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");

		ScramMechanism scram = new ScramMechanism() {
			@Override
			protected String randomString() {
				return "fyko+d2lbbFgONRv9qkxdawL";
			}
		};

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals("n,,n=user,r=fyko+d2lbbFgONRv9qkxdawL", firstClientMessage);

		String serverFirstMessage = "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096";
		String clientLastMessage = new String(Base64.decode(scram.evaluateChallenge(Base64.encode(serverFirstMessage.getBytes()), sessionObject)));
		Assert.assertEquals("c=biws,r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,p=v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=", clientLastMessage);

		Assert.assertFalse(scram.isComplete(sessionObject));

		String serverLastMessage = "v=rmF9pqV8S7suAoZWja4dJRkFsKQ=";
		Assert.assertNull(scram.evaluateChallenge(Base64.encode(serverLastMessage.getBytes()), sessionObject));

		Assert.assertTrue(scram.isComplete(sessionObject));
	}

}