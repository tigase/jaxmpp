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
import tigase.jaxmpp.core.client.Connector.BodyReceivedHandler.BodyReceivedvent;
import tigase.jaxmpp.core.client.Connector.ConnectedHandler.ConnectedEvent;
import tigase.jaxmpp.core.client.Connector.ErrorHandler.ErrorEvent;
import tigase.jaxmpp.core.client.Connector.StanzaReceivedHandler.StanzaReceivedEvent;
import tigase.jaxmpp.core.client.Connector.StanzaSendingHandler.StanzaSendingEvent;
import tigase.jaxmpp.core.client.Connector.StateChangedHandler.StateChangedEvent;
import tigase.jaxmpp.core.client.Connector.StreamTerminatedHandler.StreamTerminatedEvent;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public abstract class AbstractBoshConnector implements Connector {

	public interface BoshPacketSendingHandler extends EventHandler {

		public static class BoshPacketSendingEvent extends JaxmppEvent<BoshPacketSendingHandler> {

			private Element element;

			public BoshPacketSendingEvent(SessionObject sessionObject, Element element) {
				super(sessionObject);
				this.element = element;
			}

			@Override
			protected void dispatch(BoshPacketSendingHandler handler) throws JaxmppException {
				handler.onBoshPacketSending(sessionObject, element);
			}

			public Element getElement() {
				return element;
			}

			public void setElement(Element stanza) {
				this.element = stanza;
			}

		}

		void onBoshPacketSending(SessionObject sessionObject, Element packet) throws JaxmppException;
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

	protected final Context context;

	protected final Logger log;

	protected final Set<BoshRequest> requests = new HashSet<BoshRequest>();

	public AbstractBoshConnector(Context context) {
		this.context = context;
		this.log = Logger.getLogger(this.getClass().getName());
		context.getSessionObject().setProperty(Scope.stream, DEFAULT_TIMEOUT_KEY, "30");
	}

	protected void addToRequests(final BoshRequest worker) {
		this.requests.add(worker);
	}

	protected int countActiveRequests() {
		return this.requests.size();
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
				BodyReceivedvent event = new BodyReceivedvent(sessionObject, responseCode, response, responseData);
				context.getEventBus().fire(event, this);

			}
			if (response != null) {
				List<Element> c = response.getChildren();
				for (Element ch : c) {
					StanzaReceivedEvent event = new StanzaReceivedEvent(sessionObject, ch);
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

	@Override
	public State getState() {
		return this.context.getSessionObject().getProperty(CONNECTOR_STAGE_KEY);
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
		fireOnError(responseCode, responseData, response, caught, context.getSessionObject());
	}

	protected void onResponse(BoshRequest request, final int responseCode, String responseData, final Element response)
			throws JaxmppException {
		removeFromRequests(request);
		try {
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
		fireOnTerminate(responseCode, responseData, response, context.getSessionObject());
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
		Element e = new DefaultElement("body");
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

	protected void removeFromRequests(final BoshRequest ack) {
		this.requests.remove(ack);
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
				context.getEventBus().fire(new StanzaSendingEvent(context.getSessionObject(), stanza));
				processSendData(body);
			}
		} else
			throw new JaxmppException("Not connected");
	}

	protected void setSid(String sid) {
		this.context.getSessionObject().setProperty(SID_KEY, sid);
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
	public void start() throws XMLException, JaxmppException {
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