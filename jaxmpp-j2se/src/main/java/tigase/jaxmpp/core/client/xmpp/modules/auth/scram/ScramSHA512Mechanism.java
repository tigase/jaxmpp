/*
 * ScramSHA512Mechanism.java
 *
 * Tigase TTS-NG
 * Copyright (C) 2015-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License,
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

import tigase.jaxmpp.core.client.SessionObject;

public class ScramSHA512Mechanism
		extends AbstractScram {

	public ScramSHA512Mechanism() {
		super("SCRAM-SHA-512", "SHA-512", "Client Key".getBytes(UTF_CHARSET), "Server Key".getBytes(UTF_CHARSET));
	}

	@Override
	protected byte[] getBindData(BindType bindType, SessionObject sessionObject) {
		return null;
	}

	@Override
	protected BindType getBindType(SessionObject sessionObject) {
		return BindType.n;
	}
}
