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

import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferRequestEvent;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferEvent;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;

/**
 *
 * @author andrzej
 */
public abstract class FileTransferNegotiatorAbstract implements FileTransferNegotiator {

        private final Logger log;
        protected FileTransferManager ftManager = null;
        protected Observable observable = null;

        public FileTransferNegotiatorAbstract() {
                log = Logger.getLogger(this.getClass().getCanonicalName());
        }

        @Override
        public void setObservable(Observable observableParent) {
                observable = ObservableFactory.instance(observableParent);
        }

        protected void fireOnRequest(FileTransferRequestEvent event) {
                try {
                        observable.fireEvent(NEGOTIATION_REQUEST, new FileTransferRequestEvent(NEGOTIATION_REQUEST, event.getSessionObject(), event.getFileTransfer(), event.getId(), event.getStreamMethods()));
                } catch (JaxmppException ex1) {
                        log.log(Level.SEVERE, "Exception sending event", ex1);
                }

        }

        protected void fireOnFailure(FileTransfer ft, Throwable ex) {
                try {
                        log.log(Level.WARNING, "firing file transfer negotiation error", ex);
                        observable.fireEvent(NEGOTIATION_FAILURE, new FileTransferEvent(NEGOTIATION_FAILURE, ft.getSessionObject(), ft));
                } catch (JaxmppException ex1) {
                        log.log(Level.SEVERE, "Exception sending event", ex1);
                }
        }

        protected void fireOnReject(FileTransfer ft) {
                try {
                        log.log(Level.WARNING, "firing file transfer rejected {0}", ft.toString());
                        observable.fireEvent(NEGOTIATION_REJECTED, new FileTransferEvent(NEGOTIATION_REJECTED, ft.getSessionObject(), ft));
                } catch (JaxmppException ex1) {
                        log.log(Level.SEVERE, "Exception sending event", ex1);
                }
        }
        
}
