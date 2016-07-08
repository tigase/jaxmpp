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

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionEndpoint;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule.ActivateCallback;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Streamhost;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.j2se.filetransfer.FileTransfer;
import tigase.jaxmpp.j2se.filetransfer.FileTransferManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author andrzej
 */
public abstract class Socks5ConnectionManager implements ConnectionManager {

	public static final String PACKET_ID = "packet-id";
	protected static final String JAXMPP_KEY = "jaxmpp";
	protected static final String PROXY_JID_KEY = "proxy-jid";
	protected static final String PROXY_JID_USED_KEY = "proxy-jid-used";
	protected static final String SID_KEY = "socks5-sid";
	protected static final String STREAMHOST_KEY = "streamhost";
	private final static Charset UTF_CHARSET = Charset.forName("UTF-8");
	private static final Logger log = Logger.getLogger(Socks5ConnectionManager.class.getCanonicalName());
	private static final Map<String, ConnectionSession> sessions = new HashMap<String, ConnectionSession>();
	private static final long TIMEOUT = 15 * 60 * 1000;
	private static TcpServerThread server = null;
	// ---------------------------------------------------------------------------------------
	private static Timer timer = new Timer();
	protected Context context;

	protected static boolean checkHash(String data, ConnectionSession session) {
		return data.equals(generateHash(session));
	}

	protected static void clearSessions() {
		synchronized (sessions) {
			for (ConnectionSession session : new HashSet<ConnectionSession>(sessions.values())) {
				Socks5ConnectionManager connectionManager = session.getData(Socks5ConnectionManager.class.getCanonicalName());
				connectionManager.fireOnFailure(session);
			}
			sessions.clear();
		}
	}

