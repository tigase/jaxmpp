package tigase.jaxmpp.j2se.connectors.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Queue;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

/**
 * 
 */
public class SocketConnector implements Connector {

	private class Worker2 extends Thread {

		private final char[] buffer = new char[10240];

		private SocketConnector connector;

		public Worker2(SocketConnector connector) {
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
			int r = -2;
			try {
				while (!isInterrupted() && (r = connector.reader.read(buffer)) != -1
						&& connector.getState() != Connector.State.disconnected) {
					connector.parser.parse(connector.domHandler, buffer, 0, r);

					Queue<tigase.xml.Element> elems = domHandler.getParsedElements();
					tigase.xml.Element elem;
					while ((elem = elems.poll()) != null) {
						if (elem != null && elem.getXMLNS() != null
								&& elem.getXMLNS().equals("urn:ietf:params:xml:ns:xmpp-tls")) {
							connector.onTLSStanza(elem);
						} else
							try {
								connector.onResponse(new J2seElement(elem));
							} catch (JaxmppException e) {
								onErrorInThread(e);
							}
					}
				}
				if (log.isLoggable(LogLevel.FINEST))
					log.finest("Disconnecting: state=" + connector.getState() + "; buffer=" + r);
				connector.onStreamTerminate();
			} catch (Exception e) {
				try {
					onErrorInThread(e);
				} catch (JaxmppException e1) {
					e1.printStackTrace();
				}
			}
			interrupt();
			log.finest("Worker2 is interrupted");

		}
	}

	public static final String SERVER_HOST = "socket#ServerHost";

	public static final String SERVER_PORT = "socket#ServerPort";

	public static boolean isTLSAvailable(SessionObject sessionObject) throws XMLException {
		final Element sf = sessionObject.getStreamFeatures();
		if (sf == null)
			return false;
		Element m = sf.getChildrenNS("starttls", "urn:ietf:params:xml:ns:xmpp-tls");
		return m != null;
	}

