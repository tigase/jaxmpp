package tigase.jaxmpp.core.client;

import junit.framework.TestCase;

public class BareJIDTest extends TestCase {

	public void testBareJIDInstance() {
		assertEquals(BareJID.bareJIDInstance("a@b"), BareJID.bareJIDInstance("a@b"));
		assertEquals(BareJID.bareJIDInstance("a@b"), BareJID.bareJIDInstance("a", "b"));
		assertEquals(BareJID.bareJIDInstance("a", "b"), BareJID.bareJIDInstance("a", "b"));
		assertEquals(BareJID.bareJIDInstance("a@b"), BareJID.bareJIDInstance("a@b/c"));
	}

	public void testGetDomain() {
		BareJID jid = BareJID.bareJIDInstance("a@b");
		assertEquals("b", jid.getDomain());
	}

	public void testGetLocalpart() {
		BareJID jid = BareJID.bareJIDInstance("a@b");
		assertEquals("a", jid.getLocalpart());
	}

	public void testToString() {
		BareJID jid = BareJID.bareJIDInstance("a@b");
		assertEquals("a@b", jid.toString());

		jid = BareJID.bareJIDInstance("a@b/c");
		assertEquals("a@b", jid.toString());
	}

}
