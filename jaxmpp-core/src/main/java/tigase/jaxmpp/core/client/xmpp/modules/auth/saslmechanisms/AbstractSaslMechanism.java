/*
 * AbstractSaslMechanism.java
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

package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslMechanism;

public abstract class AbstractSaslMechanism
		implements SaslMechanism {

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