	protected static String generateHash(ConnectionSession session) {
		try {
			String sid = session.getData(SID_KEY);
			String data = session.isIncoming()
					? sid + session.getPeer().toString()
							+ ResourceBinderModule.getBindedJID(session.getSessionObject()).toString()
					: sid + ResourceBinderModule.getBindedJID(session.getSessionObject()).toString() + session.getPeer();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data.getBytes(UTF_CHARSET));
			byte[] buff = md.digest();
			StringBuilder enc = new StringBuilder();
			for (byte b : buff) {
				char ch = Character.forDigit((b >> 4) & 0xF, 16);
				enc.append(ch);
				ch = Character.forDigit(b & 0xF, 16);
				enc.append(ch);
			} // end of for (b : digest)
			if (log.isLoggable(Level.FINEST)) {
				log.finest("for " + ResourceBinderModule.getBindedJID(session.getSessionObject()).toString() + " generated "
						+ data + " hash = " + enc.toString());
			}
			return enc.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			return "";
		}
	}

	protected static ConnectionSession getSession(String hash) {
		synchronized (sessions) {
			return sessions.get(hash);
		}
	}

	protected static State processData(ConnectionSession ft, SocketChannel socket, State state, ByteBuffer buf)
			throws IOException {
		if (buf != null && buf.hasRemaining()) {
			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "processing received data of size {0} bytes", buf.remaining());
			}

			switch (state) {
			case WelcomeServ:
				byte ver1 = buf.get();
				if (ver1 != 0x05) {
					log.fine("bad protocol version! ver = " + ver1);
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
					log.fine("bad protocol version!");
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

					ft = getSession(new String(data));
					if (ft == null) {
						// if (!checkHash(new String(data), ft)) {
						if (log.isLoggable(Level.FINEST)) {
							log.log(Level.FINEST, "stopping service {0} without file transfer", socket.toString());
						}
						socket.close();
						return State.Closed;
					}

					if (log.isLoggable(Level.FINEST)) {
						log.log(Level.FINEST, "sending response to COMMAND");
					}
					socket.write(tmp);
					try {
						// small delay to workaround issue with Psi+
						Thread.sleep(100);
					} catch (Exception ex) {
					}
					if (!socket.socket().isClosed()) {
						synchronized (ft) {
							List<Socket> sockets = ft.getData("sockets");
							if (sockets == null) {
								sockets = new ArrayList<Socket>();
								ft.setData("sockets", sockets);
							}
							sockets.add(socket.socket());
						}
						state = State.ActiveServ;
					} else
						return State.Closed;
				}
				break;

			case WelcomeResp:
				if (log.isLoggable(Level.FINEST)) {
					log.finest("for WELCOME response read = " + buf.remaining());
				}
				int ver = buf.get();
				if (ver != 0x05) {
					log.fine("bad protocol version!");
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
					log.fine("bad protocol version!");
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
				log.log(Level.FINE, "wrong state, buffer has remainging = {0}", buf.remaining());
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

			byte[] hexHash = generateHash(ft).getBytes(UTF_CHARSET);

			out.put((byte) hexHash.length);
			out.put(hexHash);

			// port
			out.put((byte) 0x00);
			out.put((byte) 0x00);

			out.flip();
			int len = out.remaining();
			int wrote = socket.write(out);
			if (out.hasRemaining()) {
				log.log(Level.FINE, "we wrote to stream = {0} but we have remaining = {1}",
						new Object[] { wrote, out.remaining() });
			}
		}

		return state;
	}

	protected static void registerSession(ConnectionSession session, String sid, Socks5ConnectionManager instance) {
		synchronized (sessions) {
			session.setData(SID_KEY, sid);
			String hash = generateHash(session);
			session.setData(Socks5ConnectionManager.class.getCanonicalName(), instance);
			sessions.put(hash, session);
		}
	}

	protected static void unregisterSession(ConnectionSession session) {
		synchronized (sessions) {
			String hash = generateHash(session);
			sessions.remove(hash);

			if (sessions.isEmpty()) {
				if (server != null) {
					server.shutdown();
				}
			}
		}
	}

	protected void connectToProxy(JaxmppCore jaxmpp, ConnectionSession session, String sid, ConnectionEndpoint host)
			throws IOException, JaxmppException {
		session.setData(JAXMPP_KEY, jaxmpp);
		InetSocketAddress address = new InetSocketAddress(host.getHost(), host.getPort());
		SocketChannel channel = SocketChannel.open(address);
		if (!session.isIncoming() && !session.getPeer().equals(host.getJid())) {
			session.setData(PROXY_JID_USED_KEY, host.getJid());
		}
		session.setData(SID_KEY, sid);
		session.setData(STREAMHOST_KEY, host);
		handleConnection(session, channel.socket(), false);
	}

	public void discoverProxy(final JaxmppCore jaxmpp, final ConnectionSession session, final InitializedCallback callback)
			throws JaxmppException {
		session.setData(JAXMPP_KEY, jaxmpp);
		final DiscoveryModule discoItemsModule = jaxmpp.getModule(DiscoveryModule.class);
		JID jid = ResourceBinderModule.getBindedJID(jaxmpp.getSessionObject());
		discoItemsModule.getItems(JID.jidInstance(jid.getDomain()), new DiscoveryModule.DiscoItemsAsyncCallback() {
			@Override
			public void onError(Stanza responseStanza, XMPPException.ErrorCondition error) throws JaxmppException {
				proxyDiscoveryError(jaxmpp, session, callback, "not supported by this server");
			}

			@Override
			public void onInfoReceived(String attribute, final ArrayList<DiscoveryModule.Item> items) throws XMLException {
				final int all = items.size();
				if (all == 0) {
					proxyDiscoveryError(jaxmpp, session, callback, "not supported by this server");
				} else {
					final AtomicInteger counter = new AtomicInteger(0);
					final DiscoveryModule discoInfoModule = jaxmpp.getModule(DiscoveryModule.class);
					final List<JID> proxyComponents = Collections.synchronizedList(new ArrayList<JID>());
					for (final DiscoveryModule.Item item : items) {
						try {
							discoInfoModule.getInfo(item.getJid(), new DiscoveryModule.DiscoInfoAsyncCallback(null) {
								protected void checkFinished() {
									int count = counter.addAndGet(1);
									if (count == items.size()) {
										proxyDiscoveryFinished(jaxmpp, session, callback, proxyComponents);
									}
								}

								@Override
								public void onError(Stanza responseStanza, XMPPException.ErrorCondition error)
										throws JaxmppException {
									// TODO Auto-generated method stub
									checkFinished();
								}

								@Override
								protected void onInfoReceived(String node, Collection<DiscoveryModule.Identity> identities,
										Collection<String> features) throws XMLException {
									if (identities != null) {
										for (DiscoveryModule.Identity identity : identities) {
											if ("proxy".equals(identity.getCategory())
													&& "bytestreams".equals(identity.getType())) {
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

	protected void fireOnConnected(ConnectionSession session) {
		List<Socket> sockets = session.getData("sockets");
		for (Socket socket : sockets) {
			if (socket.isClosed())
				continue;
			try {
				socket.getInputStream().read(new byte[0]);
				socket.getOutputStream().write(new byte[0]);
			} catch (Exception ex) {
			}
			if (socket.isClosed())
				continue;
			fireOnConnected(session, socket);
		}
	}

	protected void fireOnConnected(ConnectionSession session, Socket socket) {
		try {
			context.getEventBus().fire(
					new ConnectionEstablishedHandler.ConnectionEstablishedEvent(session.getSessionObject(), session, socket)
			);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "failure firing ConnectionEstablished event", ex);
		}
	}

	protected void fireOnFailure(ConnectionSession session) {
		try {
			unregisterSession(session);
			context.getEventBus().fire(new ConnectionFailedHandler.ConnectionFailedEvent(session.getSessionObject(), session)
			);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "failure firing ConnectionFailed event", ex);
		}
	}

	protected List<Streamhost> getLocalStreamHosts(ConnectionSession session, String sid) throws JaxmppException {
		try {
			StreamhostsResolver streamhostsResolver = UniversalFactory.createInstance(
					StreamhostsResolver.class.getCanonicalName());
			// TcpServerThread server = new TcpServerThread(0, TIMEOUT);
			// server.setConnectionSession(session);
			// server.start();
			synchronized (TcpServerThread.class) {
				// synchronized(Socks5ConnectionManager.class) {
				if (server == null || !server.isAlive()) {
					server = new TcpServerThread(0);
					server.start();
				}
				registerSession(session, sid, this);
			}
			return streamhostsResolver.getLocalStreamHosts(ResourceBinderModule.getBindedJID(session.getSessionObject()),
					server.getPort());
		} catch (Exception ex) {
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
		while (state != State.Closed && state != State.Active && state != State.ActiveServ) {
			if (state == State.Welcome) {
				buf.flip();
			} else {
				if (!buf.hasRemaining()) {
					if (log.isLoggable(Level.FINE)) {
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
			} else {
				try {
					requestActivate(session, socket);
				} catch (JaxmppException ex) {
					socket.close();
					fireOnFailure(session);
				}
			}
			break;
		case ActiveServ:
			// synchronized (session) {
			// if (((Boolean) session.getData("streamhost-received") == null)
			// || ((Boolean) session.getData("streamhost-received") == false)) {
			// session.setData("socket", socket);
			// } else {
			// session.setData("socket", socket);
			// }
			// }

			// why do we need this? activation is done only for outgoing
			// connections
			// try {
			// requestActivate(session, socket);
			// } catch (JaxmppException ex) {
			// socket.close();
			// fireOnFailure(session);
			// }

			break;
		case Closed:
			if (!incoming) {
				// fireOnFailure(session);
				throw new IOException("Could not establish Socks5 connection");
			}
			break;
		}
		buf.clear();
	}

	protected void proxyDiscoveryError(JaxmppCore jaxmpp, ConnectionSession ft, InitializedCallback callback,
			String errorText) {
		log.log(Level.FINE, "error during Socks5 proxy discovery = {0}", errorText);
		ft.setData(PROXY_JID_KEY, null);
		callback.initialized(jaxmpp, ft);
	}

	protected void proxyDiscoveryFinished(JaxmppCore jaxmpp, ConnectionSession ft, InitializedCallback callback,
			List<JID> proxyComponents) {
		JID proxyJid = (proxyComponents == null || proxyComponents.isEmpty()) ? null : proxyComponents.get(0);
		ft.setData(PROXY_JID_KEY, proxyJid);
		callback.initialized(jaxmpp, ft);
	}

	protected void requestActivate(final ConnectionSession session, final Socket socket) throws JaxmppException {
		JaxmppCore jaxmpp = session.getData(JAXMPP_KEY);
		JID usedProxyJid = session.getData(PROXY_JID_USED_KEY);
		if (jaxmpp == null) {
			log.severe("no jaxmpp instance!!");
		} else if (session.getPeer() == null) {
			log.fine("no peer");
		}
		jaxmpp.getModule(Socks5BytestreamsModule.class).requestActivate(usedProxyJid, session.getSid(), session.getPeer(),
				new ActivateCallback() {
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

	@Override
	public void setContext(Context context) {
		this.context = context;
		this.context.getEventBus().addHandler(FileTransferManager.FileTransferSuccessHandler.FileTransferSuccessEvent.class,
				new FileTransferManager.FileTransferSuccessHandler() {
					@Override
					public void onFileTransferSuccess(SessionObject sessionObject, FileTransfer fileTransfer) {
						unregisterSession(fileTransfer);
					}
				});
		this.context.getEventBus().addHandler(FileTransferManager.FileTransferFailureHandler.FileTransferFailureEvent.class,
				new FileTransferManager.FileTransferFailureHandler() {
					@Override
					public void onFileTransferFailure(SessionObject sessionObject, FileTransfer fileTransfer) {
						unregisterSession(fileTransfer);
					}
				});
	}

	public enum State {

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

	private class IncomingConnectionHandlerThread extends Thread {

		private final SocketChannel socketChannel;

		private IncomingConnectionHandlerThread(SocketChannel channel) {
			this.socketChannel = channel;
		}

		@Override
		public void run() {
			try {
				handleConnection(null, socketChannel.socket(), true);
			} catch (IOException ex) {
				log.log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Internal TCP connection manager
	 */
	private class TcpServerThread extends Thread {

		// private ConnectionSession session = null;
		private ServerSocketChannel serverSocket = null;
		private boolean shutdown = false;

		private TimerTask shutdownTask = null;
		private long timeout = TIMEOUT;

		public TcpServerThread(int port /* , long timeout */) throws IOException {
			serverSocket = ServerSocketChannel.open();
			serverSocket.socket().bind(null);
			setDaemon(true);
			// serverSocket = new ServerSocket(port);
			// if (timeout != 0) {
			// this.timeout = timeout;
			// }
		}

		public int getPort() {
			if (shutdownTask != null) {
				shutdownTask.cancel();
				shutdownTask = null;
			}

			shutdownTask = new TimerTask() {
				@Override
				public void run() {
					try {
						synchronized (TcpServerThread.class) {
							if (shutdownTask == null) {
								return;
							}

							clearSessions();
						}
					} catch (Exception ex) {
						log.log(Level.FINEST, "problem with closing server socket", ex);
					}
				}
			};
			timer.schedule(shutdownTask, timeout);

			return serverSocket.socket().getLocalPort();
		}

		@Override
		public void run() {
			while (serverSocket.socket().isBound() && !shutdown) {
				try {
					SocketChannel socketChannel = serverSocket.accept();
					new IncomingConnectionHandlerThread(socketChannel).start();
				} catch (ClosedChannelException ex) {
					log.log(Level.FINEST, "Socket already closed, when we tried to accept connection", ex);
					// break;
				} catch (IOException ex) {
					log.log(Level.FINEST, "Exception occurred while we tried to accept incoming connection", ex);
				}
			}
		}

		// public void setConnectionSession(ConnectionSession session) {
		// this.session = session;
		// }

		public void shutdown() {
			synchronized (TcpServerThread.class) {
				if (shutdownTask != null) {
					shutdownTask.cancel();
					shutdownTask = null;
				}

				shutdown = true;
				try {
					serverSocket.close();
				} catch (IOException ex) {
					log.log(Level.FINEST, "problem with closing server socket", ex);
				}
			}
		}
	}
}
