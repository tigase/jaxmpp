/*
 * WebSocketReaderTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmp.j2se.connectors.websocket;

import org.junit.Test;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.connectors.socket.StreamListener;
import tigase.jaxmpp.j2se.connectors.socket.XMPPDomBuilderHandler;
import tigase.jaxmpp.j2se.connectors.websocket.WebSocketReader;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author andrzej
 */
public class WebSocketReaderTest {

	private static final Logger log = Logger.getLogger(WebSocketReaderTest.class.getCanonicalName());

	private final SimpleParser parser = SingletonFactory.getParserInstance();

	private byte[] convertToFrame(Element data) throws XMLException, UnsupportedEncodingException {
		byte[] dataByteStr = data.getAsString().getBytes("UTF-8");
		int size = dataByteStr.length;
		ByteBuffer bbuf = ByteBuffer.allocate(12 + size);
		bbuf.put((byte) 0x00);
		if (size <= 125) {
			bbuf.put((byte) size);
		} else if (size <= 0xFFFF) {
			bbuf.put((byte) 0x7E);
			bbuf.putShort((short) size);
		} else {
			bbuf.put((byte) 0x7F);
			bbuf.putLong((long) size);
		}
		bbuf.put(dataByteStr);
		bbuf.flip();
		byte[] out = new byte[bbuf.remaining()];
		bbuf.get(out);
		return out;
	}

	private Element generateData(int count) throws XMLException {
		Element el = ElementFactory.create("test");
		for (int i = 0; i < count; i++) {
			el.addChild(ElementFactory.create("test-" + i, "val-" + i, "http://test.org/xmlns"));
		}
		return el;
	}

	private void initLoggers() {
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);

		String[] list = {WebSocketReader.class.getCanonicalName(),
						 "tigase.jaxmp.j2se.connectors.websocket.WebSocketReaderTest"};

		// for (String x : list) {
		// Logger logger = Logger.getLogger(x);
		// logger.addHandler(handler);
		// logger.setLevel(Level.FINEST);
		// }
	}

	private void testReadingData(int noOfSubelements, int sizeOfBuffer) throws XMLException, IOException {
		Element data = generateData(noOfSubelements);
		byte[] inData = convertToFrame(data);
		log.log(Level.FINEST, "got websocket data {0} bytes", inData.length);

		WebSocketReader reader = new WebSocketReader(new ByteArrayInputStream(inData));

		Queue<Element> outQueue = new ArrayDeque<Element>();
		XMPPDomBuilderHandler domHandler = new XMPPDomBuilderHandler(new StreamListenerImpl(outQueue));
		char[] cbuf = new char[sizeOfBuffer];
		int r;
		int maxIter = (inData.length / cbuf.length) * 2;
		int iter = 0;
		while (((r = reader.read(cbuf)) != -1) && iter < maxIter) {
			iter++;
			parser.parse(domHandler, cbuf, 0, r);
		}
		log.log(Level.FINEST, "decoded in {0} iterations", iter);

		assertTrue(!outQueue.isEmpty());
		Element outData = outQueue.poll();
		assertEquals(data.getAsString(), outData.getAsString());
	}

	@Test
	public void testReadingDataBiggerThanBuffer() throws XMLException, IOException {
		initLoggers();
		testReadingData(10, 10);
		testReadingData(100, 100);
		testReadingData(100, 10);
		testReadingData(1000, 100);
		testReadingData(10000, 100);
	}

	static class StreamListenerImpl
			implements StreamListener {

		private Queue<Element> queue;

		public StreamListenerImpl(Queue<Element> queue) {
			this.queue = queue;
		}

		@Override
		public void nextElement(tigase.xml.Element element) {
			queue.offer(new J2seElement(element));
		}

		@Override
		public void xmppStreamClosed() {
			if (log.isLoggable(Level.FINEST)) {
				log.finest("xmppStreamClosed()");
			}
		}

		@Override
		public void xmppStreamOpened(Map<String, String> attribs) {
			if (log.isLoggable(Level.FINEST)) {
				log.finest("xmppStreamOpened()");
			}
		}
	}
}
