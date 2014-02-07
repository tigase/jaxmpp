/*
 * Tigase XMPP Client Library
 * Copyright (C) 2013 "Andrzej Wójcik" <andrzej.wojcik@tigase.org>
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
package tigase.jaxmpp.gwt.client.connectors;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.BoshXmppSessionLogic;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

import com.google.gwt.user.client.Timer;
import com.google.gwt.xml.client.XMLParser;
import java.util.ArrayList;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.MutableBoolean;
import tigase.jaxmpp.gwt.client.connectors.SeeOtherHostHandler.SeeOtherHostEvent;

/**
 * 
 * @author andrzej
 */
public class WebSocketConnector implements Connector {

	private final Context context;
	protected final Logger log;
	private Timer pingTimer = null;
	private WebSocket socket = null;

	private int SOCKET_TIMEOUT = 1000 * 60 * 3;
	private final WebSocketCallback socketCallback;

	public WebSocketConnector(Context context) {
		this.log = Logger.getLogger(this.getClass().getName());
		this.context = context;

		socketCallback = new WebSocketCallback() {
			@Override
			public void onClose(WebSocket ws) {
				try {
					stop(true);
				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				}
			}

			@Override
			public void onError(WebSocket ws) {
				log.warning("received WebSocket error - terminating");
				try {
					stop(true);
				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				}
			}

			@Override
			public void onMessage(WebSocket ws, String message) {
				try {
					parseSocketData(message);
				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				}
			}

			@Override
			public void onOpen(WebSocket ws) {
				try {
					setStage(State.connected);
					restartStream();

					pingTimer = new Timer() {
						@Override
						public void run() {
							try {
								keepalive();
							} catch (JaxmppException e) {
								log.log(Level.SEVERE, "Can't ping!", e);
							}
						}
					};
					int delay = SOCKET_TIMEOUT - 1000 * 5;

					if (log.isLoggable(Level.CONFIG)) {
						log.config("Whitespace ping period is setted to " + delay + "ms");
					}

					if (WebSocketConnector.this.context.getSessionObject().getProperty(EXTERNAL_KEEPALIVE_KEY) == null
							|| ((Boolean) WebSocketConnector.this.context.getSessionObject().getProperty(EXTERNAL_KEEPALIVE_KEY) == false)) {
						pingTimer.scheduleRepeating(delay);
					}

					fireOnConnected(WebSocketConnector.this.context.getSessionObject());

				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				}
			}
		};
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return new BoshXmppSessionLogic(context, this, modulesManager);
	}

	protected void fireOnConnected(SessionObject sessionObject) throws JaxmppException {
		if (getState() == State.disconnected) {
			return;
		}
		context.getEventBus().fire(new ConnectedHandler.ConnectedEvent(sessionObject));
	}

	protected void fireOnError(Element response, Throwable caught, SessionObject sessionObject) throws JaxmppException {
		StreamError condition = null;

		if (response != null) {
			List<Element> es = response.getChildrenNS("urn:ietf:params:xml:ns:xmpp-streams");
			if (es != null) {
				for (Element element : es) {
					String n = element.getName();
					condition = StreamError.getByElementName(n);
				}
			}
		}

		context.getEventBus().fire(new ErrorHandler.ErrorEvent(sessionObject, condition, caught));
	}

	private void fireOnStanzaReceived(Element response, SessionObject sessionObject) throws JaxmppException {
		StanzaReceivedHandler.StanzaReceivedEvent event = new StanzaReceivedHandler.StanzaReceivedEvent(sessionObject, response);
		context.getEventBus().fire(event);
	}

	protected void fireOnTerminate(SessionObject sessionObject) throws JaxmppException {
		StreamTerminatedHandler.StreamTerminatedEvent event = new StreamTerminatedHandler.StreamTerminatedEvent(sessionObject);
		context.getEventBus().fire(event);
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
		if (context.getSessionObject().getProperty(DISABLE_KEEPALIVE_KEY) == Boolean.TRUE)
			return;
		if (getState() == State.connected)
			send(" ");
	}

	protected void onError(Element response, Throwable ex) {		
		try {
			if (response != null) {
				if (handleSeeOtherHost(response)) 
					return;
			}
			stop();
			fireOnError(null, ex, WebSocketConnector.this.context.getSessionObject());
		} catch (JaxmppException ex1) {
			log.log(Level.SEVERE, null, ex1);
		}
	}

	protected boolean handleSeeOtherHost(Element response) throws JaxmppException {
		if (response == null) 
			return false;

		Element seeOtherHost = response.getChildrenNS("see-other-host", "urn:ietf:params:xml:ns:xmpp-streams");
		if (seeOtherHost != null) {
			String seeHost = seeOtherHost.getValue();
			if (log.isLoggable(Level.FINE)) {
				log.fine("Received see-other-host=" + seeHost);
			}
			MutableBoolean handled = new MutableBoolean();
			context.getEventBus().fire(
					new SeeOtherHostHandler.SeeOtherHostEvent(context.getSessionObject(), seeHost, handled));

			return false;
		}
		return false;
	}		
	
