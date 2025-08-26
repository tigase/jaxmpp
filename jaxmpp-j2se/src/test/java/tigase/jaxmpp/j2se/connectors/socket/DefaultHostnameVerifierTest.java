/*
 * DefaultHostnameVerifierTest.java
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