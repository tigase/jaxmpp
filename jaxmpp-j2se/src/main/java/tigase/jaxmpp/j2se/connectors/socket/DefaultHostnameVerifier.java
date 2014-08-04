package tigase.jaxmpp.j2se.connectors.socket;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

public class DefaultHostnameVerifier implements HostnameVerifier {

	private static final String IPv4_IPv6_PATTERN = "^(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))"
			+ "|([0-9a-fA-F:]{2,}(:([0-9]{1,3}\\.){3}[0-9]{1,3})?))$";

	protected static String extractCN(X500Principal principal) {
		String[] dd = principal.getName(X500Principal.RFC2253).split(",");
		for (String string : dd) {
			if (string.toLowerCase().startsWith("cn=")) {
				return string.substring(3);
			}
		}
		return null;
	}

	protected final Logger log;

	public DefaultHostnameVerifier() {
		this.log = Logger.getLogger(this.getClass().getName());

	}

	protected boolean match(final String hostname, final String altName) {
		if (hostname == null || hostname.isEmpty() || altName == null || altName.isEmpty())
			return false;

		final String normalizedAltName = altName.toLowerCase(Locale.US);
		if (!normalizedAltName.contains("*")) {
			return hostname.equals(normalizedAltName);
		}

		if (normalizedAltName.startsWith("*.")
				&& hostname.regionMatches(0, normalizedAltName, 2, normalizedAltName.length() - 2))
			return true;

		int asteriskIdx = normalizedAltName.indexOf('*');
		int dotIdx = normalizedAltName.indexOf('.');
		if (asteriskIdx > dotIdx) {
			return false;
		}

		if (!hostname.regionMatches(0, normalizedAltName, 0, asteriskIdx)) {
			return false;
		}

		int suffixLength = normalizedAltName.length() - (asteriskIdx + 1);
		int suffixStart = hostname.length() - suffixLength;
		if (hostname.indexOf('.', asteriskIdx) < suffixStart) {
			return false; // wildcard '*' can't match a '.'
		}
		if (!hostname.regionMatches(suffixStart, normalizedAltName, asteriskIdx + 1, suffixLength)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean verify(String hostname, SSLSession session) {
		try {
			Certificate[] certificates = session.getPeerCertificates();

			if (hostname.matches(IPv4_IPv6_PATTERN)) {
				return verifyIp(hostname, (X509Certificate) certificates[0]);
			} else {
				return verifyHostname(hostname, (X509Certificate) certificates[0]);
			}

		} catch (Exception e) {
			log.log(Level.FINE, "Can't validate hostname", e);
			return false;
		}
	}

	protected boolean verifyHostname(String hostname, X509Certificate x509Certificate) throws CertificateParsingException {
		boolean altNamePresents = false;
		for (List<?> entry : x509Certificate.getSubjectAlternativeNames()) {
			Integer altNameType = (Integer) entry.get(0);
			if (altNameType != 2)
				continue;
			altNamePresents = true;
			String altName = (String) entry.get(1);
			if (match(hostname, altName)) {
				return true;
			}
		}

		if (!altNamePresents) {
			X500Principal principal = x509Certificate.getSubjectX500Principal();
			String cn = extractCN(principal);
			if (cn != null) {
				return match(hostname, cn);
			}
		}
		return false;

	}

	protected boolean verifyIp(String ipAddr, X509Certificate x509Certificate) throws CertificateParsingException {
		for (List<?> entry : x509Certificate.getSubjectAlternativeNames()) {
			Integer altNameType = (Integer) entry.get(0);
			if (altNameType != 7)
				continue;
			String altName = (String) entry.get(1);
			if (ipAddr.equalsIgnoreCase(altName)) {
				return true;
			}
		}
		return false;
	}

}
