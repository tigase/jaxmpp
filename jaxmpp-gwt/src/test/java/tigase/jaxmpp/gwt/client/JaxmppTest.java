package tigase.jaxmpp.gwt.client;

import junit.framework.Assert;
import com.google.gwt.junit.client.GWTTestCase;

import org.junit.Test;

import tigase.jaxmpp.gwt.client.Jaxmpp;

public class JaxmppTest extends GWTTestCase {

	public void test() {
		try {
			Jaxmpp j = new Jaxmpp();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}

	}

	@Override
	public String getModuleName() {
		return "tigase.jaxmpp.gwt.JaxmppGWTJUnit";
	}

}
