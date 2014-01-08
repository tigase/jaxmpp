package tigase.jaxmpp.gwt.client;

import java.util.Set;

import org.junit.Test;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtSessionObjectTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "tigase.jaxmpp.gwt.JaxmppGWTJUnit";
	}

	@Test
	public void testReset1() {
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

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
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

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
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

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
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

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
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

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
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

		sessionObject.setProperty(Scope.user, "default-key1", "value1");

		assertEquals("value1", sessionObject.getProperty("default-key1"));
		assertEquals("value1", sessionObject.getProperty(Scope.user, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.session, "default-key1"));
		assertNull(sessionObject.getProperty("default-key2"));
	}

	@Test
	public void testSetPropertyScopeStringObject_1() {
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

		sessionObject.setProperty(Scope.user, "default-key1", "value1");
		sessionObject.setProperty(Scope.session, "default-key1", "value1");
		sessionObject.setProperty(Scope.stream, "default-key1", "value1");

		assertEquals("value1", sessionObject.getProperty(Scope.stream, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.session, "default-key1"));
		assertNull(sessionObject.getProperty(Scope.user, "default-key1"));
	}

	@Test
	public void testSetPropertyStringObject() {
		AbstractSessionObject sessionObject = new GwtSessionObject();
		sessionObject.setEventBus(new DefaultEventBus());

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
