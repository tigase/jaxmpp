package tigase.jaxmpp.j2se.connectors.socket;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by andrzej on 06.03.2016.
 */
public class TextStreamReaderTest {

	@Test
	public void test() throws IOException {
		String x = "a";

		for (int i=0; i<1999; i++) {
			x += "\u2014";
		}
		byte[] data = x.getBytes("UTF-8");
		InputStream inputStream = new ByteArrayInputStream(data);
		TextStreamReader reader = new TextStreamReader(inputStream);
		char[] cb = new char[5];
		int read;
		StringBuilder received = new StringBuilder();
		while((read = reader.read(cb)) > 0) {
			received.append(cb, 0, read);
		}

		Assert.assertEquals(x, received.toString());
	}
}
