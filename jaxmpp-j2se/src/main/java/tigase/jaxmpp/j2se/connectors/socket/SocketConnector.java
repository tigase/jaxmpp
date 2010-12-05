package tigase.jaxmpp.j2se.connectors.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

public class SocketConnector implements Connector {

	static class Worker extends Thread {

		private SocketConnector socketConnector;

		public Worker(SocketConnector socketConnector) {
			this.socketConnector = socketConnector;
		}

		@Override
		public void run() {
			super.run();
			while (socketConnector.getStage() != Stage.disconnected) {
				Queue<tigase.xml.Element> elems = socketConnector.domHandler.getParsedElements();
				tigase.xml.Element elem;
				while ((elem = elems.poll()) != null) {
					System.out.println(" RECEIVED: " + elem.toString());
					try {
						socketConnector.onResponse(new J2seElement(elem));
					} catch (JaxmppException e) {
						e.printStackTrace();
					}
				}
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						// throw new RuntimeException(e);
					}
				}
			}
		}

		public void wakeUp() {
			synchronized (this) {
				notify();
			}
		}
	}

	private static class Worker2 extends Thread {

		private final char[] buffer = new char[10240];

		private SocketConnector connector;

		public Worker2(SocketConnector connector) {
			this.connector = connector;
		}

		@Override
		public void run() {
			super.run();
			int r;
			try {
				while ((r = connector.reader.read(buffer)) != -1 && connector.getStage() != Stage.disconnected) {
					connector.parser.parse(connector.domHandler, buffer, 0, r);
					connector.worker.wakeUp();
				}
				connector.onStreamTerminate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static final String SERVER_HOST = "socket#ServerHost";

	public static final String SERVER_PORT = "socket#ServerPort";

	private final XMPPDomBuilderHandler domHandler = new XMPPDomBuilderHandler(new StreamListener() {

		@Override
		public void xmppStreamClosed() {
			SocketConnector.this.onStreamTerminate();
		}

		@Override
		public void xmppStreamOpened(Map<String, String> attribs) {
			SocketConnector.this.onStreamStart(attribs);
		}
	});

	protected final Observable observable = new Observable();

	private final SimpleParser parser = SingletonFactory.getParserInstance();

	private Reader reader;

	private Socket s;

	private SessionObject sessionObject;

	private Worker2 w2;

	private Worker worker;

	private OutputStream writer;

	public SocketConnector(SessionObject sessionObject2) {
		this.sessionObject = sessionObject2;
	}

	@Override
	public void addListener(EventType eventType, Listener<ConnectorEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return new SocketXmppSessionLogic(this, modulesManager, sessionObject, writer);
	}

	protected void fireOnConnected(SessionObject sessionObject) {
		ConnectorEvent event = new ConnectorEvent(CONNECTED);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnError(Element response, Throwable caught, SessionObject sessionObject) {
		ConnectorEvent event = new ConnectorEvent(ERROR);
		event.setStanza(response);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnStanzaReceived(Element response, SessionObject sessionObject) {
		ConnectorEvent event = new ConnectorEvent(STANZA_RECEIVED);
		event.setStanza(response);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnTerminate(SessionObject sessionObject) {
		ConnectorEvent event = new ConnectorEvent(TERMINATE);
		this.observable.fireEvent(event.getType(), event);
	}

	protected Stage getStage() {
		return this.sessionObject.getProperty(CONNECTOR_STAGE);
	}

	protected void onError(Element response, Throwable caught) {
		if (response != null)
			sessionObject.setProperty(CONNECTOR_STAGE, Stage.disconnected);
		fireOnError(response, caught, sessionObject);
	}

	protected void onResponse(final Element response) throws JaxmppException {
		fireOnStanzaReceived(response, sessionObject);
	}

	protected void onStreamStart(Map<String, String> attribs) {
		// TODO Auto-generated method stub
		System.out.println(" STREAM STSRT " + attribs.toString());
	}

	protected void onStreamTerminate() {
		if (getStage() == Stage.disconnected)
			return;
		setStage(Stage.disconnected);

		System.out.println("onTerminate()");
		terminateAllWorkers();
		fireOnTerminate(sessionObject);

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

		try {
			System.out.println("S: " + sb.toString());
			writer.write(sb.toString().getBytes());
		} catch (IOException e) {
			throw new JaxmppException(e);
		}
	}

	@Override
	public void send(Element stanza) throws XMLException, JaxmppException {
		try {
			String t = stanza.getAsString();
			System.out.println("S: " + t);
			writer.write(t.getBytes());
		} catch (IOException e) {
			throw new JaxmppException(e);
		}
	}

	protected void setStage(Stage stage) {
		this.sessionObject.setProperty(CONNECTOR_STAGE, stage);
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

		setStage(Stage.connecting);
		this.worker = new Worker(this);
		worker.start();

		try {
			Integer port = (Integer) sessionObject.getProperty(SERVER_PORT);
			port = port == null ? 5222 : port;
			s = new Socket((String) sessionObject.getProperty(SERVER_HOST), port);
			writer = s.getOutputStream();
			reader = new InputStreamReader(s.getInputStream());
			w2 = new Worker2(this);
			w2.start();

			restartStream();

			setStage(Stage.connected);
			fireOnConnected(sessionObject);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	@Override
	public void stop() throws JaxmppException {
		terminateStream();
		// try {
		// s.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// this.worker.interrupt();
		// this.w2.interrupt();
	}

	private void terminateAllWorkers() {
		worker.interrupt();
		w2.interrupt();
		worker.wakeUp();
	}

	private void terminateStream() throws JaxmppException {
		try {
			String x = "</stream:stream>";
			System.out.println(x);
			writer.write(x.getBytes());
		} catch (IOException e) {
			throw new JaxmppException(e);
		}
	}

}
