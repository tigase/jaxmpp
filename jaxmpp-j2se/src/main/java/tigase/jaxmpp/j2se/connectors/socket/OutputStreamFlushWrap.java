/*
 * OutputStreamFlushWrap.java
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
package tigase.jaxmpp.j2se.connectors.socket;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStreamFlushWrap class is wrapper class used to wrap
 * DeflaterOutputStream to force flushing every time data is written to output
 * stream
 *
 * @author andrzej
 */
public class OutputStreamFlushWrap
		extends OutputStream {

	private final OutputStream outputStream;

	public OutputStreamFlushWrap(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void close() throws IOException {
		outputStream.close();
	}

	@Override
	public void flush() throws IOException {
		outputStream.flush();
	}

	@Override
	public void write(byte[] b) throws IOException {
		outputStream.write(b);
		outputStream.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputStream.write(b, off, len);
		outputStream.flush();
	}

	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
		outputStream.flush();
	}

}
