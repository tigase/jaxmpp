/*
 * Tigase XMPP Client Library
 * Copyright (C) 2013 "Andrzej Wójcik" <andrzej.wojcik@tigase.org>
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
package tigase.jaxmpp.core.client.xmpp.modules.filetransfer;

import java.io.File;
import java.util.Date;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.DataHolder;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;

public class FileTransfer extends ConnectionSession {

        private static final Logger log = Logger.getLogger(FileTransfer.class.getCanonicalName());
                
        private String filename;
        private long fileSize;
        private String fileMimeType;
        private Date lastModified;
        private File file;
        
        private long transferredSize = 0;
        
        protected FileTransfer(SessionObject sessionObject, JID peer, String sid) {
                super(sessionObject, peer, sid, true);
        }
        
        protected void setFileInfo(String filename, long fileSize, Date lastModified, String mimeType) {
                this.filename = filename;
                this.fileSize = fileSize;
                this.fileMimeType = mimeType;
                this.lastModified = lastModified;
        }
        
        public void setFile(File file) {
                this.file = file;
        }
                
        public String getFilename() {
                return filename;
        }
        
        public long getFileSize() {
                return fileSize;
        }
        
        public String getFileMimeType() {
                return fileMimeType;
        }
        
        public Date getFileModification() {
                return lastModified;
        }
                
        public long getTransferredSize() {
                return transferredSize;
        }
        
        public File getFile() {
                return file;
        }
        
        protected void addTransferredSize(long size) {
                transferredSize += size;
        }
        
        public double getProgress() {
                if (transferredSize <= 0 || fileSize <= 0)
                        return 0;
                
                return ((double) transferredSize) / ((double) fileSize);
        }
}
