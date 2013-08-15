/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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
package tigase.jaxmpp.j2se.connectors.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule;
import tigase.jaxmpp.j2se.DNSResolver;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

/**
 * 
 */
public class SocketConnector implements Connector {

	public static interface DnsResolver {

		List<Entry> resolve(String hostname);
	}

	public final static class Entry {

		private final String hostname;

		private final Integer port;

		public Entry(String host, Integer port) {
			this.hostname = host;
			this.port = port;
		}

		public String getHostname() {
			return hostname;
		}

		public Integer getPort() {
			return port;
		}

		@Override
		public String toString() {
			return hostname + ":" + port;
		}

	}

	/**
	 * New Reader class replaces standard InputStreamReader as it cannot read
	 * from InflaterInputStream.
	 */
	private class Reader {

		private final ByteBuffer buf = ByteBuffer.allocate(DEFAULT_SOCKET_BUFFER_SIZE);

		private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

		private final InputStream inputStream;

		public Reader(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public int read(char[] cbuf) throws IOException {
			byte[] arr = buf.array();
			int read = inputStream.read(arr, 0, arr.length);
			buf.position(read);
			buf.flip();

			CharBuffer cb = CharBuffer.wrap(cbuf);
			decoder.decode(buf, cb, false);
			buf.clear();
			cb.flip();

			return cb.remaining();
		}

		// Below are alternative read methods which can be used if above method
		// will be causing performance issues
		// public int read3(char[] cbuf) throws IOException {
		// byte[] arr = new byte[2048];
		// int read = inputStream.read(arr, 0, arr.length);
		//
		// CharBuffer cb = CharBuffer.wrap(cbuf);
		// decoder.decode(ByteBuffer.wrap(arr, 0, read), cb, false);
		// cb.flip();
		//
		// return cb.remaining();
		// }
		//
		// public int read2(char[] cbuf) throws IOException {
		// byte[] arr = new byte[2048];
		// int read = inputStream.read(arr, 0, arr.length);
		//
		// CharBuffer cb = CharBuffer.allocate(2048);
		// decoder.decode(ByteBuffer.wrap(arr, 0, read), cb, false);
		// cb.flip();
		//
		// int got = cb.remaining();
		// cb.get(cbuf, 0, got);
		//
		// return got;
		// }

	}

	public static class SocketConnectorEvent extends ConnectorEvent {

		private static final long serialVersionUID = 1L;

		public SocketConnectorEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

	}

	private class Worker extends Thread {

		private final char[] buffer = new char[DEFAULT_SOCKET_BUFFER_SIZE];

		private SocketConnector connector;

