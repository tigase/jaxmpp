/*
 * WebSocketReader.java
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
package tigase.jaxmpp.j2se.connectors.websocket;

import tigase.jaxmpp.j2se.connectors.socket.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DEFAULT_SOCKET_BUFFER_SIZE;

/**
 * @author andrzej
 */
public class WebSocketReader
		implements Reader {

	private static final Logger log = Logger.getLogger(WebSocketReader.class.getCanonicalName());

	public enum FrameType {
		Text,
		Ping,
		Pong
	}

	private final ByteBuffer buf = ByteBuffer.allocate(DEFAULT_SOCKET_BUFFER_SIZE);
	private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	private final InputStream inputStream;
	private FrameType frameType = FrameType.Pong;
	private long remaining = 0;

	public WebSocketReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		byte[] arr = buf.array();
		boolean closed = false;
		int startBufPos = buf.position();
		int read = inputStream.read(arr, startBufPos, buf.remaining());
		if (read == 0) {
			return 0;
		}
		if (read == -1) {
			closed = true;
			if (!buf.hasRemaining()) {
				return -1;
			} else {
				read = 0;
			}
		}
		buf.position(startBufPos + read);
		buf.flip();

		CharBuffer cb = CharBuffer.wrap(cbuf);
		//while (buf.hasRemaining()) {
		while (buf.hasRemaining() && cb.hasRemaining()) {
			if (remaining == 0) {
				boolean reset = false;
				int position = buf.position();
				int type = buf.get();
				byte frameType = (byte) (type & 0x0F);
				switch (frameType) {
					case 0x08:
						// EOL (we should inform client that connection is closed)
						if (buf.hasRemaining()) {
							byte len = buf.get();
							if (buf.remaining() < len) {
								if (!closed) {
									buf.position(position);
									break;
								}
							} else {
								if (len > 0) {
									log.log(Level.FINE, "received WebSocket close with status = " + buf.getShort());
								}
							}

						}
						return -1;
					case 0x0A:
						this.frameType = FrameType.Pong;
						log.log(Level.FINEST, "got PONG frame");
						break;
					default:
						this.frameType = FrameType.Text;
						break;
				}
				if (buf.hasRemaining()) {
					long len = buf.get() & 0x7f;
					if (len == 126) {
						if (buf.remaining() >= 2) {
							remaining = buf.getShort() & 0xffff;
						} else {
							reset = true;
						}
					} else if (len == 127) {
						if (buf.remaining() >= 8) {
							remaining = buf.getLong();
						} else {
							reset = true;
						}
					} else {
						remaining = len;
					}
				} else {
					reset = true;
				}
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
				int rem = buf.remaining();
				buf.limit((int) (buf.position() + waiting));
				switch (this.frameType) {
					case Text:
						decoder.decode(buf, cb, false);
						break;
					case Pong:
						buf.position((int) (buf.position() + remaining));
					default:
						break;
				}
				buf.limit(limit);
				remaining -= (rem - buf.remaining());
				if (remaining < 0) {
					remaining = 0;
				}
			}
		}
		buf.compact();
		cb.flip();
		if (log.isLoggable(Level.FINEST) && cb.hasRemaining()) {
			char[] tmp = new char[cb.remaining()];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = cb.get(cb.position() + i);
			}
			log.log(Level.FINEST,
					"read data = " + new String(tmp) + ", still remaining = " + remaining + " and in buffer " +
							buf.remaining());
		}

		return cb.remaining();
	}

}
