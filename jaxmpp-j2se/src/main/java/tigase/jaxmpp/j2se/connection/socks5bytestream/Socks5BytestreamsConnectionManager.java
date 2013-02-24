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
public class Socks5BytestreamsConnectionManager implements ConnectionManager {

        private static final Logger log = Logger.getLogger(Socks5BytestreamsConnectionManager.class.getCanonicalName());
        
        private static final String JAXMPP_KEY = "jaxmpp";
        private static final String PROXY_JID_KEY = "proxy-jid";
        private static final String PROXY_JID_USED_KEY = "proxy-jid-used";
        private static final String STREAMHOST_KEY = "streamhost";
        public static final String PACKET_ID = "packet-id";
        
        private Observable observable = null;
        
        @Override
        public void setObservable(Observable observableParent) {
                observable = ObservableFactory.instance(observableParent);
        }
        
        @Override
        public void addListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
                observable.addListener(eventType, listener);
        }
        
        @Override
        public void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
                observable.removeListener(eventType, listener);
        }        
        
	public static enum State {
		Active,
		ActiveServ,
		Auth,
		AuthResp,
		Closed,
		Command,
		Welcome,
		WelcomeResp,
		WelcomeServ
	}
        
        
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
        
        public void discoverProxy(final JaxmppCore jaxmpp, final ConnectionSession session, final InitializedCallback callback) throws JaxmppException {
                session.setData(JAXMPP_KEY, jaxmpp);
                final DiscoItemsModule discoItemsModule = jaxmpp.getModule(DiscoItemsModule.class);
                JID jid = jaxmpp.getSessionObject().getBindedJid();
                discoItemsModule.getItems(JID.jidInstance(jid.getDomain()), new DiscoItemsModule.DiscoItemsAsyncCallback() {
                        @Override
                        public void onError(Stanza responseStanza, XMPPException.ErrorCondition error) throws JaxmppException {
                                proxyDiscoveryError(jaxmpp, session, callback, "not supported by this server");
                        }

                        @Override
                        public void onInfoReceived(String attribute, final ArrayList<DiscoItemsModule.Item> items) throws XMLException {
                                final int all = items.size();
                                if (all == 0) {
                                        proxyDiscoveryError(jaxmpp, session, callback, "not supported by this server");
                                } else {
                                        final AtomicInteger counter = new AtomicInteger(0);
                                        final DiscoInfoModule discoInfoModule = jaxmpp.getModule(DiscoInfoModule.class);
                                        final List<JID> proxyComponents = Collections.synchronizedList(new ArrayList<JID>());
                                        for (final DiscoItemsModule.Item item : items) {
                                                try {
                                                        discoInfoModule.getInfo(item.getJid(), new DiscoInfoModule.DiscoInfoAsyncCallback(null) {
                                                                protected void checkFinished() {
                                                                        int count = counter.addAndGet(1);
                                                                        if (count == items.size()) {
                                                                                proxyDiscoveryFinished(jaxmpp, session, callback, proxyComponents);
                                                                        }
                                                                }

                                                                @Override
                                                                public void onError(Stanza responseStanza, XMPPException.ErrorCondition error) throws JaxmppException {
                                                                        // TODO Auto-generated method stub
                                                                        checkFinished();
                                                                }

                                                                @Override
                                                                protected void onInfoReceived(String node, Collection<DiscoInfoModule.Identity> identities, Collection<String> features)
                                                                        throws XMLException {
                                                                        if (identities != null) {
                                                                                for (DiscoInfoModule.Identity identity : identities) {
                                                                                        if ("proxy".equals(identity.getCategory()) && "bytestreams".equals(identity.getType())) {
                                                                                                proxyComponents.add(item.getJid());
                                                                                        }
                                                                                }
                                                                        }

                                                                        checkFinished();
                                                                }

                                                                @Override
                                                                public void onTimeout() throws JaxmppException {
                                                                        // TODO Auto-generated method stub
                                                                        checkFinished();
                                                                }
                                                        });
                                                } catch (JaxmppException e) {
                                                        // TODO Auto-generated catch block
                                                        int count = counter.addAndGet(1);
                                                        if (count == items.size()) {
                                                                proxyDiscoveryFinished(jaxmpp, session, callback, proxyComponents);
                                                        }
                                                }
                                        }
                                }
                        }

                        @Override
                        public void onTimeout() throws JaxmppException {
                                proxyDiscoveryError(jaxmpp, session, callback, "proxy discovery timed out");
                        }
                });

        }

        protected void proxyDiscoveryFinished(JaxmppCore jaxmpp, ConnectionSession ft, InitializedCallback callback, List<JID> proxyComponents) {
                JID proxyJid = (proxyComponents == null || proxyComponents.isEmpty()) ? null : proxyComponents.get(0);
                ft.setData(PROXY_JID_KEY, proxyJid);
                callback.initialized(jaxmpp, ft);
        }

        protected void proxyDiscoveryError(JaxmppCore jaxmpp, ConnectionSession ft, InitializedCallback callback, String errorText) {
                log.log(Level.WARNING, "error during Socks5 proxy discovery = {0}", errorText);
                ft.setData(PROXY_JID_KEY, null);
                callback.initialized(jaxmpp, ft);
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
                                if (host.getJid().equals(ft.getSessionObject().getBindedJid().toString())) {
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
        
        protected void connectToProxy(JaxmppCore jaxmpp, ConnectionSession session, Streamhost host) throws IOException {
                InetSocketAddress address = new InetSocketAddress(host.getAddress(), host.getPort());
                SocketChannel channel = SocketChannel.open(address);
                if (!session.isIncoming() && !session.getPeer().equals(host.getJid())) {
                     session.setData(PROXY_JID_USED_KEY, host.getJid());
                }
                session.setData(STREAMHOST_KEY, host);
                handleConnection(session, channel.socket(), false);
        }
        
        private static final long TIMEOUT = 3 * 60 * 1000;
        
        protected List<Streamhost> getLocalStreamHosts(ConnectionSession session) throws JaxmppException {
                try {
                        StreamhostsResolver streamhostsResolver = UniversalFactory.createInstance(StreamhostsResolver.class.getCanonicalName());
                        TcpServerThread server = new TcpServerThread(0, TIMEOUT);
                        server.setConnectionSession(session);
                        server.start();
                        return streamhostsResolver.getLocalStreamHosts(session.getSessionObject().getBindedJid(), server.getPort());
                }
                catch (Exception ex) {
                        throw new JaxmppException("problem in getting local streamhosts", ex);
                }
        }
        
        protected void handleConnection(ConnectionSession session, Socket socket, boolean incoming) throws IOException {
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(0);
                socket.getChannel().configureBlocking(true);
                State state = incoming ? State.WelcomeServ : State.Welcome;
                
                ByteBuffer buf = ByteBuffer.allocate(4096);
                
                SocketChannel socketChannel = socket.getChannel();
                while(state != State.Closed && state != State.Active && state != State.ActiveServ) {
                        if (state == State.Welcome) {
                                buf.flip();
                        }
                        else {
                                if (!buf.hasRemaining()) {
                                        if (log.isLoggable(Level.WARNING)) {
                                                log.warning("no space to read from socket!!");
                                        }
                                        
                                        buf.clear();
                                }
                                int read = socketChannel.read(buf);
                                if (read == -1) {
                                        state = State.Closed;
                                        break;
                                }
                                
                                if (log.isLoggable(Level.FINEST)) {
                                        log.log(Level.FINEST, "read data = {0} state = {1}", new Object[] { read, state.name() });
                                }
                                buf.flip();
                        }
                        
                        state = processData(session, socketChannel, state, buf);                        
                        
                        if (log.isLoggable(Level.FINEST)) {
                                log.log(Level.FINEST, "socket state changed to = {0}", state);
                        }
                }
  
                switch (state) {
                        case Active:
                                if (session.isIncoming()) {
                                        fireOnConnected(session, socket);
                                }
                                else {
                                        try {
                                                requestActivate(session, socket);
                                        }
                                        catch (JaxmppException ex) {
                                                socket.close();
                                                fireOnFailure(session);
                                        }
                                }
                                break;
                        case ActiveServ:
                                synchronized (session) {                                        
                                        if (((Boolean) session.getData("streamhost-received") == null) 
                                                || ((Boolean) session.getData("streamhost-received") == false)) {
                                                session.setData("socket", socket);                                                
                                        }
                                        else {
                                                session.setData("socket", socket);                                                
                                        }
                                }

                                try {
                                        requestActivate(session, socket);
                                } catch (JaxmppException ex) {
                                        socket.close();
                                        fireOnFailure(session);
                                }
                                
                                break;
                        case Closed:
                                fireOnFailure(session);
                                break;
                }
                buf.clear();
        }
        
        protected void requestActivate(final ConnectionSession session, final Socket socket) throws JaxmppException {
                JaxmppCore jaxmpp = session.getData(JAXMPP_KEY);
                JID usedProxyJid = session.getData(PROXY_JID_USED_KEY);
                jaxmpp.getModule(Socks5BytestreamsModule.class).requestActivate(usedProxyJid, session.getSid(), session.getPeer().toString(), new ActivateCallback() {

                        @Override
                        public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
                                fireOnFailure(session);
                        }

                        @Override
                        public void onSuccess(Stanza responseStanza) throws JaxmppException {
                                fireOnConnected(session, socket);
                        }

                        @Override
                        public void onTimeout() throws JaxmppException {
                                fireOnFailure(session);
                        }
                        
                });
        }
        
        protected static State processData(ConnectionSession ft, SocketChannel socket, State state, ByteBuffer buf) throws IOException {
		if (buf != null && buf.hasRemaining()) {
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "processing received data of size {0} bytes", buf.remaining());
			}

                        switch (state) {
			case WelcomeServ:                               
                                byte ver1 = buf.get();
				if (ver1 != 0x05) {
					log.warning("bad protocol version! ver = " + ver1);
                                        socket.close();
                                        return State.Closed;
				} else {
					int count = buf.get();
					boolean ok = false;
					for (int i = 0; i < count; i++) {
						if (buf.get() == 0x00) {
							ok = true;
							break;
						}
					}

					buf.clear();

					state = State.Command;

					if (ok) {
						if (log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "sending welcome 0x05 0x00");
						}                                                
						socket.write(ByteBuffer.wrap(new byte[] { 0x05, 0x00 }));
					} else {
						if (log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "stopping service {0} after failure during WELCOME step", socket.toString());
						}
                                                socket.close();
                                                return State.Closed;
					}
				}
				break;

			case Command:
				if (log.isLoggable(Level.FINEST)) {
					log.finest("for Command read = " + buf.remaining());
				}
				if (buf.get() != 0x05) {
					log.warning("bad protocol version!");
                                        socket.close();
                                        return State.Closed;
				}
				byte cmd = buf.get();
				buf.get();
				byte atype = buf.get();
				if (cmd == 0x01 && atype == 0x03) {
					byte len = buf.get();
					byte[] data = new byte[len];
					buf.get(data);
					buf.clear();
					ByteBuffer tmp = ByteBuffer.allocate(len + 7);
					tmp.put((byte) 0x05);
					tmp.put((byte) 0x00);
					tmp.put((byte) 0x00);
					tmp.put(atype);
					tmp.put(len);
					tmp.put(data);
					tmp.put((byte) 0x00);
					tmp.put((byte) 0x00);
					tmp.flip();

					if (!checkHash(new String(data), ft)) {
						if (log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "stopping service {0} without file transfer", socket.toString());
						}
                                                socket.close();
                                                return State.Closed;
					}

					state = State.ActiveServ;

                                        if (log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "sending response to COMMAND");
					}
					socket.write(tmp);
				}
				break;

			case WelcomeResp:
				if (log.isLoggable(Level.FINEST)) {
					log.finest("for WELCOME response read = " + buf.remaining());
				}
				int ver = buf.get();
				if (ver != 0x05) {
					log.warning("bad protocol version!");
                                        socket.close();
                                        return State.Closed;
				}
				int status = buf.get();
				buf.clear();
				if (status == 0) {
					state = State.Auth;
				}
				break;

			case AuthResp:
				if (log.isLoggable(Level.FINEST)) {
					log.finest("for AUTH response read = " + buf.remaining());
				}
				if (buf.get() != 0x05) {
					log.warning("bad protocol version!");
				}

                                // let's ignore response for now
				buf.clear();
				state = State.Active;
                                
				break;

			case Active:
                                // this state should not be processed by this method
				buf.clear();
				break;

			default:
                                log.log(Level.WARNING, "wrong state, buffer has remainging = {0}", buf.remaining());
				buf.clear();
			}
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "after processing received data set in state = {0}", state);
			}
		}

		if (state == State.Welcome) {
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "sending WELCOME request");
			}
			ByteBuffer out = ByteBuffer.allocate(128);
                        // version
                        out.put((byte) 0x05);
                        // count
                        out.put((byte) 0x01);
                        // method
                        out.put((byte) 0x00);
			out.flip();
			state = State.WelcomeResp;                        
                        socket.write(out);
                        buf.clear();
		} else if (state == State.Auth) {
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "sending AUTH request");
			}
			state = State.AuthResp;
			ByteBuffer out = ByteBuffer.allocate(256);
			// version
			out.put((byte) 0x05);
			// cmd id (auth)
			out.put((byte) 0x01);
			// reserved 0x00
			out.put((byte) 0x00);
			// auth type
			out.put((byte) 0x03);

			byte[] hexHash = generateHash(ft).getBytes();

			out.put((byte) hexHash.length);
			out.put(hexHash);

			// port
			out.put((byte) 0x00);
			out.put((byte) 0x00);

			out.flip();
                        int len = out.remaining();
			int wrote = socket.write(out);
			if (out.hasRemaining()) {
				log.log(Level.WARNING, "we wrote to stream = {0} but we have remaining = {1}", new Object[] { wrote, out.remaining() });
			}
		}
                
                return state;
        }
        
        protected static boolean checkHash(String data, ConnectionSession session) {
                return data.equals(generateHash(session));
        }
        
        protected static String generateHash(ConnectionSession session) {
		try {
			String data = session.isIncoming()
                                ? session.getSid() + session.getPeer().toString() + session.getSessionObject().getBindedJid().toString()
                                : session.getSid() + session.getSessionObject().getBindedJid().toString() + session.getPeer();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data.getBytes());
			byte[] buff = md.digest();
			StringBuilder enc = new StringBuilder();
			for (byte b : buff) {
				char ch = Character.forDigit((b >> 4) & 0xF, 16);
				enc.append(ch);
				ch = Character.forDigit(b & 0xF, 16);
				enc.append(ch);
			} // end of for (b : digest)
			return enc.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			return "";
		}                
        }
        
        private void fireOnConnected(ConnectionSession session, Socket socket) {
                try {
                        observable.fireEvent(CONNECTION_ESTABLISHED, new ConnectionEvent(CONNECTION_ESTABLISHED, session.getSessionObject(), session, socket));
                }
                catch (Exception ex) {
                        log.log(Level.SEVERE, "failure firing ConnectionEstablished event");
                }
        }
        
        private void fireOnFailure(ConnectionSession session) {
                try {
                        observable.fireEvent(CONNECTION_FAILED, new ConnectionEvent(CONNECTION_FAILED, session.getSessionObject(), session));
                }
                catch (Exception ex) {
                        log.log(Level.SEVERE, "failure firing ConnectionEstablished event");
                }
        }

        // ---------------------------------------------------------------------------------------

        private Timer timer = new Timer();
        /**
         * Internal TCP connection manager
         */
        
        private class TcpServerThread extends Thread {
        
                private ConnectionSession session = null;
                private ServerSocketChannel serverSocket = null;
                private long timeout = 0;

                public TcpServerThread(int port, long timeout) throws IOException {
                        serverSocket = ServerSocketChannel.open();
                        serverSocket.socket().bind(null);
//                        serverSocket = new ServerSocket(port);
                        if (timeout != 0) {
                                this.timeout = timeout;
                        }
                }
                
                public int getPort() {
                        return serverSocket.socket().getLocalPort();
                }
                
                public void setConnectionSession(ConnectionSession session) {
                        this.session = session;
                }
                
                @Override
                public void run() {
                        timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                        try {
                                                serverSocket.close();
                                        }
                                        catch(Exception ex) {
                                                log.log(Level.WARNING, "problem with closing server socket", ex);
                                        }
                                }
                        }, timeout);
                        
                        while (serverSocket.socket().isBound()) {                                
                                try {
                                        SocketChannel socketChannel = serverSocket.accept();
                                        handleConnection(session, socketChannel.socket(), true);
                                } catch (ClosedChannelException ex) {
                                        log.log(Level.SEVERE, null, ex);
                                        break;
                                } catch (IOException ex) {
                                        log.log(Level.SEVERE, null, ex);
                                }
                        }
                }
        };
}
