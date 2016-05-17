/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.Connector.ConnectedHandler.ConnectedEvent;
import tigase.jaxmpp.core.client.Connector.ErrorHandler.ErrorEvent;
import tigase.jaxmpp.core.client.Connector.StanzaReceivedHandler.StanzaReceivedEvent;
import tigase.jaxmpp.core.client.Connector.StanzaSendingHandler.StanzaSendingEvent;
import tigase.jaxmpp.core.client.Connector.StateChangedHandler.StateChangedEvent;
import tigase.jaxmpp.core.client.Connector.StreamTerminatedHandler.StreamTerminatedEvent;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StreamPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for implementing BOSH connector.
 */
public abstract class AbstractBoshConnector implements Connector {

	/**
	 * Name of property that specify BOSH service URL.<br/>
	 * Type: {@linkplain String String}.
	 */
	public static final String BOSH_SERVICE_URL_KEY = "BOSH_SERVICE_URL_KEY";
	/**
	 * Type: {@linkplain Long Long}.
	 */
	public final static String RID_KEY = "BOSH#RID_KEY";
	/**
	 * Name of property that specify BOSH Session ID.<br/>
	 * Type: {@linkplain String String}.
	 */
	public static final String SID_KEY = "BOSH#SID_KEY";
	/**
	 * Name of property that specify longest time that client will wait for
	 * response.<br/>
	 * Type: {@linkplain String String}.
	 */
	private static final String DEFAULT_TIMEOUT_KEY = "BOSH#DEFAULT_TIMEOUT_KEY";
	protected final Context context;
	protected final Logger log;
	protected final Set<BoshRequest> requests = new HashSet<BoshRequest>();

	public AbstractBoshConnector(Context context) {
		this.context = context;
		this.log = Logger.getLogger(this.getClass().getName());
		context.getSessionObject().setProperty(Scope.stream, DEFAULT_TIMEOUT_KEY, "30");
	}

	protected void addToRequests(final BoshRequest worker) {
		synchronized (requests) {
			this.requests.add(worker);
		}
	}

	protected int countActiveRequests() {
		synchronized (this.requests) {
			return this.requests.size();
		}
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return new BoshXmppSessionLogic(context, this, modulesManager);
	}

	protected void fireOnConnected(SessionObject sessionObject) throws JaxmppException {
		ConnectedEvent event = new ConnectedEvent(sessionObject);
		context.getEventBus().fire(event, this);
	}

	protected void fireOnError(int responseCode, String responseData, Element response, Throwable caught,
			SessionObject sessionObject) throws JaxmppException {
		// XXX XXX FIXME
		StreamError condition = StreamError.undefined_condition;
		if (response != null) {
			List<Element> streamErrors = response.getChildren("stream:error");
			if (streamErrors != null && !streamErrors.isEmpty()) {
				// errorElement = streamErrors.get(0);
			}
		}

		ErrorEvent e = new ErrorEvent(sessionObject, condition, caught);
		context.getEventBus().fire(e, this);
	}

