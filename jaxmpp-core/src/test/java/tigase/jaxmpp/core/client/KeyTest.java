package tigase.jaxmpp.core.client;

import junit.framework.TestCase;
import tigase.jaxmpp.core.client.ResponseManager.Key;

public class KeyTest extends TestCase {

	private Key a_1;
	private Key a_2;
	private Key d_1;

	@Override
	protected void setUp() throws Exception {
		this.a_1 = new Key("1", BareJID.bareJIDInstance("a@b"));
		this.a_2 = new Key("1", BareJID.bareJIDInstance("a@b"));

		this.d_1 = new Key("1", null);
	}

	public void test01() {
		assertEquals(a_1, a_2);
	}

}
