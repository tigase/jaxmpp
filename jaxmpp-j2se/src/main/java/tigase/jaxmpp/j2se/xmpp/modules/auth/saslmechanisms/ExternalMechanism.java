/*
 * ExternalMechanism.java
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

package tigase.jaxmpp.j2se.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.AbstractSaslMechanism;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import javax.net.ssl.KeyManager;
import java.nio.charset.Charset;

public class ExternalMechanism
		extends AbstractSaslMechanism {

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
