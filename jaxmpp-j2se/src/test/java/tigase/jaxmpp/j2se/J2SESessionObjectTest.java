/*
 * J2SESessionObjectTest.java
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

package tigase.jaxmpp.j2se;

import org.junit.Before;
import org.junit.Test;
import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.j2se.eventbus.ThreadSafeEventBus;

import java.util.Set;

import static org.junit.Assert.*;

public class J2SESessionObjectTest {

	protected AbstractSessionObject sessionObject;

	@Before
	public void setUp() throws Exception {
		sessionObject = new J2SESessionObject();
		sessionObject.setEventBus(new ThreadSafeEventBus());
	}

	@Test
	public void testReset1() {
		sessionObject.setProperty(Scope.user, "u", "value1");
		sessionObject.setProperty(Scope.session, "se", "value2");
		sessionObject.setProperty(Scope.stream, "st", "value3");

		assertNotNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNotNull(sessionObject.getProperty("st"));

		try {
			sessionObject.clear();
		} catch (JaxmppException e) {
			fail(e.getMessage());
		}

		assertNotNull(sessionObject.getProperty("u"));
		assertNull(sessionObject.getProperty("se"));
		assertNull(sessionObject.getProperty("st"));

	}

	@Test
	public void testReset1_1() {
		sessionObject.setProperty(Scope.user, "u", "value1");
		sessionObject.setProperty(Scope.session, "se", "value2");
		sessionObject.setProperty(Scope.stream, "st", "value3");

		assertNotNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNotNull(sessionObject.getProperty("st"));

		try {
			sessionObject.clear((Scope[]) null);
		} catch (JaxmppException e) {
			fail(e.getMessage());
		}

		assertNotNull(sessionObject.getProperty("u"));
		assertNull(sessionObject.getProperty("se"));
		assertNull(sessionObject.getProperty("st"));

	}

	@Test
	public void testReset1_2() {
		sessionObject.setProperty(Scope.user, "u", "value1");
		sessionObject.setProperty(Scope.session, "se", "value2");
		sessionObject.setProperty(Scope.stream, "st", "value3");

		assertNotNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNotNull(sessionObject.getProperty("st"));

		try {
			sessionObject.clear((Set<Scope>) null);
		} catch (JaxmppException e) {
			fail(e.getMessage());
		}

		assertNotNull(sessionObject.getProperty("u"));
		assertNull(sessionObject.getProperty("se"));
		assertNull(sessionObject.getProperty("st"));

	}

	@Test
	public void testReset2() {
		sessionObject.setProperty(Scope.user, "u", "value1");
		sessionObject.setProperty(Scope.session, "se", "value2");
		sessionObject.setProperty(Scope.stream, "st", "value3");

		assertNotNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNotNull(sessionObject.getProperty("st"));

		try {
			sessionObject.clear(Scope.stream);
		} catch (JaxmppException e) {
			fail(e.getMessage());
		}

		assertNotNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNull(sessionObject.getProperty("st"));

	}

	@Test
	public void testReset3() {
		sessionObject.setProperty(Scope.user, "u", "value1");
		sessionObject.setProperty(Scope.session, "se", "value2");
		sessionObject.setProperty(Scope.stream, "st", "value3");

		assertNotNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNotNull(sessionObject.getProperty("st"));

		try {
			sessionObject.clear(Scope.stream, Scope.user);
		} catch (JaxmppException e) {
			fail(e.getMessage());
		}

		assertNull(sessionObject.getProperty("u"));
		assertNotNull(sessionObject.getProperty("se"));
		assertNull(sessionObject.getProperty("st"));
	}

	@Test
	public void testSetPropertyScopeStringObject() {
		sessionObject.setProperty(Scope.user, "default-key1", "value1");

		assertEquals("value1", sessionObject.getProperty("default-key1"));
		assertEquals("value1", sessionObject.getProperty(Scope.user, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.session, "default-key1"));
		assertNull(sessionObject.getProperty("default-key2"));
	}

	@Test
	public void testSetPropertyScopeStringObject_1() {
		sessionObject.setProperty(Scope.user, "default-key1", "value1");
		sessionObject.setProperty(Scope.session, "default-key1", "value1");
		sessionObject.setProperty(Scope.stream, "default-key1", "value1");

		assertEquals("value1", sessionObject.getProperty(Scope.stream, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.session, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.user, "default-key1"));
	}

	@Test
	public void testSetPropertyStringObject() {
		sessionObject.setProperty("default-key1", "value1");
		sessionObject.setProperty("default-key2", "value2");

		assertEquals("value1", sessionObject.getProperty("default-key1"));
		assertEquals("value1", sessionObject.getProperty(Scope.session, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.user, "default-key1"));
		assertNull(sessionObject.getProperty("default-key3"));

		assertEquals("value2", sessionObject.getProperty("default-key2"));

		sessionObject.setProperty("default-key1", null);
		assertNull(sessionObject.getProperty("default-key1"));
	}
}
