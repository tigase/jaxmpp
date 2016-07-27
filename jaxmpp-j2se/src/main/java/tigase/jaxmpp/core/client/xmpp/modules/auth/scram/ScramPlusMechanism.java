package tigase.jaxmpp.core.client.xmpp.modules.auth.scram;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import java.security.MessageDigest;
import java.security.cert.Certificate;

public class ScramPlusMechanism extends AbstractScram {

	public ScramPlusMechanism() {
		super("SCRAM-SHA-1-PLUS", "SHA1", "Client Key".getBytes(UTF_CHARSET), "Server Key".getBytes(UTF_CHARSET));
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
		if (sessionObject.getProperty(SocketConnector.TLS_SESSION_ID_KEY) != null)
			return BindType.tls_unique;
		else if (sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY) != null)
			return BindType.tls_server_end_point;
		else return BindType.n;
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		return (sessionObject.getProperty(SocketConnector.TLS_SESSION_ID_KEY) != null
				|| sessionObject.getProperty(SocketConnector.TLS_PEER_CERTIFICATE_KEY) != null)
				&& super.isAllowedToUse(sessionObject);
	}
}
