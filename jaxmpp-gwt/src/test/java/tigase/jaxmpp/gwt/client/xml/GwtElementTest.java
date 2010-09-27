package tigase.jaxmpp.gwt.client.xml;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtElementTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "tigase.jaxmpp.gwt.JaxmppGWTJUnit";
	}

	public void test01() throws Exception {
		assertEquals("FOO", "FOO");
	}

	 public void test02() throws Exception {
	 GwtElement x = GwtElement.parse("<iq to='x@y.z'><query/></iq>");
	 assertEquals("iq", x.getName());
	 }

}
