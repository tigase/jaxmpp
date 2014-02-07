/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
package tigase.jaxmpp.j2se.filetransfer;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;

public class FileTransfer extends tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer {

	private static final Logger log = Logger.getLogger(FileTransfer.class.getCanonicalName());
	private File file;
	private InputStream inputStream;
	private FileTransferNegotiator negotiator;

	protected FileTransfer(SessionObject sessionObject, JID peer, String sid) {
		super(sessionObject, peer, sid);
	}

	public File getFile() {
		return file;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	protected FileTransferNegotiator getNegotiator() {
		return negotiator;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	protected void setFileInfo(String filename, long fileSize, Date lastModified, String mimeType) {
		super.setFileInfo(filename, fileSize, lastModified, mimeType);
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	protected void setNegotiator(FileTransferNegotiator negotiator) {
		this.negotiator = negotiator;
	}

	@Override
	protected void transferredBytes(long count) {
		super.transferredBytes(count);
	}
}
