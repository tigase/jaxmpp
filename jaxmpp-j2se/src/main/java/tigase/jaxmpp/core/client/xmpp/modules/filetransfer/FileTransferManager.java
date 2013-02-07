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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.MultiJaxmpp;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

/**
 *
 * @author andrzej
 */
public class FileTransferManager implements ObservableAware {

        private static final Logger log = Logger.getLogger(FileTransferManager.class.getCanonicalName());
        
        private final Map<BareJID,JaxmppCore> multiJaxmpp = new HashMap<BareJID,JaxmppCore>();
        
        private final List<FileTransferNegotiator> negotiators = new ArrayList<FileTransferNegotiator>();

        protected Observable observable = null;

        protected Listener<ConnectionEvent> connectionEventListener = new Listener<ConnectionEvent>() {

                @Override
                public void handleEvent(ConnectionEvent be) throws JaxmppException {
                        if (be.getType() == ConnectionManager.CONNECTION_ESTABLISHED) {
                                if (be.getSocket() == null) {
                                        // this should not happen
                                        throw new JaxmppException("SOCKET IS NULL");
                                }
                                connectionEstablished((FileTransfer) be.getConnectionSession(), be.getSocket());
                        }
                }

        };
        
        @Override
        public void setObservable(Observable observableParent) {
                observable = ObservableFactory.instance(observableParent);
                observable.addListener(ConnectionManager.CONNECTION_ESTABLISHED, connectionEventListener);
                observable.addListener(ConnectionManager.CONNECTION_CLOSED, connectionEventListener);
                observable.addListener(ConnectionManager.CONNECTION_FAILED, connectionEventListener);
        }
        
        public void addListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
                observable.addListener(eventType, listener);
        }
        
        public void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
                observable.removeListener(eventType, listener);
        }
        
        public void registerJaxmpp(JaxmppCore jaxmpp) {
                multiJaxmpp.put(jaxmpp.getSessionObject().getUserBareJid(), jaxmpp);
        }
        
        public void unregisterJaxmpp(JaxmppCore jaxmpp) {
                multiJaxmpp.remove(jaxmpp.getSessionObject().getUserBareJid());
        }
        
        protected JaxmppCore getJaxmpp(SessionObject sessionObject) {
                return multiJaxmpp.get(sessionObject.getUserBareJid());
        }
        
        public void addNegotiator(FileTransferNegotiator negotiator) {
                negotiator.setObservable(observable);
                for(JaxmppCore jaxmpp : multiJaxmpp.values()) {
                        negotiator.registerListeners(jaxmpp);
                }
                negotiators.add(negotiator);                
        }
        
        public void removeNegotiator(FileTransferNegotiator negotiator) {
                negotiators.remove(negotiator);
                for(JaxmppCore jaxmpp : multiJaxmpp.values()) {
                        negotiator.unregisterListeners(jaxmpp);
                }
                negotiator.setObservable(observable);
        }

        public FileTransfer sendFile(SessionObject sessionObject, JID peer, File file) throws JaxmppException {
                FileTransfer ft = new FileTransfer(sessionObject, peer, generateSid());
                ft.setIncoming(false);
                ft.setFileInfo(file.getName(), file.length(), new Date(file.lastModified()), null);
                ft.setFile(file);
        
                JaxmppCore jaxmpp = getJaxmpp(sessionObject);

                negotiators.get(0).sendFile(jaxmpp, ft);
                
                return ft;
        }
        
        public void acceptFile(FileTransfer ft) throws JaxmppException {
                ft.setIncoming(true);
                
                JaxmppCore jaxmpp = getJaxmpp(ft.getSessionObject());
                negotiators.get(0).acceptFile(jaxmpp, ft);
        }

        public void rejectFile(FileTransfer ft) throws JaxmppException {
                ft.setIncoming(true);
                
                JaxmppCore jaxmpp = getJaxmpp(ft.getSessionObject());
                negotiators.get(0).rejectFile(jaxmpp, ft);
        }

        protected static String getCapsNode(Presence presence) throws XMLException {
                if (presence == null) return null;
		Element c = presence.getChildrenNS("c", "http://jabber.org/protocol/caps");
		if (c == null)
			return null;
                return c.getValue();
        }
        
        private String generateSid() {
                return UUID.randomUUID().toString();
        }
        
        private void connectionEstablished(FileTransfer fileTransfer, Socket socket) {
                if (fileTransfer.isIncoming()) {
                        startReceiving(fileTransfer, socket);
                }
                else {
                        startSending(fileTransfer, socket);
                }
        }

        private void startSending(final FileTransfer fileTransfer, final Socket socket) {
                new Thread() {
                        @Override
                        public void run() {
                                try {
                                        File f = fileTransfer.getFile();
                                        FileInputStream fis = new FileInputStream(f);
                                        transferData(fis.getChannel(), socket.getChannel());
                                        fis.close();

                                        socket.close();
                                }
                                catch (IOException ex) {
                                        log.log(Level.SEVERE, "exception transfering data", ex);
                                }
                        }
                }.start();
        }

        private void startReceiving(final FileTransfer fileTransfer, final Socket socket) {
                new Thread() {
                        @Override
                        public void run() {
                                try {
                                        try {
                                                Thread.sleep(1000);
                                        } catch (Exception ex) {}
                                        File f = fileTransfer.getFile();
                                        FileOutputStream fos = new FileOutputStream(f);
                                        transferData(socket.getChannel(), fos.getChannel());
                                        fos.close();
                                        try {
                                                Thread.sleep(1000);
                                        } catch (Exception ex) {}
                                        socket.close();
                                }
                                catch (IOException ex) {
                                        log.log(Level.SEVERE, "exception transfering data", ex);
                                }
                        }
                }.start();
        }
                
        private void transferData(ByteChannel in, ByteChannel out) throws IOException {
                ByteBuffer buf = ByteBuffer.allocate(16 * 1024);
                int read;
                int transferred = 0;
                while((read = in.read(buf)) > -1) {
                        buf.flip();
                        transferred += out.write(buf);                                                
                        buf.clear();
                        
                        if (log.isLoggable(Level.FINEST)) {
                                log.log(Level.FINEST, "transferred bytes = {0}", transferred);
                        }
                }
        }
}
