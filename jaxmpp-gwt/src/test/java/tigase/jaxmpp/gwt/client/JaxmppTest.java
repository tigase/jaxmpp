package tigase.jaxmpp.gwt.client;

import junit.framework.Assert;

import com.google.gwt.junit.client.GWTTestCase;

public class JaxmppTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "tigase.jaxmpp.gwt.JaxmppGWTJUnit";
	}

	public void test() {
		try {
			Jaxmpp j = new Jaxmpp();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}

	}

}