	private final XMPPDomBuilderHandler domHandler = new XMPPDomBuilderHandler(new StreamListener() {

		@Override
		public void xmppStreamClosed() {
			try {
				if (log.isLoggable(LogLevel.FINEST))
					log.finest("xmppStreamClosed()");
				SocketConnector.this.onStreamTerminate();
			} catch (JaxmppException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void xmppStreamOpened(Map<String, String> attribs) {
			SocketConnector.this.onStreamStart(attribs);
		}
	});

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

	private final Logger log;

	protected Observable observable;

	private final SimpleParser parser = SingletonFactory.getParserInstance();

	private Reader reader;

	private Socket s;

	private SessionObject sessionObject;

	private Worker2 w2;

	private OutputStream writer;

	public SocketConnector(Observable parentObservable, SessionObject sessionObject2) {
		this.observable = new Observable(parentObservable);
		this.log = LoggerFactory.getLogger(this.getClass());
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
		return new SocketXmppSessionLogic(this, modulesManager, sessionObject, writer);
	}

	protected void fireOnConnected(SessionObject sessionObject) throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		ConnectorEvent event = new ConnectorEvent(Connected);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnError(Element response, Throwable caught, SessionObject sessionObject) throws JaxmppException {
		ConnectorEvent event = new ConnectorEvent(Error);
		event.setStanza(response);
		event.setCaught(caught);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnStanzaReceived(Element response, SessionObject sessionObject) throws JaxmppException {
		ConnectorEvent event = new ConnectorEvent(StanzaReceived);
		event.setStanza(response);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnTerminate(SessionObject sessionObject) throws JaxmppException {
		ConnectorEvent event = new ConnectorEvent(StreamTerminated);
		this.observable.fireEvent(event.getType(), event);
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
		return this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
	}

	@Override
	public boolean isSecure() {
		return ((Boolean) sessionObject.getProperty(ENCRYPTED_KEY)) == Boolean.TRUE;
	}

	protected void onError(Element response, Throwable caught) throws JaxmppException {
		if (response != null)
			sessionObject.setProperty(CONNECTOR_STAGE_KEY, State.disconnected);
		fireOnError(response, caught, sessionObject);
	}

	protected void onErrorInThread(Exception e) throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		fireOnError(null, e, sessionObject);
	}

	protected void onResponse(final Element response) throws JaxmppException {
		fireOnStanzaReceived(response, sessionObject);
	}

	protected void onStreamStart(Map<String, String> attribs) {
		// TODO Auto-generated method stub
	}

	protected void onStreamTerminate() throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		setStage(State.disconnected);

		if (log.isLoggable(LogLevel.FINE))
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

	protected void proceedTLS() throws JaxmppException {
		log.fine("Proceeding TLS");
		try {
			TrustManager trustManager = sessionObject.getProperty(TRUST_MANAGER_KEY);
			final SSLSocketFactory factory;
			if (trustManager == null) {
				factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			} else {
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(new KeyManager[0], new TrustManager[] { trustManager }, new SecureRandom());
				factory = ctx.getSocketFactory();
			}

			SSLSocket s1 = (SSLSocket) factory.createSocket(s, s.getInetAddress().getHostAddress(), s.getPort(), true);
			s1.setUseClientMode(true);
			s1.addHandshakeCompletedListener(new HandshakeCompletedListener() {

				@Override
				public void handshakeCompleted(HandshakeCompletedEvent arg0) {
					log.info("TLS completed " + arg0);
					sessionObject.setProperty(ENCRYPTED_KEY, Boolean.TRUE);
					ConnectorEvent event = new ConnectorEvent(EncryptionEstablished);
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
			s = s1;
			writer = s.getOutputStream();
			reader = new InputStreamReader(s.getInputStream());
			restartStream();
		} catch (javax.net.ssl.SSLHandshakeException e) {
			log.log(LogLevel.SEVERE, "Can't establish encrypted connection", e);
			onError(null, e);
		} catch (Exception e) {
			log.log(LogLevel.SEVERE, "Can't establish encrypted connection", e);
			// TODO Auto-generated catch block
			onError(null, e);
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
		sb.append("to='").append((String) sessionObject.getProperty(SessionObject.SERVER_NAME)).append("' ");
		sb.append("xmlns='jabber:client' ");
		sb.append("xmlns:stream='http://etherx.jabber.org/streams' ");
		sb.append("version='1.0'>");

		if (writer != null)
			try {
				if (log.isLoggable(LogLevel.FINEST))
					log.finest("Restarting XMPP Stream");
				writer.write(sb.toString().getBytes());
			} catch (IOException e) {
				throw new JaxmppException(e);
			}
	}

	@Override
	public void send(Element stanza) throws XMLException, JaxmppException {
		if (writer != null)
			try {
				String t = stanza.getAsString();
				if (log.isLoggable(LogLevel.FINEST))
					log.finest("SEND: " + t);
				writer.write(t.getBytes());
			} catch (IOException e) {
				throw new JaxmppException(e);
			}
	}

	@Override
	public void setObservable(Observable observable) {
		if (observable == null)
			this.observable = new Observable(null);
		else
			this.observable = observable;
	}

	protected void setStage(State state) throws JaxmppException {
		State s = this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
		this.sessionObject.setProperty(CONNECTOR_STAGE_KEY, state);
		if (s != state) {
			ConnectorEvent e = new ConnectorEvent(StateChanged);
			observable.fireEvent(e);
		}
	}

	@Override
	public void start() throws XMLException, JaxmppException {
		if (sessionObject.getProperty(SERVER_HOST) == null)
			throw new JaxmppException("No Server Hostname specified");

		if (sessionObject.getProperty(SessionObject.USER_JID) == null)
			throw new JaxmppException("No user JID specified");

		if (sessionObject.getProperty(SessionObject.SERVER_NAME) == null)
			sessionObject.setProperty(SessionObject.SERVER_NAME,
					((JID) sessionObject.getProperty(SessionObject.USER_JID)).getDomain());

		if (sessionObject.getProperty(TRUST_MANAGER_KEY) == null)
			sessionObject.setProperty(TRUST_MANAGER_KEY, dummyTrustManager);

		setStage(State.connecting);

		try {
			Integer port = (Integer) sessionObject.getProperty(SERVER_PORT);
			port = port == null ? 5222 : port;

			s = SocketFactory.getDefault().createSocket((String) sessionObject.getProperty(SERVER_HOST), port);
			writer = s.getOutputStream();
			reader = new InputStreamReader(s.getInputStream());
			w2 = new Worker2(this);
			w2.start();

			restartStream();

			setStage(State.connected);
			fireOnConnected(sessionObject);
		} catch (Exception e) {
			throw new JaxmppException(e);
		}
	}

	public void startTLS() throws JaxmppException {
		if (writer != null)
			try {
				log.fine("Start TLS");
				DefaultElement e = new DefaultElement("starttls", null, "urn:ietf:params:xml:ns:xmpp-tls");
				writer.write(e.getAsString().getBytes());
			} catch (Exception e) {
				throw new JaxmppException(e);
			}
	}

	@Override
	public void stop() throws JaxmppException {
		setStage(State.disconnecting);
		terminateStream();
		terminateAllWorkers();
	}

	private void terminateAllWorkers() throws JaxmppException {
		log.finest("Terminating all workers");
		setStage(State.disconnected);
		try {
			s.close();
		} catch (IOException e) {
			log.log(LogLevel.FINEST, "Problem with closing socket", e);
		}
		try {
			w2.interrupt();
		} catch (Exception e) {
			log.log(LogLevel.FINEST, "Problem with interrupting w2", e);
		}
	}

	private void terminateStream() throws JaxmppException {
		if (writer != null)
			try {
				String x = "</stream:stream>";
				log.fine("Terminating XMPP Stream");
				writer.write(x.getBytes());
			} catch (IOException e) {
				throw new JaxmppException(e);
			}
	}

}