	protected void fireOnStanzaReceived(int responseCode, String responseData, Element response, SessionObject sessionObject)
			throws JaxmppException {
		try {
			{
				BoshPacketReceivedHandler.BoshPacketReceivedEvent event = new BoshPacketReceivedHandler.BoshPacketReceivedEvent(
						sessionObject, responseCode, response, responseData);
				context.getEventBus().fire(event, this);

			}
			if (response != null) {
				List<Element> c = response.getChildren();
				for (Element ch : c) {
					StreamPacket p;
					if (Stanza.canBeConverted(ch)) {
						p = Stanza.create(ch);
					} else {
						p = new StreamPacket(ch) {
						};
					}

					p.setXmppStream(context.getStreamsManager().getDefaultStream());
					StanzaReceivedEvent event = new StanzaReceivedEvent(sessionObject, p);
					context.getEventBus().fire(event, this);
				}
			}
		} catch (XMLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void fireOnTerminate(int responseCode, String responseData, Element response, SessionObject sessionObject)
			throws JaxmppException {

		// XXX check

		StreamTerminatedEvent event = new StreamTerminatedEvent(sessionObject);
		context.getEventBus().fire(event, this);
	}

	protected String getSid() {
		return this.context.getSessionObject().getProperty(SID_KEY);
	}

	protected void setSid(String sid) {
		this.context.getSessionObject().setProperty(SID_KEY, sid);
	}

	@Override
	public State getState() {
		return this.context.getSessionObject().getProperty(CONNECTOR_STAGE_KEY);
	}

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
		Long i = context.getSessionObject().getProperty(RID_KEY);
		if (i == null) {
			i = (long) (Math.random() * 10000000);
		}
		i++;
		context.getSessionObject().setProperty(RID_KEY, i);
		return i;
	}

	protected void onError(BoshRequest request, int responseCode, String responseData, Element response, Throwable caught)
			throws JaxmppException {
		removeFromRequests(request);
		if (log.isLoggable(Level.FINER))
			log.log(Level.FINER, "responseCode=" + responseCode, caught);
		setStage(State.disconnected);
		terminateAllWorkers();
		fireOnError(responseCode, responseData, response, caught, context.getSessionObject());
	}

	protected void onResponse(BoshRequest request, final int responseCode, String responseData, final Element response)
			throws JaxmppException {
		removeFromRequests(request);
		if (response != null && getState() == State.connecting) {
			setSid(response.getAttribute("sid"));
			setStage(State.connected);
			fireOnConnected(context.getSessionObject());
		}
		if (response != null)
			fireOnStanzaReceived(responseCode, responseData, response, context.getSessionObject());

		if (getState() == State.connected && countActiveRequests() == 0) {
			final Element body = prepareBody((Element) null);
			processSendData(body);
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
		fireOnTerminate(responseCode, responseData, response, context.getSessionObject());
	}

	protected Element prepareBody(byte[] payload) throws XMLException {
		Element e = ElementFactory.create("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.setValue(new String(payload));
		return e;
	}

	protected Element prepareBody(Element payload) throws XMLException {
		Element e = ElementFactory.create("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	protected Element prepareRetartBody() throws XMLException {
		Element e = ElementFactory.create("body");
		final BareJID from = context.getSessionObject().getProperty(SessionObject.USER_BARE_JID);
		if (from != null) {
			e.setAttribute("from", from.toString());
		}
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("to", (String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:restart", "true");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");

		return e;
	}

	protected Element prepareStartBody() throws XMLException {
		Element e = ElementFactory.create("body");
		e.setAttribute("content", "text/xml; charset=utf-8");
		// e.setAttribute("from", data.fromUser);
		final BareJID from = context.getSessionObject().getProperty(SessionObject.USER_BARE_JID);
		Boolean seeOtherHost = context.getSessionObject().getProperty(SEE_OTHER_HOST_KEY);
		if (from != null && seeOtherHost != null && seeOtherHost) {
			e.setAttribute("from", from.toString());
		}
		e.setAttribute("hold", "1");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("to", (String) context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME));
		e.setAttribute("secure", "true");
		e.setAttribute("wait", (String) context.getSessionObject().getProperty(DEFAULT_TIMEOUT_KEY));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:version", "1.0");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");
		e.setAttribute("cache", "on");

		return e;
	}

	protected Element prepareTerminateBody(Element payload) throws XMLException {
		Element e = ElementFactory.create("body");
		e.setAttribute("rid", nextRid().toString());
		e.setAttribute("sid", getSid());
		e.setAttribute("type", "terminate");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	protected abstract void processSendData(final Element element) throws JaxmppException;

	protected void removeFromRequests(final BoshRequest ack) {
		synchronized (this.requests) {
			this.requests.remove(ack);
		}
	}

	@Override
	public void restartStream() throws JaxmppException {
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
	public void send(final Element stanza) throws JaxmppException {
		if (getState() == State.connected) {
			if (stanza != null) {
				final Element body = prepareBody(stanza);
				context.getEventBus().fire(new StanzaSendingEvent(context.getSessionObject(), stanza));
				processSendData(body);
			}
		} else
			throw new JaxmppException("Not connected");
	}

	protected void setStage(State state) throws JaxmppException {
		State s = this.context.getSessionObject().getProperty(CONNECTOR_STAGE_KEY);
		this.context.getSessionObject().setProperty(Scope.stream, CONNECTOR_STAGE_KEY, state);
		if (s != state) {
			StateChangedEvent e = new StateChangedEvent(context.getSessionObject(), s, state);
			context.getEventBus().fire(e, this);
		}
	}

	@Override
	public void start() throws JaxmppException {
		// if
		// (context.getSessionObject().getProperty(SessionObject.USER_BARE_JID)
		// == null)
		// throw new JaxmppException("No user JID specified");

		if (context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME) == null)
			context.getSessionObject().setProperty(SessionObject.DOMAIN_NAME,
					((BareJID) context.getSessionObject().getProperty(SessionObject.USER_BARE_JID)).getDomain());

		String u = context.getSessionObject().getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
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
	public void stop() throws JaxmppException {
		stop(false);
	}

	@Override
	public void stop(boolean terminate) throws JaxmppException {
		State oldState = getState();

		setStage(State.disconnecting);
		if (terminate)
			terminateAllWorkers();
		else if (getState() != State.disconnected && oldState != State.disconnecting) {
			// if we already are in disconnecting state then we should not send termination
			// of stream as it was already sent
			processSendData(prepareTerminateBody(null));
		}
	}

	protected void terminateAllWorkers() {
		synchronized (this.requests) {
			for (BoshRequest w : this.requests) {
				w.terminate();
			}
			this.requests.clear();
		}

		context.getEventBus().fire(new DisconnectedHandler.DisconnectedEvent(context.getSessionObject()));
	}

	/**
	 * Implemented by handlers of {@linkplain BoshPacketReceivedEvent
	 * BoshPacketReceivedEvent}.
	 */
	public interface BoshPacketReceivedHandler extends EventHandler {

		/**
		 * Called when {@linkplain BoshPacketReceivedEvent
		 * BoshPacketReceivedEvent} is fired.
		 *
		 * @param sessionObject session object related to connection.
		 * @param responseCode  HTTP response code.
		 * @param response      received BOSH packet.
		 */
		void onBoshPacketReceived(SessionObject sessionObject, int responseCode, Element response);

		/**
		 * Fired BOSH packet is received.
		 */
		class BoshPacketReceivedEvent extends JaxmppEvent<BoshPacketReceivedHandler> {

			private String receivedData;

			private Element response;

			private int responseCode;

			public BoshPacketReceivedEvent(SessionObject sessionObject, int responseCode, Element response,
										   String responseData) {
				super(sessionObject);
				this.responseCode = responseCode;
				this.response = response;
				this.receivedData = responseData;
			}

			@Override
			public void dispatch(BoshPacketReceivedHandler handler) {
				handler.onBoshPacketReceived(sessionObject, responseCode, response);
			}

			public String getReceivedData() {
				return receivedData;
			}

			public Element getResponse() {
				return response;
			}

			public int getResponseCode() {
				return responseCode;
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain BoshPacketSendingEvent
	 * BoshPacketSendingEvent}.
	 */
	public interface BoshPacketSendingHandler extends EventHandler {

		/**
		 * Called when {@linkplain BoshPacketSendingEvent
		 * BoshPacketSendingEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 * @param packet
		 *            sending BOSH packet.
		 */
		void onBoshPacketSending(SessionObject sessionObject, Element packet) throws JaxmppException;

		/**
		 * Fired when BOSH packet is sending.
		 */
		class BoshPacketSendingEvent extends JaxmppEvent<BoshPacketSendingHandler> {

			private Element element;

			public BoshPacketSendingEvent(SessionObject sessionObject, Element element) {
				super(sessionObject);
				this.element = element;
			}

			@Override
			public void dispatch(BoshPacketSendingHandler handler) throws JaxmppException {
				handler.onBoshPacketSending(sessionObject, element);
			}

			public Element getElement() {
				return element;
			}

			public void setElement(Element stanza) {
				this.element = stanza;
			}

		}
	}
}