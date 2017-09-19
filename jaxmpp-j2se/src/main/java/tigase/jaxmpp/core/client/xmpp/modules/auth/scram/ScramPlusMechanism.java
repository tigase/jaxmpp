/*
 * ScramPlusMechanism.java
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

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import java.security.MessageDigest;
import java.security.cert.Certificate;

public class ScramPlusMechanism
		extends AbstractScram {

	public ScramPlusMechanism() {
		super("SCRAM-SHA-1-PLUS", "SHA1", "Client Key".getBytes(UTF_CHARSET), "Server Key".getBytes(UTF_CHARSET));
	}

	protected ScramPlusMechanism(String mechanismName, String algorithm, byte[] clientKey, byte[] serverKey) {
		super(mechanismName, algorithm, clientKey, serverKey);
	}

	private byte[] calculateHash(final Certificate cert) {
		try {
			final String usealgo;
			final String algo = cert.getPublicKey().getAlgorithm();
			if (algo.equals("MD5") || algo.equals("SHA-1")) {
				usealgo = "SHA-256";
			} else {
				usealgo = algo;
			}
			final MessageDigest md = MessageDigest.getInstance(usealgo);
			final byte[] der = cert.getEncoded();
			md.update(der);
			return md.digest();
		} catch (Exception e) {
			throw new RuntimeException("Cannot calculate certificate hash", e);
		}
	}

	@Override
	protected byte[] getBindData(BindType bindType, SessionObject sessionObject) {
		switch (bindType) {
			case tls_unique:
				return sessionObject.getProperty(SocketConnector.TLS_SESSION_ID_KEY);
			case tls_server_end_point:
				Certificate peerCertificate = sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY);
				return calculateHash(peerCertificate);
			default:
				return null;
		}
	}

	@Override
	protected BindType getBindType(SessionObject sessionObject) {
		if (sessionObject.getProperty(SocketConnector.TLS_SESSION_ID_KEY) != null) {
			return BindType.tls_unique;
		} else if (sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY) != null) {
			return BindType.tls_server_end_point;
		} else {
			return BindType.n;
		}
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		return (sessionObject.getProperty(SocketConnector.TLS_SESSION_ID_KEY) != null ||
				sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY) != null) &&
				super.isAllowedToUse(sessionObject);
	}
}
