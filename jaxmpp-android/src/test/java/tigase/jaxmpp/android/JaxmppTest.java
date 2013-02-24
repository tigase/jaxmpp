package tigase.jaxmpp.android;

import junit.framework.Assert;

import org.junit.Test;

import tigase.jaxmpp.android.Jaxmpp;

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
