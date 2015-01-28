/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc. <office@tigase.com>
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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StreamPacket;
import tigase.jaxmpp.core.client.xmpp.utils.MutableBoolean;

/**
 *
 * @author andrzej
 */
public abstract class AbstractWebSocketConnector implements Connector {

	protected final Context context;
	protected final Logger log;

	protected Boolean rfcCompatible = null;

	protected AbstractWebSocketConnector(Context context) {
		this.log = Logger.getLogger(this.getClass().getName());
		this.context = context;
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return new WebSocketXmppSessionLogic(this, modulesManager, context);
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

	protected void fireOnStanzaReceived(StreamPacket response, SessionObject sessionObject) throws JaxmppException {
		StanzaReceivedHandler.StanzaReceivedEvent event = new StanzaReceivedHandler.StanzaReceivedEvent(sessionObject, response);
		context.getEventBus().fire(event);
	}

	protected void fireOnTerminate(SessionObject sessionObject) throws JaxmppException {
		StreamTerminatedHandler.StreamTerminatedEvent event = new StreamTerminatedHandler.StreamTerminatedEvent(sessionObject);
		context.getEventBus().fire(event);
	}

	@Override
	public Connector.State getState() {
		return this.context.getSessionObject().getProperty(CONNECTOR_STAGE_KEY);
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
			context.getEventBus().fire(new SeeOtherHostHandler.SeeOtherHostEvent(context.getSessionObject(), seeHost, handled));

			return false;
		}
		return false;
	}

	protected boolean handleSeeOtherUri(String seeOtherUri) throws JaxmppException {
		try {
			stop();
			fireOnError(null, null, AbstractWebSocketConnector.this.context.getSessionObject());
		} catch (Exception ex) {
			log.log(Level.SEVERE, "could not properly handle see-other-host", ex);
		}
		MutableBoolean handled = new MutableBoolean();
		context.getEventBus().fire(new SeeOtherHostHandler.SeeOtherHostEvent(context.getSessionObject(), seeOtherUri, handled));

		return false;
	}

	@Override
	public boolean isCompressed() {
		return false;
	}

	protected boolean isRfc() {
		return rfcCompatible;
	}

	@Override
	public void keepalive() throws JaxmppException {
		if (context.getSessionObject().getProperty(DISABLE_KEEPALIVE_KEY) == Boolean.TRUE)
			return;
		if (getState() == Connector.State.connected)
			send(" ");
	}

	protected void onError(Element response, Throwable ex) {
		try {
			if (response != null) {
				if (handleSeeOtherHost(response))
					return;
			}
			stop();
			fireOnError(null, ex, AbstractWebSocketConnector.this.context.getSessionObject());
		} catch (JaxmppException ex1) {
			log.log(Level.SEVERE, null, ex1);
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
		fireOnTerminate(context.getSessionObject());
	}

	protected void processElement(Element child) throws JaxmppException {
		boolean isRfc = isRfc();
		if (isRfc && "urn:ietf:params:xml:ns:xmpp-framing".equals(child.getXMLNS())) {
			if ("close".equals(child.getName())) {
				if (child.getAttribute("see-other-uri") != null) {
					// received new version of see-other-host called
					// see-other-uri
					// designed just for XMPP over WebSocket
					String uri = child.getAttribute("see-other-uri");
					handleSeeOtherUri(uri);
					return;
				}
				log.finest("received <close/> stanza, so we need to close this connection..");
				// stop();
				this.onStreamTerminate();
			}
			if ("open".equals(child.getName())) {
				// received <open/> stanza should be ignored
				this.onStreamStart(child.getAttributes());
				return;
			}
		}

		if (("error".equals(child.getName()) && child.getXMLNS() != null && child.getXMLNS().equals(
				"http://etherx.jabber.org/streams"))
				|| "stream:error".equals(child.getName())) {
			onError(child, null);
		} else {
			StreamPacket p;
			if (Stanza.canBeConverted(child)) {
				p = Stanza.create(child);
			} else {
				p = new StreamPacket(child) {
				};
			}
			p.setXmppStream(context.getStreamsManager().getDefaultStream());
			fireOnStanzaReceived(p, context.getSessionObject());
		}
	}

	@Override
	public void restartStream() throws XMLException, JaxmppException {
		StringBuilder sb = new StringBuilder();
		if (isRfc()) {
			sb.append("<open ");
		} else {
			sb.append("<stream:stream ");
		}

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

		sb.append("version='1.0' ");

		if (isRfc()) {
			sb.append("xmlns='urn:ietf:params:xml:ns:xmpp-framing'/>");
		} else {
			sb.append("xmlns='jabber:client' ");
			sb.append("xmlns:stream='http://etherx.jabber.org/streams'>");
		}

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
		try {
			context.getEventBus().fire(new StanzaSendingHandler.StanzaSendingEvent(context.getSessionObject(), stanza));
		} catch (Exception e) {
		}
		send(stanza.getAsString());
	}

	protected abstract void send(String data) throws JaxmppException;

	protected void setStage(State state) throws JaxmppException {
		State s = this.context.getSessionObject().getProperty(CONNECTOR_STAGE_KEY);
		this.context.getSessionObject().setProperty(SessionObject.Scope.stream, CONNECTOR_STAGE_KEY, state);
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
	public void stop() throws JaxmppException {
		if (getState() == State.disconnected)
			return;
		terminateStream();
		setStage(State.disconnecting);
		terminateAllWorkers();
	}

	@Override
	public void stop(boolean terminate) throws JaxmppException {
		if (terminate)
			this.onStreamTerminate();
		else
			this.stop();
	}

	protected void terminateAllWorkers() throws JaxmppException {
		context.getEventBus().fire(new DisconnectedHandler.DisconnectedEvent(context.getSessionObject()));
	}

	protected void terminateStream() throws JaxmppException {
		final State state = getState();
		if (state == State.connected || state == State.connecting) {
			String x = isRfc() ? "<close xmlns='urn:ietf:params:xml:ns:xmpp-framing'/>" : "</stream:stream>";
			log.fine("Terminating XMPP Stream");
			send(x);
		} else {
			log.fine("Stream terminate not sent, because of connection state==" + state);
		}
	}
}
