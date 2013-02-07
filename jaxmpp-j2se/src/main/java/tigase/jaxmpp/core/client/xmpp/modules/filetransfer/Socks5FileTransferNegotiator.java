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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.DiscoInfoAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.Identity;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule.DiscoItemsAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule.Item;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 *
 * @author andrzej
 */
public class Socks5FileTransferNegotiator extends FileTransferNegotiatorAbstract {

        private static final Logger log = Logger.getLogger(Socks5FileTransferNegotiator.class.getCanonicalName());
        
        private final String BASE = "session-";
        private final String STREAM_METHOD = BASE + "stream-method";
        private final String PACKET_ID = BASE + "initiation-packet-id";

        private final Socks5BytestreamsConnectionManager connectionManager = new Socks5BytestreamsConnectionManager();
        
        private final Listener<ConnectionEvent> connectionEstablishedListener = new Listener<ConnectionEvent>() {

                @Override
                public void handleEvent(ConnectionEvent be) throws JaxmppException {
                        FileTransfer ft = (FileTransfer) be.getConnectionSession();
                        if (log.isLoggable(Level.FINEST)) {
                                log.log(Level.FINEST, "got ft incoming = {0} with packet id = {1}", new Object[]{ft.isIncoming(), ft.getData(Socks5BytestreamsConnectionManager.PACKET_ID)});
                        }
                        // if it is incoming file transfer we need to notify peer about used streamhost
                        if (ft.isIncoming() && ft.getData(Socks5BytestreamsConnectionManager.PACKET_ID) != null) {
                                connectionManager.sendStreamhostUsed(ft, (String) ft.getData(Socks5BytestreamsConnectionManager.PACKET_ID));
                        }
                }
                
        };
        
        @Override
        public void setObservable(Observable observableParent) {
                super.setObservable(observableParent);
                connectionManager.setObservable(observable);
                observable.addListener(ConnectionManager.CONNECTION_ESTABLISHED, connectionEstablishedListener);                
        }
        
        @Override
        public void sendFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException {
                // check if receiver contains support
                Presence p = ft.getSessionObject().getPresence().getPresence(ft.getPeer());
                CapabilitiesModule capsModule = jaxmpp.getModule(CapabilitiesModule.class);
                String capsNode = FileTransferManager.getCapsNode(p);
                Set<String> features = capsModule.getCache().getFeatures(capsNode);                
                if (true || (features != null && features.contains(Socks5BytestreamsModule.XMLNS_BS) && features.contains(FileTransferModule.XMLNS_SI_FILE))) {
                        FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
                        if (ftModule != null) {
                                connectionManager.initConnection(jaxmpp, ft, new ConnectionManager.InitializedCallback() {

                                        @Override
                                        public void initialized(JaxmppCore jaxmpp, ConnectionSession session) {
                                                try {
                                                        sendFile2(jaxmpp, (FileTransfer) session);
                                                }
                                                catch(JaxmppException ex) {
                                                        fireOnFailure((FileTransfer) session, ex);
                                                }
                                        }
                                        
                                });
                                //ftModule.sendStreamInitiationOffer(ft, new String[] { FileTransferModule.XMLNS_BS });
                                return;
                        }
                }

                fireOnFailure(ft, null);
        }

        public void sendFile2(final JaxmppCore jaxmpp, final FileTransfer ft) throws JaxmppException {
                FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
                if (ftModule != null) {
                        ftModule.sendStreamInitiationOffer(ft, new String[]{Socks5BytestreamsModule.XMLNS_BS}, new StreamInitiationOfferAsyncCallback() {
                                @Override
                                public void onAccept(String sid) {      
                                        try {                                                
                                                connectionManager.connectTcp(jaxmpp, ft);
                                        } catch (JaxmppException ex) {
                                                fireOnFailure(ft, ex);
                                        }
                                }

                                @Override
                                public void onError() {
                                        fireOnFailure(ft, null);
                                }

                                @Override
                                public void onReject() {
                                        fireOnReject(ft);
                                }
                        });
                }
        }

        @Override
        public void acceptFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException {
                FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
                if (ftModule != null) {
                        String packetId = ft.getData(PACKET_ID);
                        String streamMethod = ft.getData(STREAM_METHOD);
                        if (packetId == null) {
                                fireOnFailure(ft, null);
                        } else if (streamMethod == null) {
                                ftModule.rejectStreamInitiation(ft, packetId);
                        } else {
                                connectionManager.register(jaxmpp, ft);
                                ftModule.acceptStreamInitiation(ft, packetId, streamMethod);
                        }
                }
        }

        @Override
        public void rejectFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException {
                FileTransferModule ftModule = jaxmpp.getModule(FileTransferModule.class);
                if (ftModule != null) {
                        String packetId = ft.getData(PACKET_ID);
                        if (packetId == null) {
                                fireOnFailure(ft, null);
                        } else {
                                ftModule.rejectStreamInitiation(ft, packetId);
                        }
                }
        }

        @Override
        public void registerListeners(JaxmppCore jaxmpp) {
                jaxmpp.getModule(FileTransferModule.class).addListener(FileTransferModule.RequestEventType, new Listener<FileTransferRequestEvent>() {

                        @Override
                        public void handleEvent(FileTransferRequestEvent be) throws JaxmppException {
                                FileTransfer ft = be.getFileTransfer();
                                ft.setData(PACKET_ID, be.getId());
                                ft.setData(STREAM_METHOD, be.getStreamMethods().get(0));
                                fireOnRequest(be);
                        }
                        
                });                
        }

        @Override
        public void unregisterListeners(JaxmppCore jaxmpp) {
                throw new UnsupportedOperationException("Not supported yet.");
        }
        
}
