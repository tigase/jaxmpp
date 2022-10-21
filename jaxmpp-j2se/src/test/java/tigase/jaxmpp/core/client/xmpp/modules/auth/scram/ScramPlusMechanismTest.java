/*
 * ScramPlusMechanismTest.java
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

package tigase.jaxmpp.core.client.xmpp.modules.auth.scram;

import org.junit.Assert;
import org.junit.Test;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.j2se.J2SESessionObject;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import java.util.Collections;
import java.util.List;

public class ScramPlusMechanismTest {

	@Test
	public void testMessages() throws Exception {
		SessionObject sessionObject = new J2SESessionObject();
		sessionObject.setProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("bmalkow@example.com"));
		sessionObject.setProperty(SessionObject.PASSWORD, "123456");
		sessionObject.setProperty(SocketConnector.TLS_SESSION_ID_KEY, new byte[]{'D', 'P', 'I'});

		ScramPlusMechanism scram = new ScramPlusMechanism() {
			@Override
			protected String randomString() {
				return "SpiXKmhi57DBp5sdE5G3H3ms";
			}

			// override to force usage of tls_unique
			@Override
			protected List<BindType> getServerBindTypes(SessionObject sessionObject) {
				return Collections.singletonList(BindType.tls_unique);
			}
		};

		String firstClientMessage = new String(Base64.decode(scram.evaluateChallenge(null, sessionObject)));
		Assert.assertEquals("p=tls-unique,,n=bmalkow,r=SpiXKmhi57DBp5sdE5G3H3ms", firstClientMessage);

		String serverFirstMessage = "r=SpiXKmhi57DBp5sdE5G3H3ms5kLrhitKUHVoSOmzdR,s=Ey6OJnGx7JEJAIJp,i=4096";
		String clientLastMessage = new String(
				Base64.decode(scram.evaluateChallenge(Base64.encode(serverFirstMessage.getBytes()), sessionObject)));
		Assert.assertEquals(
				"c=cD10bHMtdW5pcXVlLCxEUEk=,r=SpiXKmhi57DBp5sdE5G3H3ms5kLrhitKUHVoSOmzdR,p=+zQvUd4nQqo03thSCcc2K6gueD4=",
				clientLastMessage);

		Assert.assertFalse(scram.isComplete(sessionObject));

		String serverLastMessage = "v=NQ/f8FjeMxUuRK9F88G8tMji4pk=";
		Assert.assertNull(scram.evaluateChallenge(Base64.encode(serverLastMessage.getBytes()), sessionObject));

		Assert.assertTrue(scram.isComplete(sessionObject));
	}

}