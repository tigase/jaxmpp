/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.XOAuth2TokenCallback;

public class XOAuth2Mechanism extends AbstractSaslMechanism {

	public static final String X_OAUTH2_TOKEN_CALLBACK_KEY = "X_OAUTH2_TOKEN_CALLBACK";
	public static final String X_OAUTH2_TOKEN_KEY = "X_OAUTH2_TOKEN";
	private static final String NULL = String.valueOf((char) 0);

	public XOAuth2Mechanism() {
	}

	@Override
	public String evaluateChallenge(String input, SessionObject sessionObject) {
		if (!isComplete(sessionObject)) {
			XOAuth2TokenCallback callback = sessionObject.getProperty(X_OAUTH2_TOKEN_CALLBACK_KEY);
			if (callback == null)
				callback = new DefaultXOAuth2TokenCallback(sessionObject);
			BareJID userJID = sessionObject.getProperty(SessionObject.USER_BARE_JID);
			String lreq = NULL + userJID.getLocalpart() + NULL + callback.getCredential();

			String base64 = Base64.encode(lreq.getBytes(UTF_CHARSET));
			setComplete(sessionObject, true);
			return base64;
		} else
			return null;
	}

	@Override
	public boolean isAllowedToUse(final SessionObject sessionObject) {
		return (sessionObject.getProperty(X_OAUTH2_TOKEN_KEY) != null
				|| sessionObject.getProperty(X_OAUTH2_TOKEN_CALLBACK_KEY) != null)
				&& sessionObject.getProperty(SessionObject.USER_BARE_JID) != null;
	}

	@Override
	public String name() {
		return "X-OAUTH2";
	}

	private class DefaultXOAuth2TokenCallback implements XOAuth2TokenCallback {

		private SessionObject sessionObject;

		public DefaultXOAuth2TokenCallback(SessionObject sessionObject) {
			this.sessionObject = sessionObject;
		}

		@Override
		public String getCredential() {
			return sessionObject.getProperty(X_OAUTH2_TOKEN_KEY);
		}
	}
}