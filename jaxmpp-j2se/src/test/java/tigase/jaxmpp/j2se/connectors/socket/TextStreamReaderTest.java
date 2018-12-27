/*
 * TextStreamReaderTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

package tigase.jaxmpp.j2se.connectors.socket;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by andrzej on 06.03.2016.
 */
public class TextStreamReaderTest {

	@Test
	public void test() throws IOException {
		String x = "a";

		for (int i = 0; i < 1999; i++) {
			x += "\u2014";
		}
		byte[] data = x.getBytes("UTF-8");
		InputStream inputStream = new ByteArrayInputStream(data);
		TextStreamReader reader = new TextStreamReader(inputStream);
		char[] cb = new char[5];
		int read;
		StringBuilder received = new StringBuilder();
		while ((read = reader.read(cb)) > 0) {
			received.append(cb, 0, read);
		}

		Assert.assertEquals(x, received.toString());
	}
}
