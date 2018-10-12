/*
 * ScramMechanismTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

package tigase.jaxmpp.core.client.xmpp.modules.auth.scram;

import org.junit.Assert;
import org.junit.Test;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;
import tigase.jaxmpp.j2se.J2SESessionObject;

public class ScramMechanismTest {

	@Test
	public void testForcingAuthzid_FALSE() throws Exception {
		SessionObject sessionObject = new J2SESessionObject();
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(SaslMechanism.FORCE_AUTHZID, false);
		sessionObject.setUserProperty(AuthModule.LOGIN_USER_NAME_KEY, "some-username");

		ScramMechanism scram = new ScramMechanism() {
			@Override
			protected String randomString() {
				return "fyko+d2lbbFgONRv9qkxdawL";
			}
		};

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals("n,,n=some-username,r=fyko+d2lbbFgONRv9qkxdawL", firstClientMessage);
	}

	@Test
	public void testForcingAuthzid_TRUE() throws Exception {
		SessionObject sessionObject = new J2SESessionObject();
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(SaslMechanism.FORCE_AUTHZID, true);

		ScramMechanism scram = new ScramMechanism() {
			@Override
			protected String randomString() {
				return "fyko+d2lbbFgONRv9qkxdawL";
			}
		};

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals("n,a=user@example.com,n=user,r=fyko+d2lbbFgONRv9qkxdawL", firstClientMessage);
	}

	@Test
	public void testForcingAuthzid_Unset() throws Exception {
		SessionObject sessionObject = new J2SESessionObject();
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(AuthModule.LOGIN_USER_NAME_KEY, "some-username");

		ScramMechanism scram = new ScramMechanism() {
			@Override
			protected String randomString() {
				return "fyko+d2lbbFgONRv9qkxdawL";
			}
		};

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals("n,a=user@example.com,n=some-username,r=fyko+d2lbbFgONRv9qkxdawL", firstClientMessage);
	}

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
		String clientLastMessage = new String(
				Base64.decode(scram.evaluateChallenge(Base64.encode(serverFirstMessage.getBytes()), sessionObject)));
		Assert.assertEquals("c=biws,r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,p=v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=",
							clientLastMessage);

		Assert.assertFalse(scram.isComplete(sessionObject));

		String serverLastMessage = "v=rmF9pqV8S7suAoZWja4dJRkFsKQ=";
		Assert.assertNull(scram.evaluateChallenge(Base64.encode(serverLastMessage.getBytes()), sessionObject));

		Assert.assertTrue(scram.isComplete(sessionObject));
	}

}
