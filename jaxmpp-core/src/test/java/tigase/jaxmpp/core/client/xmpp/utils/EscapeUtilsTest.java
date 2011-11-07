package tigase.jaxmpp.core.client.xmpp.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EscapeUtilsTest {

	@Test
	public void testEscape() {
		assertEquals("&lt;a&gt;&quot;&amp;&lt;a&gt;", EscapeUtils.escape("<a>\"&<a>"));
		assertEquals("&lt;a&gt;", EscapeUtils.escape("<a>"));
		assertEquals("&lt;a b=&quot;x'x&quot;&gt;", EscapeUtils.escape("<a b=\"x'x\">"));
	}

	@Test
	public void testUnescape() {
		assertEquals("<a>", EscapeUtils.unescape("&lt;a&gt;"));
		assertEquals("<a b=\"x'x\">", EscapeUtils.unescape("&lt;a b=&quot;x'x&quot;&gt;"));
		assertEquals("<a>\"&<a>", EscapeUtils.unescape("&lt;a&gt;&quot;&amp;&lt;a&gt;"));
	}

}
