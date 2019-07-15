package tigase.jaxmpp.core.client;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class HexTest {

	@Test
	public void decode() {
		Assert.assertArrayEquals(new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57},
								 Hex.decode("30313233343536373839"));
	}

	@Test
	public void encode() {
		Assert.assertEquals("30313233343536373839", Hex.encode(new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57}));
	}

	@Test
	public void testFormat(){
		Assert.assertEquals("1", Hex.format("1",4));
		Assert.assertEquals("12", Hex.format("12",4));
		Assert.assertEquals("123", Hex.format("123",4));
		Assert.assertEquals("1234", Hex.format("1234",4));
		Assert.assertEquals("1234 5", Hex.format("12345",4));
		Assert.assertEquals("1234 56", Hex.format("123456",4));
		Assert.assertEquals("1234 567", Hex.format("1234567",4));
		Assert.assertEquals("1234 5678", Hex.format("12345678",4));
		Assert.assertEquals("1234 5678 9", Hex.format("123456789",4));
		Assert.assertEquals("1234 5678 90", Hex.format("1234567890",4));

		Assert.assertEquals("123 456 7", Hex.format("1234567",3));
		Assert.assertEquals("123 456 78", Hex.format("12345678",3));
	}

	@Test
	public void encodeAndDecodeString() throws UnsupportedEncodingException {
		String t = "Zbłaźń mżystość ględów hiperfunkcją.";
		String h = Hex.encode(t.getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals(
				"5a62c58261c5bac584206dc5bc7973746fc59bc48720676cc49964c3b37720686970657266756e6b636ac4852e", h);
		String t1 = new String(Hex.decode(h));
		Assert.assertEquals(t, t1);
	}
}