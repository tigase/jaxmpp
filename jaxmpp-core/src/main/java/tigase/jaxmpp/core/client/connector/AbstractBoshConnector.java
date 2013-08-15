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
package tigase.jaxmpp.core.client.connector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.ErrorElement;

public abstract class AbstractBoshConnector implements Connector {

	public static class BoshConnectorEvent extends ConnectorEvent {

		private static final long serialVersionUID = 1L;
		private Element body;
		private ErrorElement errorElement;
		private int responseCode;
		private String responseData;

		public BoshConnectorEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Element getBody() {
			return body;
		}

		public ErrorElement getErrorElement() {
			return errorElement;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public String getResponseData() {
			return responseData;
		}

		public void setBody(Element response) {
			this.body = response;
		}

		public void setErrorElement(ErrorElement errorElement) {
			this.errorElement = errorElement;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public void setResponseData(String responseData) {
			this.responseData = responseData;
		}
	}

	public static final String AUTHID_KEY = "BOSH#AUTHID_KEY";

	/**
	 * @deprecated use {@linkplain BOSH_SERVICE_URL_KEY
	 *             AbstractBoshConnector#BOSH_SERVICE_URL_KEY}
	 */
	@Deprecated
	public static final String BOSH_SERVICE_URL = "BOSH_SERVICE_URL_KEY";

	public static final String BOSH_SERVICE_URL_KEY = "BOSH_SERVICE_URL_KEY";

	private static final String DEFAULT_TIMEOUT_KEY = "BOSH#DEFAULT_TIMEOUT_KEY";

	public final static String RID_KEY = "BOSH#RID_KEY";

	public static final String SID_KEY = "BOSH#SID_KEY";

	protected final Logger log;

	protected Observable observable;

	protected final Set<BoshRequest> requests = new HashSet<BoshRequest>();

	protected final SessionObject sessionObject;

	public AbstractBoshConnector(Observable parentObservable, SessionObject sessionObject) {
		this.observable = ObservableFactory.instance(parentObservable);
		this.log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		sessionObject.setProperty(Scope.stream, DEFAULT_TIMEOUT_KEY, "30");
	}

	@Override
	public void addListener(EventType eventType, Listener<? extends ConnectorEvent> listener) {
		observable.addListener(eventType, listener);
	}

	protected void addToRequests(final BoshRequest worker) {
		this.requests.add(worker);
	}

	protected int countActiveRequests() {
		return this.requests.size();
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return new BoshXmppSessionLogic(this, modulesManager, sessionObject, writer);
	}

	protected void fireOnConnected(SessionObject sessionObject) throws JaxmppException {
		BoshConnectorEvent event = new BoshConnectorEvent(Connected, sessionObject);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnError(int responseCode, String responseData, Element response, Throwable caught,
			SessionObject sessionObject) throws JaxmppException {
		BoshConnectorEvent event = new BoshConnectorEvent(Error, sessionObject);
		event.setResponseCode(responseCode);
		event.setResponseData(responseData);
		if (response != null) {
			try {
				event.setErrorElement(ErrorElement.extract(response));
			} catch (XMLException e) {
				event.setErrorElement(null);
			}
			List<Element> streamError = response.getChildren("stream:error");
			if (streamError != null && !streamError.isEmpty()) {
				event.setStreamErrorElement(streamError.get(0));
			}
		}
		event.setBody(response);
		event.setCaught(caught);
		this.observable.fireEvent(event.getType(), event);
	}

	protected void fireOnStanzaReceived(int responseCode, String responseData, Element response, SessionObject sessionObject)
			throws JaxmppException {
		try {
			{
				BoshConnectorEvent event = new BoshConnectorEvent(BodyReceived, sessionObject);
				event.setResponseData(responseData);
				event.setBody(response);
				event.setResponseCode(responseCode);
				this.observable.fireEvent(event.getType(), event);

			}
			if (response != null) {
				List<Element> c = response.getChildren();
				for (Element ch : c) {
					BoshConnectorEvent event = new BoshConnectorEvent(StanzaReceived, sessionObject);
					event.setResponseData(responseData);
					event.setBody(response);
					event.setResponseCode(responseCode);
					if (response != null) {
						event.setStanza(ch);
					}
					this.observable.fireEvent(event.getType(), event);
				}
			}
		} catch (XMLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireOnTerminate(int responseCode, String responseData, Element response, SessionObject sessionObject)
			throws JaxmppException {
		BoshConnectorEvent event = new BoshConnectorEvent(StreamTerminated, sessionObject);
		event.setResponseCode(responseCode);
		event.setResponseData(responseData);
		event.setBody(response);
		this.observable.fireEvent(event.getType(), event);
	}

	@Override
	public Observable getObservable() {
		return observable;
	}

	protected String getSid() {
		return this.sessionObject.getProperty(SID_KEY);
	}

	@Override
	public State getState() {
		return this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
	}

	/**
	 * Returns true when stream is compressed
	 * 
	 * @return
	 */
	@Override
	public boolean isCompressed() {
		return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void keepalive() throws JaxmppException {
		if (getState() == State.connected) {
			processSendData(prepareBody((Element) null));
		} else
			throw new JaxmppException("Not connected");
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

	protected void onError(BoshRequest request, int responseCode, String responseData, Element response, Throwable caught)
			throws JaxmppException {
		removeFromRequests(request);
		if (log.isLoggable(Level.FINER))
			log.log(Level.FINER, "responseCode=" + responseCode, caught);
		setStage(State.disconnected);
		fireOnError(responseCode, responseData, response, caught, sessionObject);
	}

	protected void onResponse(BoshRequest request, final int responseCode, String responseData, final Element response)
			throws JaxmppException {
		removeFromRequests(request);
		try {
			if (response != null && getState() == State.connecting) {
				setSid(response.getAttribute("sid"));
				setStage(State.connected);
				fireOnConnected(sessionObject);
			}
			if (response != null)
				fireOnStanzaReceived(responseCode, responseData, response, sessionObject);

			if (getState() == State.connected && countActiveRequests() == 0) {
				final Element body = prepareBody((Element) null);
				processSendData(body);
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void onTerminate(BoshRequest request, int responseCode, String responseData, Element response)
			throws JaxmppException {
		removeFromRequests(request);
		if (getState() == State.disconnected)
			return;
		if (log.isLoggable(Level.FINE))
			log.fine("Stream terminated. responseCode=" + responseCode);
		setStage(State.disconnected);
		terminateAllWorkers();
		fireOnTerminate(responseCode, responseData, response, sessionObject);
	}

	protected Element prepareBody(byte[] payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.setValue(new String(payload));
		return e;
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
		final BareJID from = sessionObject.getProperty(SessionObject.USER_BARE_JID);
		if (from != null) {
			e.setAttribute("from", from.toString());
		}
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("to", (String) sessionObject.getProperty(SessionObject.DOMAIN_NAME));
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
		final BareJID from = sessionObject.getProperty(SessionObject.USER_BARE_JID);
		Boolean seeOtherHost = sessionObject.getProperty(SEE_OTHER_HOST_KEY);
		if (from != null && seeOtherHost != null && seeOtherHost) {
			e.setAttribute("from", from.toString());
		}
		e.setAttribute("hold", "1");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("to", (String) sessionObject.getProperty(SessionObject.DOMAIN_NAME));
		e.setAttribute("secure", "true");
		e.setAttribute("wait", (String) sessionObject.getProperty(DEFAULT_TIMEOUT_KEY));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:version", "1.0");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");
		e.setAttribute("cache", "on");

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

	protected void removeFromRequests(final BoshRequest ack) {
		this.requests.remove(ack);
	}

	@Override
	public void removeListener(EventType eventType, Listener<ConnectorEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	@Override
	public void restartStream() throws XMLException, JaxmppException {
		if (getState() != State.disconnected) {
			processSendData(prepareRetartBody());
		}
	}

	public void send(byte[] buffer) throws JaxmppException {
		if (getState() == State.connected) {
			if (buffer != null && buffer.length > 0) {
				final Element body = prepareBody(buffer);
				processSendData(body);
			}
		} else
			throw new JaxmppException("Not connected");
	}

	@Override
	public void send(final Element stanza) throws XMLException, JaxmppException {
		if (getState() == State.connected) {
			if (stanza != null) {
				final Element body = prepareBody(stanza);
				processSendData(body);
			}
		} else
			throw new JaxmppException("Not connected");
	}

	@Override
	public void setObservable(Observable observable) {
		if (observable == null)
			this.observable = ObservableFactory.instance(null);
		else
			this.observable = observable;
	}

	protected void setSid(String sid) {
		this.sessionObject.setProperty(SID_KEY, sid);
	}

	protected void setStage(State state) throws JaxmppException {
		State s = this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
		this.sessionObject.setProperty(Scope.stream, CONNECTOR_STAGE_KEY, state);
		if (s != state) {
			ConnectorEvent e = new ConnectorEvent(StateChanged, sessionObject);
			observable.fireEvent(e);
		}
	}

	@Override
	public void start() throws XMLException, JaxmppException {
		// if (sessionObject.getProperty(SessionObject.USER_BARE_JID) == null)
		// throw new JaxmppException("No user JID specified");

		if (sessionObject.getProperty(SessionObject.DOMAIN_NAME) == null)
			sessionObject.setProperty(SessionObject.DOMAIN_NAME,
					((BareJID) sessionObject.getProperty(SessionObject.USER_BARE_JID)).getDomain());

		String u = sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
		if (u == null)
			throw new JaxmppException("BOSH service URL not defined!");

		if (getState() == State.connected) {
			processSendData(prepareBody((Element) null));

			Element x = prepareBody((Element) null);
			x.setAttribute("cache", "get_all");
			processSendData(x);

			// processSendData(prepareBody(null));
			// requests.clear();
		} else {
			setStage(State.connecting);
			processSendData(prepareStartBody());
		}
	}

	@Override
	public void stop() throws XMLException, JaxmppException {
		stop(false);
	}

	@Override
	public void stop(boolean terminate) throws XMLException, JaxmppException {
		setStage(State.disconnecting);
		if (terminate)
			terminateAllWorkers();
		else if (getState() != State.disconnected) {
			processSendData(prepareTerminateBody(null));
		}
	}

	protected void terminateAllWorkers() {
		for (BoshRequest w : this.requests) {
			w.terminate();
		}
		this.requests.clear();
	}
}