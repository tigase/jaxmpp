package tigase.jaxmpp.j2se;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public class J2SESessionObjectTest {

	protected AbstractSessionObject sessionObject;

	@Before
	public void setUp() throws Exception {
		sessionObject = new J2SESessionObject();
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
