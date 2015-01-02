/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc. <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.j2se.connectors.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.j2se.connectors.socket.Reader;
import static tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DEFAULT_SOCKET_BUFFER_SIZE;

/**
 *
 * @author andrzej
 */
public class WebSocketReader implements Reader {

	private static final Logger log = Logger.getLogger(WebSocketReader.class.getCanonicalName());
	
	private final ByteBuffer buf = ByteBuffer.allocate(DEFAULT_SOCKET_BUFFER_SIZE);

	private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

	private final InputStream inputStream;
	
	private long remaining = 0;

	public WebSocketReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public int read(char[] cbuf) throws IOException {
		byte[] arr = buf.array();
		int read = inputStream.read(arr, buf.position(), buf.remaining());
		if (read == -1)
			return -1;		
		buf.position(read);
		buf.flip();
		
		CharBuffer cb = CharBuffer.wrap(cbuf);
		while (buf.hasRemaining()) {
			if (remaining == 0) {
				boolean reset = false;
				int position = buf.position();
				int type = buf.get();
				if (type == 0x08) {
					// EOL (we should inform client that connection is closed)
					break;
				}
				if (buf.hasRemaining()) {
					long len = buf.get() & 0x7f;
					if (len == 126) {
						if (buf.remaining() >= 2)
							remaining = buf.getShort();
						else 
							reset = true;
					} else if (len == 127) {
						if (buf.remaining() >= 8)
							remaining = buf.getLong();
						else
							reset = true;
					} else {
						remaining = len;
					}
				} else 
					reset = true;
				if (reset) {
					buf.position(position);
					break;
				}
				log.log(Level.FINEST, "got frame of type = " + type + " with length = " + remaining);
			}
			if (remaining > 0) {
				long waiting = (remaining <= buf.remaining()) ? remaining : buf.remaining();
				// decode what is waiting
				int limit = buf.limit();
				buf.limit((int) (buf.position() + waiting));
				decoder.decode(buf, cb, false);
				buf.limit(limit);
				remaining -= waiting;
				if (remaining < 0) 
					remaining = 0;
			}
		}
		buf.compact();
		cb.flip();	
		if (log.isLoggable(Level.FINEST) && cb.hasRemaining()) {
			char[] tmp = new char[cb.remaining()];
			for (int i=0; i<tmp.length; i++) {
				tmp[i] = cb.get(cb.position() + i);
			}
			log.log(Level.FINEST, "read data = " + new String(tmp) + ", still remaining = " + remaining + " and in buffer " + buf.remaining());
		}
		
		return cb.remaining();
	}
	
}
