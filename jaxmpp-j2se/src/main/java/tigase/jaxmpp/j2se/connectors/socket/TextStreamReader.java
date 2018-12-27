/*
 * TextStreamReader.java
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DEFAULT_SOCKET_BUFFER_SIZE;

/**
 * TextStreamReader class replaces standard InputStreamReader as it cannot read from
 * InflaterInputStream.
 *
 * @author andrzej
 */
public class TextStreamReader
		implements Reader {

	private final ByteBuffer buf = ByteBuffer.allocate(DEFAULT_SOCKET_BUFFER_SIZE);

	private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

	private final InputStream inputStream;

	public TextStreamReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		byte[] arr = buf.array();
		int read = inputStream.read(arr, buf.position(), buf.remaining());
		if (read >= 0) {
			buf.position(buf.position() + read);
		}
		buf.flip();

		CharBuffer cb = CharBuffer.wrap(cbuf);
		decoder.decode(buf, cb, false);
		buf.compact();
		cb.flip();

		return cb.hasRemaining() ? cb.remaining() : (read < 0 ? -1 : 0);
	}

	// Below are alternative read methods which can be used if above method
	// will be causing performance issues
	// public int read3(char[] cbuf) throws IOException {
	// byte[] arr = new byte[2048];
	// int read = inputStream.read(arr, 0, arr.length);
	//
	// CharBuffer cb = CharBuffer.wrap(cbuf);
	// decoder.decode(ByteBuffer.wrap(arr, 0, read), cb, false);
	// cb.flip();
	//
	// return cb.remaining();
	// }
	//
	// public int read2(char[] cbuf) throws IOException {
	// byte[] arr = new byte[2048];
	// int read = inputStream.read(arr, 0, arr.length);
	//
	// CharBuffer cb = CharBuffer.allocate(2048);
	// decoder.decode(ByteBuffer.wrap(arr, 0, read), cb, false);
	// cb.flip();
	//
	// int got = cb.remaining();
	// cb.get(cbuf, 0, got);
	//
	// return got;
	// }	
}
