/*
 * ScramPlusMechanism.java
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

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScramPlusMechanism
		extends AbstractScram {

	public ScramPlusMechanism() {
		super("SCRAM-SHA-1-PLUS", "SHA1", "Client Key".getBytes(UTF_CHARSET), "Server Key".getBytes(UTF_CHARSET));
	}

	protected ScramPlusMechanism(String mechanismName, String algorithm, byte[] clientKey, byte[] serverKey) {
		super(mechanismName, algorithm, clientKey, serverKey);
	}

	private byte[] calculateHash(final X509Certificate cert) {
		try {
			String usealgo;
			final String algo =  cert.getSigAlgName();
			int withIdx = algo.indexOf("with");
			if (withIdx <= 0) {
				throw new RuntimeException("Unable to parse SigAlgName: " + algo);
			}
			usealgo = algo.substring(0, withIdx);
			if (usealgo.equalsIgnoreCase("MD5") || usealgo.equalsIgnoreCase("SHA1")) {
				usealgo = "SHA-256";
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
				X509Certificate peerCertificate = sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY);
				return calculateHash(peerCertificate);
			default:
				return null;
		}
	}

	@Override
	protected BindType getBindType(SessionObject sessionObject) {
		List<BindType> serverSupportedTypes = getServerBindTypes(sessionObject);
		if (sessionObject.getProperty(SocketConnector.TLS_SESSION_ID_KEY) != null && serverSupportedTypes.contains(BindType.tls_unique)) {
			return BindType.tls_unique;
		} else if (sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY) != null && serverSupportedTypes.contains(BindType.tls_server_end_point)) {
			return BindType.tls_server_end_point;
		} else {
			return BindType.n;
		}
	}

	protected List<BindType> getServerBindTypes(SessionObject sessionObject) {
		try {
			Element features = StreamFeaturesModule.getStreamFeatures(sessionObject);
			if (features == null) {
				return Collections.emptyList();
			}
			Element bindings = features.getChildrenNS("sasl-channel-binding", "urn:xmpp:sasl-cb:0");
			if (bindings == null || bindings.getChildren() == null) {
				return Collections.emptyList();
			}
			List<BindType> result = new ArrayList<>();
			for (Element child : bindings.getChildren()) {
				String type = child.getAttribute("type");
				if (type != null) {
					if ("tls-server-end-point".equals(type)) {
						result.add(BindType.tls_server_end_point);
					} else if ("tls-unique".equals(type)) {
						result.add(BindType.tls_unique);
					}
				}
			}
			return result;
		} catch (XMLException ex) {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		return getBindType(sessionObject) != BindType.n && super.isAllowedToUse(sessionObject);
	}
}
