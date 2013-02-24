package tigase.jaxmpp.j2se;

import junit.framework.Assert;

import org.junit.Test;

public class JaxmppTest {

	@Test
	public void test() {
		try {
			Jaxmpp j = new Jaxmpp();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}

	}

}
