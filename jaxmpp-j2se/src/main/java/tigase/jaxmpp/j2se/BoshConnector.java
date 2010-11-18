package tigase.jaxmpp.j2se;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class BoshConnector {

	public static final class BoshConnectorEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		private Element responseBody;

		private int responseCode;

		private Element stanza;

		public BoshConnectorEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Element getResponseBody() {
			return responseBody;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public Element getStanza() {
			return stanza;
		}
	}

	static class ConnectorData {

		int defaultTimeout = 3;

		String fromUser;

		long rid;

		String sid;

		Stage stage = Stage.disconnected;

		String toHost;

		URL url;

	}

	protected static enum Stage {
		connected,
		connecting,
		disconnected
	}

	public static final String BOSH_SERVICE_URL = "boshServiceUrl";

	public final static EventType CONNECTED = new EventType();

	public final static EventType ERROR = new EventType();

	public final static EventType STANZA_RECEIVED = new EventType();

	public final static EventType TERMINATE = new EventType();

	private final ConnectorData data = new ConnectorData();

	protected final Observable observable = new Observable();

	protected final Map<String, BoshWorker> requests = new HashMap<String, BoshWorker>();

	private SessionObject sessionObject;

	public void addListener(EventType eventType, Listener<BoshConnectorEvent> listener) {
		observable.addListener(eventType, listener);
	}

	protected void addToRequests(final BoshWorker worker) {
		this.requests.put(worker.getRid(), worker);
	}

	protected int countActiveRequests() {
		return this.requests.size();
	}

	protected void fireOnConnected(SessionObject sessionObject) {
		BoshConnectorEvent event = new BoshConnectorEvent(CONNECTED, sessionObject);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnError(int responseCode, Element response, Throwable caught, SessionObject sessionObject) {
		BoshConnectorEvent event = new BoshConnectorEvent(ERROR, sessionObject);
		event.responseCode = responseCode;
		event.responseBody = response;
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnStanzaReceived(int responseCode, Element response, SessionObject sessionObject) {
		try {
			BoshConnectorEvent event = new BoshConnectorEvent(STANZA_RECEIVED, sessionObject);
			event.responseBody = response;
			event.responseCode = responseCode;
			if (response != null) {
				Element ch = response.getFirstChild();
				event.stanza = ch;
			}
			this.observable.fireEvent(event.getType(), event);
		} catch (XMLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireOnTerminate(int responseCode, Element response, SessionObject sessionObject) {
		BoshConnectorEvent event = new BoshConnectorEvent(TERMINATE, sessionObject);
		event.responseCode = responseCode;
		event.responseBody = response;
		this.observable.fireEvent(event.getType(), event);
	}

	protected void onError(int responseCode, Element response, Throwable caught) {
		try {
			if (response != null)
				removeFromRequests(response.getAttribute("ack"));
			System.out.println("onError(): responseCode=" + responseCode + "; " + " " + caught);
			this.data.stage = Stage.disconnected;
			fireOnError(responseCode, response, caught, sessionObject);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void onResponse(final int responseCode, final Element response) {
		System.out.println("onResponse()");
		try {
			if (response != null)
				removeFromRequests(response.getAttribute("ack"));
			if (this.data.stage == Stage.connecting) {
				this.data.sid = response.getAttribute("sid");
				data.stage = Stage.connected;
				fireOnConnected(sessionObject);
			}
			if (this.data.stage == Stage.connected && countActiveRequests() == 0) {
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
			System.out.println("onTerminate()");
			if (response != null)
				removeFromRequests(response.getAttribute("ack"));
			this.data.stage = Stage.disconnected;
			terminateAllWorkers();
			fireOnTerminate(responseCode, response, sessionObject);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	private Element prepareBody(Element payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("sid", this.data.sid);
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	private Element prepareRetartBody() throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("sid", data.sid);
		e.setAttribute("to", data.toHost);
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:restart", "true");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");

		return e;
	}

	private Element prepareStartBody() throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("content", "text/xml; charset=utf-8");
		e.setAttribute("from", data.fromUser);
		e.setAttribute("hold", "1");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("to", data.toHost);
		e.setAttribute("secure", "true");
		e.setAttribute("wait", String.valueOf(data.defaultTimeout));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:version", "1.0");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");

		return e;
	}

	private Element prepareTerminateBody(Element payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("sid", this.data.sid);
		e.setAttribute("type", "terminate");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	protected void processSendData(final Element element) throws XMLException {
		BoshWorker worker = new BoshWorker(data, element) {

			@Override
			protected void onError(int responseCode, Element response, Throwable caught) {
				BoshConnector.this.onError(responseCode, response, caught);
			}

			@Override
			protected void onSuccess(int responseCode, Element response) {
				BoshConnector.this.onResponse(responseCode, response);
			}

			@Override
			protected void onTerminate(int responseCode, Element response) {
				BoshConnector.this.onTerminate(responseCode, response);
			}
		};

		addToRequests(worker);

		(new Thread(worker)).start();
	}

	protected void removeFromRequests(final String ack) {
		if (ack == null)
			return;
		this.requests.remove(ack);
	}

	public void removeListener(EventType eventType, Listener<BoshConnectorEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void restartStream() throws XMLException {
		if (data.stage != Stage.disconnected)
			processSendData(prepareRetartBody());
	}

	public void send(final Element stanza) throws XMLException {
		if (this.data.stage == Stage.connected) {
			if (stanza != null) {
				final Element body = prepareBody(stanza);
				System.out.println(" >>>> " + stanza.getAsString());
				processSendData(body);
			}
		} else
			throw new RuntimeException("Not connected");
	}

	public void start(final SessionObject sessionObject) throws IOException, XMLException {
		this.sessionObject = sessionObject;
		data.url = new URL((String) sessionObject.getProperty(BOSH_SERVICE_URL));
		data.fromUser = sessionObject.getProperty(SessionObject.USER_JID);
		data.toHost = sessionObject.getProperty(SessionObject.SERVER_NAME);

		data.stage = Stage.connecting;
		this.data.rid = (long) (Math.random() * 10000000);
		processSendData(prepareStartBody());

	}

	public void stop() throws IOException, XMLException {
		if (data.stage != Stage.disconnected)
			processSendData(prepareTerminateBody(null));
	}

	protected void terminateAllWorkers() {
		for (BoshWorker w : this.requests.values()) {
			w.terminate();
		}
		this.requests.clear();
	}
}