		private final XMPPDomBuilderHandler domHandler = new XMPPDomBuilderHandler(new StreamListener() {

			@Override
			public void nextElement(tigase.xml.Element element) {
				try {
					processElement(element);
				} catch (JaxmppException e) {
					log.log(Level.SEVERE, "Error on processing element", e);
				}
			}

			@Override
			public void xmppStreamClosed() {
				try {
					if (log.isLoggable(Level.FINEST))
						log.finest("xmppStreamClosed()");
					SocketConnector.this.onStreamTerminate();
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void xmppStreamOpened(Map<String, String> attribs) {
				if (log.isLoggable(Level.FINEST))
					log.finest("xmppStreamOpened()");
				SocketConnector.this.onStreamStart(attribs);
			}
		});

		private final SimpleParser parser = SingletonFactory.getParserInstance();

		public Worker(SocketConnector connector) {
			this.connector = connector;
		}

		@Override
		public void interrupt() {
			super.interrupt();
			log.fine("Worker Interrupted");
		}

		@Override
		public void run() {
			super.run();
			log.finest(hashCode() + " Starting " + this);

			int r = -2;
			try {
				while (connector.reader != null && !isInterrupted() && (r = connector.reader.read(buffer)) != -1
						&& connector.getState() != Connector.State.disconnected) {
					parser.parse(domHandler, buffer, 0, r);
				}
				// if (log.isLoggable(Level.FINEST))
				log.finest(hashCode() + "Disconnecting: state=" + connector.getState() + "; buffer=" + r + "   " + this);
				if (!isInterrupted())
					connector.onStreamTerminate();
			} catch (Exception e) {
				if (SocketConnector.this.getState() != Connector.State.disconnecting
						&& SocketConnector.this.getState() != Connector.State.disconnected) {
					log.log(Level.WARNING, "Exception in worker", e);
					try {
						onErrorInThread(e);
					} catch (JaxmppException e1) {
						e1.printStackTrace();
					}
				}
			} finally {
				interrupt();
				log.finest("Worker2 is interrupted");
				connector.workerTerminated(this);
			}
		}
	}

	public final static String COMPRESSION_DISABLED_KEY = "COMPRESSION_DISABLED";

	/**
	 * Default size of buffer used to decode data before parsing
	 */
	private final static int DEFAULT_SOCKET_BUFFER_SIZE = 2048;

	/**
	 * Instance of empty byte array used to force flush of compressed stream
	 */
	private final static byte[] EMPTY_BYTEARRAY = new byte[0];

	/**
	 * see-other-host
	 */
	public final static EventType HostChanged = new EventType();

	public static final String KEY_MANAGERS_KEY = "KEY_MANAGERS_KEY";

	private final static String RECONNECTING_KEY = "s:reconnecting";

	public static final String SASL_EXTERNAL_ENABLED_KEY = "SASL_EXTERNAL_ENABLED_KEY";

	public static final String SERVER_HOST = "socket#ServerHost";

	public static final String SERVER_PORT = "socket#ServerPort";

	public static final String SSL_SOCKET_FACTORY_KEY = "socket#SSLSocketFactory";

	public static final String TLS_DISABLED_KEY = "TLS_DISABLED";

	public static boolean isTLSAvailable(SessionObject sessionObject) throws XMLException {
		final Element sf = sessionObject.getStreamFeatures();
		if (sf == null)
			return false;
		Element m = sf.getChildrenNS("starttls", "urn:ietf:params:xml:ns:xmpp-tls");
		return m != null;
	}

	/**
	 * Returns true if server send stream features in which it advertises
	 * support for stream compression using ZLIB
	 * 
	 * @param sessionObject
	 * @return
	 * @throws XMLException
	 */
	public static boolean isZLibAvailable(SessionObject sessionObject) throws XMLException {
		final Element sf = sessionObject.getStreamFeatures();
		if (sf == null)
			return false;
		Element m = sf.getChildrenNS("compression", "http://jabber.org/features/compress");
		if (m == null)
			return false;

		for (Element method : m.getChildren("method")) {
			if ("zlib".equals(method.getValue()))
				return true;
		}

		return false;
	}

	private final TrustManager dummyTrustManager = new X509TrustManager() {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};

	private final Object ioMutex = new Object();

	private final Logger log;

	protected Observable observable;

	private TimerTask pingTask;

	private boolean preventAgainstFireErrors = false;

	private volatile Reader reader;

	private SessionObject sessionObject;

	private Socket socket;

	/**
	 * Socket timeout.
	 */
	private int SOCKET_TIMEOUT = 1000 * 60 * 3;

	private Timer timer;

	private Worker worker;

	private OutputStream writer;

	public SocketConnector(Observable parentObservable, SessionObject sessionObject2) {
		this.observable = ObservableFactory.instance(parentObservable);
		this.log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject2;
	}

	@Override
	public void addListener(EventType eventType, Listener<? extends ConnectorEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		if (sessionObject.getProperty(InBandRegistrationModule.IN_BAND_REGISTRATION_MODE_KEY) == Boolean.TRUE) {
			log.info("Using XEP-0077 mode!!!!");
			return new SocketInBandRegistrationXmppSessionLogic(this, modulesManager, sessionObject, writer);
		} else
			return new SocketXmppSessionLogic(this, modulesManager, sessionObject, writer);
	}

	protected void fireOnConnected(SessionObject sessionObject) throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		ConnectorEvent event = new SocketConnectorEvent(Connected, sessionObject);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnError(Element response, Throwable caught, SessionObject sessionObject) throws JaxmppException {
		ConnectorEvent event = new SocketConnectorEvent(Error, sessionObject);
		event.setStanza(response);
		event.setCaught(caught);

		if (response != null) {
			List<Element> es = response.getChildrenNS("urn:ietf:params:xml:ns:xmpp-streams");
			if (es != null)
				for (Element element : es) {
					String n = element.getName();
					StreamError err = StreamError.getByElementName(n);
					event.setStreamError(err);
					event.setStreamErrorElement(element);
				}
		}

		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnStanzaReceived(Element response, SessionObject sessionObject) throws JaxmppException {
		ConnectorEvent event = new SocketConnectorEvent(StanzaReceived, sessionObject);
		event.setStanza(response);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnTerminate(SessionObject sessionObject) throws JaxmppException {
		ConnectorEvent event = new SocketConnectorEvent(StreamTerminated, sessionObject);
		this.observable.fireEvent(event.getType(), event);
	}

	private Entry getHostFromSessionObject() {
		String serverHost = (String) sessionObject.getProperty(SERVER_HOST);
		Integer port = (Integer) sessionObject.getProperty(SERVER_PORT);
		if (serverHost == null)
			return null;
		return new Entry(serverHost, port == null ? 5222 : port);

	}

	protected KeyManager[] getKeyManagers() throws NoSuchAlgorithmException {
		KeyManager[] result = sessionObject.getProperty(KEY_MANAGERS_KEY);
		return result == null ? new KeyManager[0] : result;
	}

	@Override
	public Observable getObservable() {
		return observable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public State getState() {
		State st = this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
		return st == null ? State.disconnected : st;
	}

	/**
	 * Returns true when stream is compressed
	 * 
	 * @return
	 */
	@Override
	public boolean isCompressed() {
		return ((Boolean) sessionObject.getProperty(COMPRESSED_KEY)) == Boolean.TRUE;
	}

	@Override
	public boolean isSecure() {
		return ((Boolean) sessionObject.getProperty(ENCRYPTED_KEY)) == Boolean.TRUE;
	}

	@Override
	public void keepalive() throws JaxmppException {
		if (sessionObject.getProperty(DISABLE_KEEPALIVE_KEY) == Boolean.TRUE)
			return;
		if (getState() == State.connected)
			send(new byte[] { 32 });
	}

	protected void onError(Element response, Throwable caught) throws JaxmppException {
		if (response != null) {
			Element seeOtherHost = response.getChildrenNS("see-other-host", "urn:ietf:params:xml:ns:xmpp-streams");
			if (seeOtherHost != null) {
				if (log.isLoggable(Level.FINE))
					log.fine("Received see-other-host=" + seeOtherHost.getValue());
				preventAgainstFireErrors = true;
				reconnect(seeOtherHost.getValue());
				return;
			}
		}
		stop();
		fireOnError(response, caught, sessionObject);
	}

	protected void onErrorInThread(Exception e) throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		stop();
		fireOnError(null, e, sessionObject);
	}

	protected void onResponse(final Element response) throws JaxmppException {
		synchronized (ioMutex) {
			if ("error".equals(response.getName()) && response.getXMLNS() != null
					&& response.getXMLNS().equals("http://etherx.jabber.org/streams")) {
				onError(response, null);
			} else {
				fireOnStanzaReceived(response, sessionObject);
			}
		}
	}

	protected void onStreamStart(Map<String, String> attribs) {
		// TODO Auto-generated method stub
	}

	protected void onStreamTerminate() throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		setStage(State.disconnected);

		if (log.isLoggable(Level.FINE))
			log.fine("Stream terminated");

		terminateAllWorkers();
		fireOnTerminate(sessionObject);

	}

	public void onTLSStanza(tigase.xml.Element elem) throws JaxmppException {
		if (elem.getName().equals("proceed")) {
			proceedTLS();
		} else if (elem.getName().equals("failure")) {
			log.info("TLS Failure");
		}
	}

	/**
	 * Handles result of requesting stream compression
	 * 
	 * @param elem
	 * @throws JaxmppException
	 */
	public void onZLibStanza(tigase.xml.Element elem) throws JaxmppException {
		if (elem.getName().equals("compressed") && "http://jabber.org/protocol/compress".equals(elem.getXMLNS())) {
			proceedZLib();
		} else if (elem.getName().equals("failure")) {
			log.info("ZLIB Failure");
		}
	}

	protected void proceedTLS() throws JaxmppException {
		log.fine("Proceeding TLS");
		try {
			sessionObject.setProperty(Scope.stream, DISABLE_KEEPALIVE_KEY, Boolean.TRUE);
			TrustManager[] trustManagers = sessionObject.getProperty(TRUST_MANAGERS_KEY);
			final SSLSocketFactory factory;
			if (trustManagers == null) {
				if (sessionObject.getProperty(SSL_SOCKET_FACTORY_KEY) != null) {
					factory = sessionObject.getProperty(SSL_SOCKET_FACTORY_KEY);
				} else {
					factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				}
			} else {
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(getKeyManagers(), trustManagers, new SecureRandom());
				factory = ctx.getSocketFactory();
			}

			SSLSocket s1 = (SSLSocket) factory.createSocket(socket, socket.getInetAddress().getHostAddress(), socket.getPort(),
					true);

			// if (sessionObject.getProperty(DISABLE_SOCKET_TIMEOUT_KEY) == null
			// || !((Boolean)
			// sessionObject.getProperty(DISABLE_SOCKET_TIMEOUT_KEY)).booleanValue())
			// {
			// s1.setSoTimeout(SOCKET_TIMEOUT);
			// }
			s1.setSoTimeout(0);
			s1.setKeepAlive(false);
			s1.setTcpNoDelay(true);
			s1.setUseClientMode(true);
			s1.addHandshakeCompletedListener(new HandshakeCompletedListener() {

				@Override
				public void handshakeCompleted(HandshakeCompletedEvent arg0) {
					log.info("TLS completed " + arg0);
					sessionObject.setProperty(Scope.stream, ENCRYPTED_KEY, Boolean.TRUE);
					ConnectorEvent event = new SocketConnectorEvent(EncryptionEstablished, sessionObject);
					try {
						observable.fireEvent(EncryptionEstablished, event);
					} catch (JaxmppException e) {
						throw new RuntimeException(e);
					}
				}
			});
			writer = null;
			reader = null;
			log.fine("Start handshake");
			s1.startHandshake();
			socket = s1;
			writer = socket.getOutputStream();
			reader = new Reader(socket.getInputStream());
			restartStream();
		} catch (javax.net.ssl.SSLHandshakeException e) {
			log.log(Level.SEVERE, "Can't establish encrypted connection", e);
			onError(null, e);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Can't establish encrypted connection", e);
			onError(null, e);
		} finally {
			sessionObject.setProperty(Scope.stream, DISABLE_KEEPALIVE_KEY, Boolean.FALSE);
		}
	}

	/**
	 * Method activates stream compression by replacing reader and writer fields
	 * values and restarting XMPP stream
	 * 
	 * @throws JaxmppException
	 */
	protected void proceedZLib() throws JaxmppException {
		log.fine("Proceeding ZLIB");
		try {
			sessionObject.setProperty(Scope.stream, DISABLE_KEEPALIVE_KEY, Boolean.TRUE);

			writer = null;
			reader = null;
			log.fine("Start ZLIB compression");

			Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, false);
			try {
				// on Android platform Deflater has field named flushParm which
				// can force flushing data to socket for us
				Field f = compressor.getClass().getDeclaredField("flushParm");
				if (f != null) {
					f.setAccessible(true);
					f.setInt(compressor, 2); // Z_SYNC_FLUSH
					writer = new DeflaterOutputStream(socket.getOutputStream(), compressor);
				}
			} catch (NoSuchFieldException ex) {
				writer = new DeflaterOutputStream(socket.getOutputStream(), compressor) {
					@Override
					public void write(byte[] data) throws IOException {
						super.write(data);
						super.write(EMPTY_BYTEARRAY);
						super.def.setLevel(Deflater.NO_COMPRESSION);
						super.deflate();
						super.def.setLevel(Deflater.BEST_COMPRESSION);
						super.deflate();
					}
				};
			}

			Inflater decompressor = new Inflater(false);
			final InflaterInputStream is = new InflaterInputStream(socket.getInputStream(), decompressor);
			reader = new Reader(is);

			sessionObject.setProperty(Scope.stream, Connector.COMPRESSED_KEY, true);
			log.info("ZLIB compression started");

			restartStream();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Can't establish compressed connection", e);
			onError(null, e);
		} finally {
			sessionObject.setProperty(Scope.stream, DISABLE_KEEPALIVE_KEY, Boolean.FALSE);
		}
	}

	public void processElement(tigase.xml.Element elem) throws JaxmppException {
		if (log.isLoggable(Level.FINEST))
			log.finest("RECV: " + elem.toString());
		if (elem != null && elem.getXMLNS() != null && elem.getXMLNS().equals("urn:ietf:params:xml:ns:xmpp-tls")) {
			onTLSStanza(elem);
		} else if (elem != null && elem.getXMLNS() != null && "http://jabber.org/protocol/compress".equals(elem.getXMLNS())) {
			onZLibStanza(elem);
		} else
			try {
				onResponse(new J2seElement(elem));
			} catch (JaxmppException e) {
				onErrorInThread(e);
			}
	}

	private void reconnect(final String newHost) {
		log.info("See other host: " + newHost);
		try {
			terminateAllWorkers();

			Object x1 = this.sessionObject.getProperty(Jaxmpp.SYNCHRONIZED_MODE);

			this.sessionObject.clear();
			this.sessionObject.setProperty(SERVER_HOST, newHost);
			worker = null;
			reader = null;
			writer = null;

			this.sessionObject.setProperty(RECONNECTING_KEY, Boolean.TRUE);
			this.sessionObject.setProperty(Jaxmpp.SYNCHRONIZED_MODE, x1);

			log.finest("Waiting for workers termination");

			// start();
		} catch (JaxmppException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	@Override
	public void removeListener(EventType eventType, Listener<ConnectorEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	@Override
	public void restartStream() throws XMLException, JaxmppException {
		StringBuilder sb = new StringBuilder();
		sb.append("<stream:stream ");

		final BareJID from = sessionObject.getProperty(SessionObject.USER_BARE_JID);
		String to;
		Boolean seeOtherHost = sessionObject.getProperty(SEE_OTHER_HOST_KEY);
		if (from != null && (seeOtherHost == null || seeOtherHost)) {
			to = from.getDomain();
			sb.append("from='").append(from.toString()).append("' ");
		} else {
			to = sessionObject.getProperty(SessionObject.DOMAIN_NAME);
		}

		if (to != null) {
			sb.append("to='").append(to).append("' ");
		}

		sb.append("xmlns='jabber:client' ");
		sb.append("xmlns:stream='http://etherx.jabber.org/streams' ");
		sb.append("version='1.0'>");

		if (log.isLoggable(Level.FINEST))
			log.finest("Restarting XMPP Stream");
		send(sb.toString().getBytes());
	}

	public void send(byte[] buffer) throws JaxmppException {
		synchronized (ioMutex) {
			if (writer != null)
				try {
					if (log.isLoggable(Level.FINEST))
						log.finest("Send: " + new String(buffer));
					writer.write(buffer);
				} catch (IOException e) {
					throw new JaxmppException(e);

				}
		}
	}

	@Override
	public void send(Element stanza) throws XMLException, JaxmppException {
		synchronized (ioMutex) {
			if (writer != null)
				try {
					String t = stanza.getAsString();
					if (log.isLoggable(Level.FINEST))
						log.finest("Send: " + t);

					try {
						SocketConnectorEvent event = new SocketConnectorEvent(StanzaSending, sessionObject);
						event.setStanza(stanza);
						observable.fireEvent(event);
					} catch (Exception e) {
					}
					writer.write(t.getBytes());
				} catch (IOException e) {
					this.stop(true);
					throw new JaxmppException(e);
				}
		}
		try {
			Thread.sleep((2));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setObservable(Observable observable) {
		if (observable == null)
			this.observable = ObservableFactory.instance(null);
		else
			this.observable = observable;
	}

	protected void setStage(State state) throws JaxmppException {
		State s = this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
		this.sessionObject.setProperty(Scope.stream, CONNECTOR_STAGE_KEY, state);
		if (s != state) {
			log.fine("Connector state changed: " + s + "->" + state);
			ConnectorEvent e = new SocketConnectorEvent(StateChanged, sessionObject);
			observable.fireEvent(e);
			if (!preventAgainstFireErrors && state == State.disconnected) {
				fireOnTerminate(sessionObject);
			}
		}
	}

	@Override
	public void start() throws XMLException, JaxmppException {
		preventAgainstFireErrors = false;
		log.fine("Start connector.");
		if (timer != null) {
			try {
				timer.cancel();
			} catch (Exception e) {
			}
		}
		timer = new Timer(true);

		if (sessionObject.getProperty(TRUST_MANAGERS_KEY) == null)
			sessionObject.setProperty(TRUST_MANAGERS_KEY, new TrustManager[] { dummyTrustManager });

		setStage(State.connecting);

		try {
			Entry serverHost = getHostFromSessionObject();
			if (serverHost == null) {
				String x = sessionObject.getProperty(SessionObject.DOMAIN_NAME);
				log.info("Resolving SRV recrd of domain '" + x + "'");
				List<Entry> xx;
				DnsResolver dnsResolver = UniversalFactory.createInstance(DnsResolver.class.getName());
				if (dnsResolver != null) {
					xx = dnsResolver.resolve(x);
				} else {
					xx = DNSResolver.resolve(x);
				}

				if (xx.size() > 0) {
					serverHost = xx.get(0);
				}
			}

			sessionObject.setProperty(Scope.stream, DISABLE_KEEPALIVE_KEY, Boolean.FALSE);

			if (log.isLoggable(Level.FINER))
				log.finer("Preparing connection to " + serverHost);

			InetAddress x = InetAddress.getByName(serverHost.getHostname());
			if (log.isLoggable(Level.FINEST))
				log.finest("Starting socket " + x + ":" + serverHost.getPort());
			socket = new Socket(x, serverHost.getPort());
			// if (sessionObject.getProperty(DISABLE_SOCKET_TIMEOUT_KEY) == null
			// || ((Boolean)
			// sessionObject.getProperty(DISABLE_SOCKET_TIMEOUT_KEY)).booleanValue())
			// {
			// socket.setSoTimeout(SOCKET_TIMEOUT);
			// }
			socket.setSoTimeout(SOCKET_TIMEOUT);
			socket.setKeepAlive(false);
			socket.setTcpNoDelay(true);
			// writer = new BufferedOutputStream(socket.getOutputStream());
			writer = socket.getOutputStream();
			reader = new Reader(socket.getInputStream());
			worker = new Worker(this);
			log.finest("Starting worker...");
			worker.start();

			restartStream();

			setStage(State.connected);

			this.pingTask = new TimerTask() {

				@Override
				public void run() {
					new Thread() {
						@Override
						public void run() {
							try {
								keepalive();
							} catch (JaxmppException e) {
								log.log(Level.SEVERE, "Can't ping!", e);
							}
						}
					}.start();
				}
			};
			long delay = SOCKET_TIMEOUT - 1000 * 5;

			if (log.isLoggable(Level.CONFIG))
				log.config("Whitespace ping period is setted to " + delay + "ms");

			if (sessionObject.getProperty(EXTERNAL_KEEPALIVE_KEY) == null
					|| ((Boolean) sessionObject.getProperty(EXTERNAL_KEEPALIVE_KEY) == false)) {
				timer.schedule(pingTask, delay, delay);
			}

			fireOnConnected(sessionObject);
		} catch (Exception e) {
			stop();
			onError(null, e);
			throw new JaxmppException(e);
		}
	}

	public void startTLS() throws JaxmppException {
		if (writer != null)
			try {
				log.fine("Start TLS");
				DefaultElement e = new DefaultElement("starttls", null, "urn:ietf:params:xml:ns:xmpp-tls");
				send(e.getAsString().getBytes());
			} catch (Exception e) {
				throw new JaxmppException(e);
			}
	}

	/**
	 * Sends <compress/> stanza to start stream compression using ZLIB
	 * 
	 * @throws JaxmppException
	 */
	public void startZLib() throws JaxmppException {
		if (writer != null)
			try {
				log.fine("Start ZLIB");
				DefaultElement e = new DefaultElement("compress", null, "http://jabber.org/protocol/compress");
				e.addChild(new DefaultElement("method", "zlib", null));
				send(e.getAsString().getBytes());
			} catch (Exception e) {
				throw new JaxmppException(e);
			}
	}

	@Override
	public void stop() throws JaxmppException {
		stop(false);
	}

	@Override
	public void stop(boolean terminate) throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		setStage(State.disconnecting);
		if (!terminate)
			terminateStream();
		terminateAllWorkers();
	}

	private void terminateAllWorkers() throws JaxmppException {
		log.finest("Terminating all workers");
		if (this.pingTask != null) {
			this.pingTask.cancel();
			this.pingTask = null;
		}
		setStage(State.disconnected);
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			log.log(Level.FINEST, "Problem with closing socket", e);
		}
		try {
			if (worker != null)
				worker.interrupt();
		} catch (Exception e) {
			log.log(Level.FINEST, "Problem with interrupting w2", e);
		}
		try {
			if (timer != null)
				timer.cancel();
		} catch (Exception e) {
			log.log(Level.FINEST, "Problem with canceling timer", e);
		} finally {
			timer = null;
		}
	}

	private void terminateStream() throws JaxmppException {
		final State state = getState();
		if (state == State.connected || state == State.connecting) {
			String x = "</stream:stream>";
			log.fine("Terminating XMPP Stream");
			send(x.getBytes());
		} else
			log.fine("Stream terminate not sent, because of connection state==" + state);
	}

	private void workerTerminated(final Worker worker) {
		try {
			setStage(State.disconnected);
		} catch (JaxmppException e) {
		}
		log.finest("Worker terminated");
		try {
			if (this.sessionObject.getProperty(RECONNECTING_KEY) == Boolean.TRUE) {
				this.sessionObject.setProperty(RECONNECTING_KEY, null);
				SocketConnectorEvent event = new SocketConnectorEvent(HostChanged, sessionObject);
				observable.fireEvent(HostChanged, event);
				log.finest("Restarting...");
				start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
