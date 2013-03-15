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
package tigase.jaxmpp.j2se.connection.socks5bytestream;

import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer;
import tigase.jaxmpp.j2se.connection.ConnectionEvent;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule.ActivateCallback;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Streamhost;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamhostUsedCallback;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamhostsCallback;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.StreamhostsEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 *
 * @author andrzej
 */
public class Socks5BytestreamsConnectionManager extends Socks5ConnectionManager {

        private static final Logger log = Logger.getLogger(Socks5BytestreamsConnectionManager.class.getCanonicalName());
                
        @Override
        public void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback) throws JaxmppException {
                session.setData(JAXMPP_KEY, jaxmpp);
                discoverProxy(jaxmpp, session, callback);                
        }

        @Override
        public void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException {
                //fireOnSuccess(ft);
                session.setData(JAXMPP_KEY, jaxmpp);
                JID proxyJid = session.getData(PROXY_JID_KEY);
                if (proxyJid != null) {
                        requestStreamHosts(jaxmpp, session, proxyJid);
                } else {
                        sendStreamHosts(jaxmpp, session, null);
                }
        }

        @Override
        public void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) {
                throw new UnsupportedOperationException("Not supported yet.");
        }
                
        public void register(final JaxmppCore jaxmpp, final ConnectionSession session) {
                session.setData(JAXMPP_KEY, jaxmpp);
                final Socks5BytestreamsModule socks5Module = jaxmpp.getModule(Socks5BytestreamsModule.class);
                socks5Module.addListener(Socks5BytestreamsModule.StreamhostsEventType, new Listener<StreamhostsEvent>() {

                        @Override
                        public void handleEvent(StreamhostsEvent be) throws JaxmppException {
                                session.setData(PACKET_ID, be.getId());
                                if (session.getSid().equals(be.getSid())) {
                                        socks5Module.removeListener(this);
                                        for (Streamhost host : be.getHosts()) {
                                                try {
                                                        connectToProxy(jaxmpp, session, host);
                                                        break;
                                                } catch (IOException ex) {
                                                }
                                        }
                                }
                        }
                        
                });
        }
        
        protected void requestStreamHosts(final JaxmppCore jaxmpp, final ConnectionSession session, final JID proxyJid) throws JaxmppException {
                Socks5BytestreamsModule socks5Module = jaxmpp.getModule(Socks5BytestreamsModule.class);
                socks5Module.requestStreamhosts(proxyJid, new StreamhostsCallback(socks5Module) {

                        @Override
                        public void onStreamhosts(List<Streamhost> hosts) throws JaxmppException {
                                sendStreamHosts(jaxmpp, session, hosts);
                        }

                        @Override
                        public void onError(Stanza responseStanza, XMPPException.ErrorCondition error) throws JaxmppException {
                                sendStreamHosts(jaxmpp, session, null);
                        }

                        @Override
                        public void onTimeout() throws JaxmppException {
                                sendStreamHosts(jaxmpp, session, null);
                        }
                        
                });
        }
        
        protected void sendStreamHosts(final JaxmppCore jaxmpp, final ConnectionSession ft, List<Streamhost> proxyStreamhosts) throws JaxmppException {
                Socks5BytestreamsModule socks5Module = jaxmpp.getModule(Socks5BytestreamsModule.class);
                List<Streamhost> streamhosts = getLocalStreamHosts(ft);
                if (proxyStreamhosts != null) {
                        streamhosts.addAll(proxyStreamhosts);
                }
                
                StreamhostUsedCallback streamhostUsedCallback = new StreamhostUsedCallback() {

                        @Override
                        public boolean onSuccess(Streamhost host) {
                                System.out.println("streamhost-used = " + host.getJid());
                                if (host.getJid().equals(ft.getSessionObject().getBindedJid())) {
                                        System.out.println("streamhost-used = 'local'");
                                        synchronized(ft) {
                                                Socket socket = ft.getData("socket");
                                                if (socket != null) {
                                                        fireOnConnected(ft, socket);
                                                }
                                                else {
                                                        ft.setData("streamhost-received", true);
                                                }
                                        }
                                } else {
                                        try {
                                                connectToProxy(jaxmpp, ft, host);
                                        }
                                        catch (Exception ex) {
                                                log.log(Level.WARNING, "exception while connecting to proxy", ex);
                                                return false;
                                        }
                                }
                                
                                return true;
                        }

                        @Override
                        public void onError(Exception ex, String errorText) {
                                log.log(Level.SEVERE, errorText, ex);
                                //fireOnFailure(ft, ex);
                        }
                        
                };
                
                streamhostUsedCallback.setHosts(streamhosts);
                
                socks5Module.sendStreamhosts(ft.getPeer(), ft.getSid(), streamhosts, streamhostUsedCallback);
                
        }
        
        public void sendStreamhostUsed(FileTransfer ft, String packetId) throws JaxmppException {
                JaxmppCore jaxmpp = ft.getData(JAXMPP_KEY);
                Streamhost streamhost = ft.getData(STREAMHOST_KEY);
                jaxmpp.getModule(Socks5BytestreamsModule.class).sendStreamhostUsed(ft.getPeer(), packetId, ft.getSid(), streamhost);
        }
                
}
