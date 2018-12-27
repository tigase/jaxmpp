/*
 * PlainMechanismTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
	public void testForcingAuthzid_Unset() throws Exception {
		SessionObject sessionObject = new MockSessionObject(null);
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("user@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "pencil");
		sessionObject.setUserProperty(AuthModule.LOGIN_USER_NAME_KEY, "some-username");

		PlainMechanism scram = new PlainMechanism();

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals(generateMessage("user@example.com", "some-username", "pencil"), firstClientMessage);
	}
}
