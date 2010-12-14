package tigase.jaxmpp.core.client.connector;

import java.util.HashMap;
import java.util.Map;

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
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public abstract class AbstractBoshConnector implements Connector {

	public static class BoshConnectorEvent extends ConnectorEvent {

		private static final long serialVersionUID = 1L;
		private Element responseBody;
		private int responseCode;

		public BoshConnectorEvent(EventType type) {
			super(type);
		}

		public Element getResponseBody() {
			return responseBody;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseBody(Element response) {
			this.responseBody = response;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}
	}

	public static final String BOSH_SERVICE_URL = "boshServiceUrl";

	private static final String DEFAULT_TIMEOUT_KEY = "bosh#defaultTimeout";

	public final static String RID_KEY = "bosh#rid";

	public static final String SID_KEY = "bosh#sid";

	protected final Logger log;

	protected final Observable observable = new Observable();

	protected final Map<String, BoshRequest> requests = new HashMap<String, BoshRequest>();

	protected final SessionObject sessionObject;

	public AbstractBoshConnector(SessionObject sessionObject) {
		this.log = LoggerFactory.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
	}

	@Override
	public void addListener(EventType eventType, Listener<ConnectorEvent> listener) {
		observable.addListener(eventType, listener);
	}

	protected void addToRequests(final BoshRequest worker) {
		this.requests.put(worker.getRid(), worker);
	}

	protected int countActiveRequests() {
		return this.requests.size();
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return new BoshXmppSessionLogic(this, modulesManager, sessionObject, writer);
	}

	protected void fireOnConnected(SessionObject sessionObject) {
		BoshConnectorEvent event = new BoshConnectorEvent(Connected);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnError(int responseCode, Element response, Throwable caught, SessionObject sessionObject) {
		BoshConnectorEvent event = new BoshConnectorEvent(Error);
		event.setResponseCode(responseCode);
		event.setResponseBody(response);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnStanzaReceived(int responseCode, Element response, SessionObject sessionObject) {
		try {
			BoshConnectorEvent event = new BoshConnectorEvent(StanzaReceived);
			event.setResponseBody(response);
			event.setResponseCode(responseCode);
			if (response != null) {
				Element ch = response.getFirstChild();
				event.setStanza(ch);
			}
			this.observable.fireEvent(event.getType(), event);
		} catch (XMLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireOnTerminate(int responseCode, Element response, SessionObject sessionObject) {
		BoshConnectorEvent event = new BoshConnectorEvent(StreamTerminated);
		event.setResponseCode(responseCode);
		event.setResponseBody(response);
		this.observable.fireEvent(event.getType(), event);
	}

	protected String getSid() {
		return this.sessionObject.getProperty(SID_KEY);
	}

	@Override
	public Stage getStage() {
		return this.sessionObject.getProperty(CONNECTOR_STAGE);
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	protected Long nextRid() {
		Long i = sessionObject.getProperty(RID_KEY);
		if (i == null) {
			i = (long) (Math.random() * 10000000);
		}
		i++;
		sessionObject.setProperty(RID_KEY, i);
		return i;
	}

	protected void onError(int responseCode, Element response, Throwable caught) {
		try {
			if (response != null)
				removeFromRequests(response.getAttribute("ack"));
			if (log.isLoggable(LogLevel.FINER))
				log.log(LogLevel.FINER, "responseCode=" + responseCode, caught);
			setStage(Stage.disconnected);
			fireOnError(responseCode, response, caught, sessionObject);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void onResponse(final int responseCode, final Element response) throws JaxmppException {
		try {
			if (response != null)
				removeFromRequests(response.getAttribute("ack"));
			if (getStage() == Stage.connecting) {
				setSid(response.getAttribute("sid"));
				setStage(Stage.connected);
				fireOnConnected(sessionObject);
			}
			if (getStage() == Stage.connected && countActiveRequests() == 0) {
				final Element body = prepareBody(null);
				processSendData(body);
			}
			fireOnStanzaReceived(responseCode, response, sessionObject);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void onTerminate(int responseCode, Element response) {
		try {
			if (log.isLoggable(LogLevel.FINE))
				log.fine("Stream terminated. responseCode=" + responseCode);
			if (response != null)
				removeFromRequests(response.getAttribute("ack"));
			setStage(Stage.disconnected);
			terminateAllWorkers();
			fireOnTerminate(responseCode, response, sessionObject);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected Element prepareBody(Element payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	protected Element prepareRetartBody() throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("to", (String) sessionObject.getProperty(SessionObject.SERVER_NAME));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:restart", "true");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");

		return e;
	}

	protected Element prepareStartBody() throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("content", "text/xml; charset=utf-8");
		// e.setAttribute("from", data.fromUser);
		e.setAttribute("hold", "1");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("to", (String) sessionObject.getProperty(SessionObject.SERVER_NAME));
		e.setAttribute("secure", "true");
		e.setAttribute("wait", (String) sessionObject.getProperty(DEFAULT_TIMEOUT_KEY));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:version", "1.0");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");

		return e;
	}

	protected Element prepareTerminateBody(Element payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("type", "terminate");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	protected abstract void processSendData(final Element element) throws XMLException, JaxmppException;

	@Override
	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	protected void removeFromRequests(final String ack) {
		if (ack == null)
			return;
		this.requests.remove(ack);
	}

	@Override
	public void removeListener(EventType eventType, Listener<ConnectorEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	@Override
	public void restartStream() throws XMLException, JaxmppException {
		if (getStage() != Stage.disconnected)
			processSendData(prepareRetartBody());
	}

	@Override
	public void send(final Element stanza) throws XMLException, JaxmppException {
		if (getStage() == Stage.connected) {
			if (stanza != null) {
				final Element body = prepareBody(stanza);
				processSendData(body);
			}
		} else
			throw new RuntimeException("Not connected");
	}

	protected void setSid(String sid) {
		this.sessionObject.setProperty(SID_KEY, sid);
	}

	protected void setStage(Stage stage) {
		Stage s = this.sessionObject.getProperty(CONNECTOR_STAGE);
		this.sessionObject.setProperty(CONNECTOR_STAGE, stage);
		if (s != stage) {
			ConnectorEvent e = new ConnectorEvent(StageChanged);
			observable.fireEvent(e);
		}
	}

	@Override
	public void start() throws XMLException, JaxmppException {
		if (sessionObject.getProperty(SessionObject.USER_JID) == null)
			throw new JaxmppException("No user JID specified");

		if (sessionObject.getProperty(SessionObject.SERVER_NAME) == null)
			sessionObject.setProperty(SessionObject.SERVER_NAME,
					((JID) sessionObject.getProperty(SessionObject.USER_JID)).getDomain());

		String u = sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL);
		if (u == null)
			throw new JaxmppException("BOSH service URL not defined!");

		setStage(Stage.connecting);
		processSendData(prepareStartBody());
	}

	@Override
	public void stop() throws XMLException, JaxmppException {
		if (getStage() != Stage.disconnected)
			processSendData(prepareTerminateBody(null));
	}

	protected void terminateAllWorkers() {
		for (BoshRequest w : this.requests.values()) {
			w.terminate();
		}
		this.requests.clear();
	}
}
