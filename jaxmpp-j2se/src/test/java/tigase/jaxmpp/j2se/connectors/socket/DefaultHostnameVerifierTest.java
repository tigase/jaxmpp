package tigase.jaxmpp.j2se.connectors.socket;

import junit.framework.TestCase;

public class DefaultHostnameVerifierTest
		extends TestCase {

	private final DefaultHostnameVerifier verifier = new DefaultHostnameVerifier();

	public void testMatch() {
		// exact domain matches
		assertTrue(verifier.match("example.com", "example.com"));
		assertFalse(verifier.match("example.com", "example.con"));
		assertFalse(verifier.match("example.com", "examp1e.com"));

		// base domain with wildcard
		assertTrue(verifier.match("example.com", "*.example.com"));
		assertTrue(verifier.match("example.com", "*.com"));
		assertFalse(verifier.match("example.com", "*"));
		assertFalse(verifier.match("example.com", "*.example.co"));
		assertFalse(verifier.match("exa.ple.com", "exa*ple.com"));
		assertTrue(verifier.match("example.com", "exa*ple.com"));
		assertTrue(verifier.match("exammple.com", "exa*ple.com"));

		// subdomain with wildcard
		assertTrue(verifier.match("test.example.com", "*.example.com"));
		assertFalse(verifier.match("test.example.com", "xtest.example.com"));
		assertFalse(verifier.match("test.example.com", "test1.example.com"));
		assertTrue(verifier.match("test.example.com", "test.*.com"));
		assertTrue(verifier.match("test.example.com", "test.example.*"));
		assertFalse(verifier.match("test.example.com", "*.example.co"));
	}
}