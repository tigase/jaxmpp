/*
 * WebSocketConnector.java
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
package tigase.jaxmpp.j2se.connectors.websocket;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.AbstractWebSocketConnector;
import tigase.jaxmpp.core.client.connector.SeeOtherHostHandler;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.utils.MutableBoolean;
import tigase.jaxmpp.j2se.connectors.socket.Reader;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;
import tigase.jaxmpp.j2se.connectors.socket.Worker;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;

import static tigase.jaxmpp.j2se.connectors.socket.SocketConnector.*;

/**
 * @author andrzej
 */
public class WebSocketConnector
		extends AbstractWebSocketConnector {

	private final static Charset UTF_CHARSET = Charset.forName("UTF-8");

	private static final String EOL = "\r\n";

	private static final byte[] HTTP_RESPONSE_101 = "HTTP/1.1 101 ".getBytes(UTF_CHARSET);

	private static final String SEC_UUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	/**
	 * Property to specify custom {@linkplain Socket#setSoTimeout(int) socket timeout} for Socket. Default is {@link
	 * SocketConnector#DEFAULT_SOCKET_TIMEOUT 180000 ms}.
	 */
	private static final String WEB_SOCKET_TIMEOUT_KEY = "WEB_SOCKET_TIMEOUT_KEY";

	private final Object ioMutex = new Object();
	private Timer closeTimer;
	private byte[] mask = new byte[4];
	private TimerTask pingTask;
	private Random random = new SecureRandom();
	private Reader reader = null;
	private Socket socket = null;
	private Timer timer = null;
	private Worker worker = null;
	private OutputStream writer = null;

	public WebSocketConnector(Context context) {
		super(context);
		context.getEventBus().addHandler(SeeOtherHostHandler.SeeOtherHostEvent.class, new SeeOtherHostHandler() {

			@Override
			public void onSeeOtherHost(String seeHost, MutableBoolean handled) {
				WebSocketConnector.this.context.getSessionObject()
						.setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, seeHost);
			}
		});
	}

	private void closeSocket() {
		if (socket.isConnected()) {
			try {
				writer.write(new byte[]{(byte) 0x88, (byte) 0x00});
				socket.close();
			} catch (IOException ex) {
				log.log(Level.FINEST, "Problem with closing socket", ex);
			}
		}
	}

	protected KeyManager[] getKeyManagers() throws NoSuchAlgorithmException {
		KeyManager[] result = context.getSessionObject().getProperty(KEY_MANAGERS_KEY);
		return result == null ? new KeyManager[0] : result;
	}

	private void handshake(URI uri) throws IOException {
		String wskey = UUID.randomUUID().toString();
		StringBuilder sb = new StringBuilder();
		sb.append("GET ").append(uri.getPath() != null ? uri.getPath() : "/").append(" HTTP/1.1").append(EOL);
		sb.append("Host: ").append(uri.getHost());
		if (uri.getPort() != -1) {
			sb.append(":").append(uri.getPort());
		}
		sb.append(EOL);
		sb.append("Connection: Upgrade").append(EOL);
		sb.append("Upgrade: websocket").append(EOL);
		sb.append("Sec-WebSocket-Key: ").append(wskey).append(EOL);
		sb.append("Sec-WebSocket-Protocol: ").append("xmpp").append(",").append("xmpp-framing").append(EOL);
		sb.append("Sec-WebSocket-Version: 13").append(EOL);
		sb.append(EOL);
		byte[] buffer = sb.toString().getBytes(UTF_CHARSET);

		socket.getOutputStream().write(buffer);
		buffer = new byte[4096];
		int read = 0;
		Map<String, String> headers = new HashMap<String, String>();
		sb = new StringBuilder();
		boolean eol = false;
		boolean httpResponseOk = false;
		String key = null;
		while ((read = socket.getInputStream().read(buffer, 0, buffer.length)) != -1) {
			if (!httpResponseOk) {
				for (int i = 0; i < HTTP_RESPONSE_101.length; i++) {
					if (buffer[i] != HTTP_RESPONSE_101[i]) {
						throw new IOException("Wrong HTTP response, got: " + new String(buffer));
					}
				}
			}
			boolean headersRead = false;
			for (int i = 0; i < read; i++) {
				byte b = buffer[i];
				switch (b) {
					case ':':
						if (key == null) {
							key = sb.toString();
							sb = new StringBuilder(64);
							i++;
						} else {
							sb.append((char) b);
						}
						eol = false;
						break;
					case '\n':
						break;
					case '\r':
						if (eol) {
							headersRead = true;
							break;
						}
						if (key != null) {
							headers.put(key.trim(), sb.toString().trim());
							key = null;
						}
						sb = new StringBuilder(64);
						eol = true;
						break;
					default:
						sb.append((char) b);
						eol = false;
						break;
				}
				if (headersRead) {
					break;
				}
			}
			if (headersRead) {
				break;
			}
		}
		// if (!"websocket".equals(headers.get("Upgrade"))) {
		// throw new IOException("Bad upgrade header in HTTP response");
		// }
		try {
			String accept = Base64.encode(
					MessageDigest.getInstance("SHA-1").digest((wskey + SEC_UUID).getBytes(UTF_CHARSET)));
			if (!accept.equals(headers.get("Sec-WebSocket-Accept"))) {
				throw new IOException("Invalid Sec-WebSocket-Accept header value");
			}
		} catch (NoSuchAlgorithmException ex) {
			throw new IOException("Could not validate 'Sec-WebSocket-Accept' header", ex);
		}
		String protocol = headers.get("Sec-WebSocket-Protocol");
		if ("xmpp-framing".equals(protocol)) {
			rfcCompatible = true;
		} else if (!"xmpp".equals(protocol)) {
			throw new IOException("Established unsupported WebSocket protocol: " + protocol);
		}
	}

	@Override
	public boolean isSecure() {
		return context.getSessionObject().getProperty(ENCRYPTED_KEY) == Boolean.TRUE;
	}

	protected void onErrorInThread(Exception e) throws JaxmppException {
		if (getState() == State.disconnected) {
			return;
		}
		terminateAllWorkers();
		fireOnError(null, e, context.getSessionObject());
	}

	protected void send(byte[] buffer) throws JaxmppException {
		synchronized (ioMutex) {
			if (writer != null) {
				try {
					if (log.isLoggable(Level.FINEST)) {
						log.finest("Send: " + new String(buffer));
					}

					// prepare WebSocket header according to Hybi specification
					int size = buffer.length;
					random.nextBytes(mask);
					byte maskedLen = (byte) 0x80;
					ByteBuffer bbuf = ByteBuffer.allocate(12);
					bbuf.put((byte) 0x81);
					if (size <= 125) {
						maskedLen |= (byte) size;
						bbuf.put(maskedLen);
					} else if (size <= 0xFFFF) {
						maskedLen |= (byte) 0x7E;
						bbuf.put(maskedLen);
						bbuf.putShort((short) size);
					} else {
						maskedLen |= (byte) 0x7F;
						bbuf.put(maskedLen);
						bbuf.putLong(size);
					}
					bbuf.flip();
					writer.write(bbuf.array(), 0, bbuf.remaining());
					writer.write(mask, 0, 4);

					for (int i = 0; i < buffer.length; i++) {
						buffer[i] = (byte) (buffer[i] ^ mask[i % 4]);
					}
					// send actual data
					writer.write(buffer);
					writer.flush();
				} catch (IOException e) {
					throw new JaxmppException(e);

				}
			}
		}
	}

	@Override
	public void send(Element stanza) throws JaxmppException {
		synchronized (ioMutex) {
			if (writer != null) {
				super.send(stanza);
			}
		}
	}

	@Override
	public void send(final String data) throws JaxmppException {
		if (getState() == State.connected || getState() == State.connecting || getState() == State.disconnecting) {
			send(data.getBytes(UTF_CHARSET));
		} else {
			throw new JaxmppException("Not connected");
		}
	}

	@Override
	public void start() throws JaxmppException {
		log.fine("Start connector.");
		super.start();
		if (timer != null) {
			try {
				timer.cancel();
			} catch (Exception e) {
			}
		}
		timer = new Timer(true);

		if (context.getSessionObject().getProperty(HOSTNAME_VERIFIER_DISABLED_KEY) == Boolean.TRUE) {
			context.getSessionObject().setProperty(HOSTNAME_VERIFIER_KEY, null);
		} else if (context.getSessionObject().getProperty(HOSTNAME_VERIFIER_KEY) == null) {
			context.getSessionObject().setProperty(HOSTNAME_VERIFIER_KEY, DEFAULT_HOSTNAME_VERIFIER);
		}

		setStage(State.connecting);

		try {
			String url = context.getSessionObject().getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
			URI uri = URI.create(url);
			InetAddress x = InetAddress.getByName(uri.getHost());
			boolean isSecure = uri.getScheme().startsWith("wss");

			context.getSessionObject().setProperty(SessionObject.Scope.stream, DISABLE_KEEPALIVE_KEY, Boolean.FALSE);

			if (log.isLoggable(Level.FINER)) {
				log.finer("Preparing connection to " + uri.getHost());
			}

			int port = uri.getPort() == -1 ? (isSecure ? 443 : 80) : uri.getPort();

			log.info("Opening connection to " + x + ":" + port);

			if (context.getSessionObject().getProperty(Connector.PROXY_HOST) != null) {
				final String proxyHost = context.getSessionObject().getProperty(Connector.PROXY_HOST);
				final int proxyPort = context.getSessionObject().getProperty(Connector.PROXY_PORT);
				Proxy.Type proxyType = context.getSessionObject().getProperty(Connector.PROXY_TYPE);
				if (proxyType == null) {
					proxyType = Proxy.Type.HTTP;
				}

				log.info("Using " + proxyType + " proxy: " + proxyHost + ":" + proxyPort);

				SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
				Proxy proxy = new Proxy(proxyType, addr);
				socket = new Socket(proxy);
			} else {
				socket = new Socket();
			}

			Integer soTimeout = getTimeout(WEB_SOCKET_TIMEOUT_KEY, DEFAULT_SOCKET_TIMEOUT);
			if (soTimeout != null) {
				socket.setSoTimeout(soTimeout);
			}
			socket.setKeepAlive(false);
			socket.setTcpNoDelay(true);
			socket.connect(new InetSocketAddress(x, port));

			if (isSecure) {
				TrustManager[] trustManagers = context.getSessionObject().getProperty(TRUST_MANAGERS_KEY);
				final SSLSocketFactory factory;
				if (trustManagers == null) {
					if (context.getSessionObject().getProperty(SSL_SOCKET_FACTORY_KEY) != null) {
						factory = context.getSessionObject().getProperty(SSL_SOCKET_FACTORY_KEY);
					} else {
						factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
					}
				} else {
					SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(getKeyManagers(), trustManagers, new SecureRandom());
					factory = ctx.getSocketFactory();
				}

				socket = factory.createSocket(socket, x.getHostAddress(), port, true);
				socket.setSoTimeout(0);
				socket.setKeepAlive(false);
				socket.setTcpNoDelay(true);
				((SSLSocket) socket).setUseClientMode(true);
				((SSLSocket) socket).addHandshakeCompletedListener(new HandshakeCompletedListener() {
					@Override
					public void handshakeCompleted(HandshakeCompletedEvent arg0) {
						log.info("TLS completed " + arg0);
						context.getSessionObject().setProperty(SessionObject.Scope.stream, ENCRYPTED_KEY, Boolean.TRUE);
						context.getEventBus()
								.fire(new EncryptionEstablishedHandler.EncryptionEstablishedEvent(
										context.getSessionObject()));
					}
				});
			}
			// if
			// (context.getSessionObject().getProperty(DISABLE_SOCKET_TIMEOUT_KEY)
			// == null
			// || ((Boolean)
			// context.getSessionObject().getProperty(DISABLE_SOCKET_TIMEOUT_KEY)).booleanValue())
			// {
			// socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
			// }
			// writer = new BufferedOutputStream(socket.getOutputStream());
			writer = socket.getOutputStream();
			worker = new Worker(this) {

				@Override
				protected Reader getReader() {
					return reader;
				}

				@Override
				protected void onErrorInThread(Exception e) throws JaxmppException {
					WebSocketConnector.this.onErrorInThread(e);
				}

				@Override
				protected void onStreamStart(Map<String, String> attribs) {
					WebSocketConnector.this.onStreamStart(attribs);
				}

				@Override
				protected void onStreamTerminate() throws JaxmppException {
					WebSocketConnector.this.onStreamTerminate();
				}

				@Override
				protected void processElement(Element elem) throws JaxmppException {
					WebSocketConnector.this.processElement(elem);
				}

				@Override
				protected void workerTerminated() {
					WebSocketConnector.this.workerTerminated(this);
				}

			};

			log.finest("Starting WebSocket handshake...");
			handshake(uri);

			//reader = new WebSocketReader(new BufferedInputStream(socket.getInputStream()));
			reader = new WebSocketReader(socket.getInputStream());
			log.finest("Starting worker...");
			worker.start();

			restartStream();

			setStage(State.connected);

			this.pingTask = new TimerTask() {

				@Override
				public void run() {
					Thread t = new Thread("Keep-Alive-Thread") {
						@Override
						public void run() {
							try {
								keepalive();
							} catch (JaxmppException e) {
								log.log(Level.SEVERE, "Can't ping!", e);
							}
						}
					};
					t.setDaemon(true);
					t.start();
				}
			};
			if (context.getSessionObject().getProperty(EXTERNAL_KEEPALIVE_KEY) == null ||
					((Boolean) context.getSessionObject().getProperty(EXTERNAL_KEEPALIVE_KEY) == false)) {
				Integer defaultDelay = getTimeout(PLAIN_SOCKET_TIMEOUT_KEY, DEFAULT_SOCKET_TIMEOUT);
				defaultDelay = defaultDelay == null ? -1 : defaultDelay - 1000 * 5;

				Integer delay = getTimeout(SocketConnector.KEEP_ALIVE_DELAY_KEY, defaultDelay);

				if (log.isLoggable(Level.CONFIG)) {
					log.config("Whitespace ping period is setted to " + delay + "ms");
				}

				if (delay != null) {
					timer.schedule(pingTask, delay, delay);
				}
			}

			fireOnConnected(context.getSessionObject());
		} catch (Exception e) {
			terminateAllWorkers();
			onError(null, e);
			throw new JaxmppException(e);
		}
	}

	@Override
	protected void terminateAllWorkers() throws JaxmppException {
		log.finest("Terminating all workers");
		if (this.pingTask != null) {
			this.pingTask.cancel();
			this.pingTask = null;
		}
//		setStage(State.disconnected);
		if (socket != null && socket.isConnected()) {
			if (closeTimer != null) {
				closeTimer.cancel();
			}
			closeTimer = new Timer(true);
			closeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					closeSocket();
					closeTimer.cancel();
					try {
						setStage(State.disconnected);
					} catch (JaxmppException ex) {
						log.log(Level.FINEST, "Exception while updating connector state during connection close", ex);
					}
					closeTimer = null;
				}
			}, 3 * 1000);
			// sending websocket close

			//socket.close();
		} else {
			setStage(State.disconnected);
		}

		// it there a need for this?
//		try {
//			if (worker != null)
//				worker.interrupt();
//		} catch (Exception e) {
//			log.log(Level.FINEST, "Problem with interrupting w2", e);
//		}
		try {
			if (timer != null) {
				timer.cancel();
			}
		} catch (Exception e) {
			log.log(Level.FINEST, "Problem with canceling timer", e);
		} finally {
			timer = null;
		}
	}

	private void workerTerminated(final Worker worker) {
		try {
			if (closeTimer != null) {
				closeTimer.cancel();
				closeTimer = null;
			}
			setStage(State.disconnected);
		} catch (JaxmppException e) {
		}
		log.finest("Worker terminated");
		try {
			if (this.context.getSessionObject().getProperty(SocketConnector.RECONNECTING_KEY) == Boolean.TRUE) {
				this.context.getSessionObject().setProperty(SocketConnector.RECONNECTING_KEY, null);
				context.getEventBus()
						.fire(new SocketConnector.HostChangedHandler.HostChangedEvent(context.getSessionObject()));
				log.finest("Restarting...");
				start();
			} else {
				context.getEventBus().fire(new DisconnectedHandler.DisconnectedEvent(context.getSessionObject()));
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Cannot terminate worker correctly", e);
		}
	}
}