	private void parseSocketData(String x) throws JaxmppException {
		// ignore keep alive "whitespace"
		if (x == null || x.length() == 1) {
			x = x.trim();
			if (x.length() == 0)
				return;
		}
		
		log.warning("received = " + x);

		// workarounds for xml parsers implemented in browsers
		if (x.endsWith("</stream:stream>") && !x.startsWith("<stream:stream ")) {
			x = "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' >" + x;
		}
		// workarounds for xml parsers implemented in browsers
		else if (x.startsWith("<stream:")) {
			// unclosed xml tags causes error!!
			if (x.startsWith("<stream:stream ") && !x.contains("</stream:stream>")) {
				x += "</stream:stream>";
			}
			// xml namespace must be declared!!
			else if (!x.contains("xmlns:stream")) {
				int spaceIdx = x.indexOf(" ");
				int closeIdx = x.indexOf(">");
				int idx = spaceIdx < closeIdx ? spaceIdx : closeIdx;
				x = x.substring(0, idx) + " xmlns:stream='http://etherx.jabber.org/streams' " + x.substring(idx);
			}
		}

		Element response = new GwtElement(XMLParser.parse(x).getDocumentElement());
		List<Element> received = null;
		if ("stream:stream".equals(response.getName()) || "stream".equals(response.getName())) {
			received = response.getChildren();
		}
		else {
			received = new ArrayList<Element>();
			received.add(response);
		}

		if (received != null) {
			for (Element child : received) {
				if ("parsererror".equals(child.getName())) {
					continue;
				}

				if (("error".equals(child.getName()) && child.getXMLNS() != null
						&& child.getXMLNS().equals("http://etherx.jabber.org/streams"))
						|| "stream:error".equals(child.getName())) {
					onError(child, null);
				} else {
					fireOnStanzaReceived(child, context.getSessionObject());
				}
			}
		}
		
		
	}

	@Override
	public void restartStream() throws XMLException, JaxmppException {
		StringBuilder sb = new StringBuilder();
		sb.append("<stream:stream ");

		final BareJID from = context.getSessionObject().getProperty(SessionObject.USER_BARE_JID);
		String to;
		Boolean seeOtherHost = context.getSessionObject().getProperty(SEE_OTHER_HOST_KEY);
		if (from != null && (seeOtherHost == null || seeOtherHost)) {
			to = from.getDomain();
			sb.append("from='").append(from.toString()).append("' ");
		} else {
			to = context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME);
		}

		if (to != null) {
			sb.append("to='").append(to).append("' ");
		}

		sb.append("xmlns='jabber:client' ");
		sb.append("xmlns:stream='http://etherx.jabber.org/streams' ");
		sb.append("version='1.0'>");

		if (log.isLoggable(Level.FINEST)) {
			log.finest("Restarting XMPP Stream");
		}
		send(sb.toString());
	}

	@Override
	public void send(Element stanza) throws XMLException, JaxmppException {
		if (stanza == null) {
			return;
		}
		send(stanza.getAsString());
	}

	public void send(final String data) throws JaxmppException {
		if (getState() == State.connected) {
			socket.send(data);
		} else {
			throw new JaxmppException("Not connected");
		}
	}

	protected void setStage(State state) throws JaxmppException {
		State s = this.context.getSessionObject().getProperty(CONNECTOR_STAGE_KEY);
		this.context.getSessionObject().setProperty(Scope.stream, CONNECTOR_STAGE_KEY, state);
		if (s != state) {
			log.fine("Connector state changed: " + s + "->" + state);
			StateChangedHandler.StateChangedEvent e = new StateChangedHandler.StateChangedEvent(context.getSessionObject(), s,
					state);
			context.getEventBus().fire(e);
			if (state == State.disconnected) {
				setStage(State.disconnected);
				fireOnTerminate(context.getSessionObject());
			}

			if (state == State.disconnecting) {
				try {
					throw new JaxmppException("disconnecting!!!");
				} catch (Exception ex) {
					log.log(Level.WARNING, "DISCONNECTING!!", ex);
				}
			}
		}
	}

	@Override
	public void start() throws XMLException, JaxmppException {
		String url = context.getSessionObject().getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
		setStage(State.connecting);
		socket = new WebSocket(url, "xmpp", socketCallback);
	}

	@Override
	public void stop() throws XMLException, JaxmppException {
		stop(false);
	}

	@Override
	public void stop(boolean terminate) throws XMLException, JaxmppException {
		if (getState() == State.disconnected) {
			return;
		}
		setStage(State.disconnecting);
		if (!terminate) {
			terminateStream();
		}

		if (this.pingTimer != null) {
			this.pingTimer.cancel();
			this.pingTimer = null;
		}

		socket.close();
	}

	private void terminateStream() throws JaxmppException {
		final State state = getState();
		if (state == State.connected || state == State.connecting) {
			String x = "</stream:stream>";
			log.fine("Terminating XMPP Stream");
			send(x);
		} else {
			log.fine("Stream terminate not sent, because of connection state==" + state);
		}
	}
}
