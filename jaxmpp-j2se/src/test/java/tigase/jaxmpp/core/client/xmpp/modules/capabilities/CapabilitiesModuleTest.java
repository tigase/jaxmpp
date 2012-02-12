package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CapabilitiesModuleTest {

	@Test
	public void testGenerateVerificationString() {
		assertEquals(
				"QgayPKawpkPSDYmwT/WM94uAlu0=",
				CapabilitiesModule.generateVerificationString(new String[] { "client/pc//Exodus 0.9.1" }, new String[] {
						"http://jabber.org/protocol/caps", "http://jabber.org/protocol/disco#info",
						"http://jabber.org/protocol/disco#items", "http://jabber.org/protocol/muc" }));
	}

}
