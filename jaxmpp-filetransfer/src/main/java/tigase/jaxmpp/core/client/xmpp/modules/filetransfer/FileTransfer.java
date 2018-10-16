/*
 * FileTransfer.java
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
package tigase.jaxmpp.core.client.xmpp.modules.filetransfer;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;

import java.util.Date;

public class FileTransfer
		extends ConnectionSession {

	private String fileMimeType;
	private long fileSize;
	private String filename;
	private Date lastModified;

	private long transferredBytes = 0;

	protected FileTransfer(SessionObject sessionObject, JID peer, String sid) {
		super(sessionObject, peer, sid, true);
	}

	public String getFileMimeType() {
		return fileMimeType;
	}

	public void setFileMimeType(String mimeType) {
		this.fileMimeType = mimeType;
	}

	public Date getFileModification() {
		return lastModified;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getFilename() {
		return filename;
	}

	public Double getProgress() {
		if (getFileSize() == 0) {
			return null;
		}
		return ((double) transferredBytes * 100) / getFileSize();
	}

	public long getTransferredBytes() {
		return transferredBytes;
	}

	protected void setFileInfo(String filename, long fileSize, Date lastModified, String mimeType) {
		this.filename = filename;
		this.fileSize = fileSize;
		this.fileMimeType = mimeType;
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return "sid = " + getSid() + ", jid = " + getPeer().toString() + ", file = " + getFilename();
	}

	protected void transferredBytes(long count) {
		transferredBytes += count;
	}

}
