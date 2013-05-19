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

import com.google.gwt.user.client.Timer;
import com.google.gwt.xml.client.XMLParser;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector.BoshConnectorEvent;
import tigase.jaxmpp.core.client.connector.BoshXmppSessionLogic;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

/**
 *
 * @author andrzej
 */
public class WebSocketConnector implements Connector {

        protected final Logger log;
        private Observable observable;
        private final SessionObject sessionObject;
        private final WebSocketCallback socketCallback;
        private WebSocket socket = null;

        private Timer pingTimer = null;
	private int SOCKET_TIMEOUT = 1000 * 60 * 3;
        
        public WebSocketConnector(Observable parentObservable, SessionObject sessionObject) {
                this.observable = ObservableFactory.instance(parentObservable);
                this.log = Logger.getLogger(this.getClass().getName());
                this.sessionObject = sessionObject;

                socketCallback = new WebSocketCallback() {
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

                                        if (WebSocketConnector.this.sessionObject.getProperty(EXTERNAL_KEEPALIVE_KEY) == null
                                                || ((Boolean) WebSocketConnector.this.sessionObject.getProperty(EXTERNAL_KEEPALIVE_KEY) == false)) {
                                                pingTimer.scheduleRepeating(delay);
                                        }

                                        fireOnConnected(WebSocketConnector.this.sessionObject);

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
                        public void onError(WebSocket ws) {
                                log.warning("received WebSocket error - terminating");
                                try {
                                        stop(true);
                                } catch (JaxmppException ex) {
                                        WebSocketConnector.this.onError(null, ex);
                                }
                        }

                        @Override
                        public void onClose(WebSocket ws) {
                                try {
                                        stop(true);
                                } catch (JaxmppException ex) {
                                        WebSocketConnector.this.onError(null, ex);
                                }
                        }
                };
        }

        @Override
        public void addListener(EventType eventType, Listener<? extends ConnectorEvent> listener) {
                observable.addListener(eventType, listener);
        }

        @Override
        public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
                return new BoshXmppSessionLogic(this, modulesManager, sessionObject, writer);
        }

        @Override
        public Observable getObservable() {
                return observable;
        }

        @Override
        public State getState() {
                return this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
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
		if (sessionObject.getProperty(DISABLE_KEEPALIVE_KEY) == Boolean.TRUE)
			return;
		if (getState() == State.connected)
			send(" ");
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

        @Override
        public void setObservable(Observable observable) {
                if (observable == null) {
                        this.observable = ObservableFactory.instance(null);
                } else {
                        this.observable = observable;
                }
        }

        @Override
        public void start() throws XMLException, JaxmppException {
                String url = sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
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

        protected void setStage(State state) throws JaxmppException {
                State s = this.sessionObject.getProperty(CONNECTOR_STAGE_KEY);
                this.sessionObject.setProperty(CONNECTOR_STAGE_KEY, state);
                if (s != state) {
                        log.fine("Connector state changed: " + s + "->" + state);
                        ConnectorEvent e = new ConnectorEvent(StateChanged, sessionObject);
                        observable.fireEvent(e);
                        if (state == State.disconnected) {
                                setStage(State.disconnected);
                                fireOnTerminate(sessionObject);
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

        private void parseSocketData(String x) throws JaxmppException {
                // ignore keep alive "whitespace"
                if (x == null || x.length() == 1) {
                        x = x.trim();
                        if (x.length() == 0) 
                                return;
                }
                                
                // workarounds for xml parsers implemented in browsers
                if (x.endsWith("</stream:stream>")) {
                        x = "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' >" + x;
                }
                // workarounds for xml parsers implemented in browsers
                else if (x.startsWith("<stream:")) {
                        // unclosed xml tags causes error!!
                        if (x.startsWith("<stream:stream ")) {
                                x += "</stream:stream>";
                        }
                        // xml namespace must be declared!!
                        else {
                                int spaceIdx = x.indexOf(" ");
                                int closeIdx = x.indexOf(">");
                                int idx = spaceIdx < closeIdx ? spaceIdx : closeIdx;
                                x  = x.substring(0, idx) + " xmlns:stream='http://etherx.jabber.org/streams' " + x.substring(idx);
                        }
                }
                                
                Element response = new GwtElement(XMLParser.parse(x).getDocumentElement());                
                if ("stream:stream".equals(response.getName()) || "stream".equals(response.getName())) {
                        List<Element> children = response.getChildren();
                        if (children != null) {
                                for (Element child : children) {
                                        if ("parsererror".equals(child.getName())) {
                                                continue;
                                        }

                                        fireOnStanzaReceived(child, sessionObject);
                                }
                        }
                        return;
                }
                if ("error".equals(response.getName()) && response.getXMLNS() != null
                        && response.getXMLNS().equals("http://etherx.jabber.org/streams")) {
                        onError(response, null);
                } else {
                        fireOnStanzaReceived(response, sessionObject);
                }
        }

        protected void onError(Element elem, Throwable ex) {
                try {
                        stop();
                        fireOnError(null, ex, WebSocketConnector.this.sessionObject);
                } catch (JaxmppException ex1) {
                        log.log(Level.SEVERE, null, ex1);
                }
        }
        
        protected void fireOnConnected(SessionObject sessionObject) throws JaxmppException {
                if (getState() == State.disconnected) {
                        return;
                }
                ConnectorEvent event = new ConnectorEvent(Connected, sessionObject);
                this.observable.fireEvent(event.getType(), event);
        }

        protected void fireOnError(Element response, Throwable caught, SessionObject sessionObject) throws JaxmppException {
                ConnectorEvent event = new ConnectorEvent(Error, sessionObject);
                event.setStanza(response);
                event.setCaught(caught);

                if (response != null) {
                        List<Element> es = response.getChildrenNS("urn:ietf:params:xml:ns:xmpp-streams");
                        if (es != null) {
                                for (Element element : es) {
                                        String n = element.getName();
                                        StreamError err = StreamError.getByElementName(n);
                                        event.setStreamError(err);
                                        event.setStreamErrorElement(element);
                                }
                        }
                }

                this.observable.fireEvent(event.getType(), event);
        }

        private void fireOnStanzaReceived(Element response, SessionObject sessionObject) throws JaxmppException {
                ConnectorEvent event = new ConnectorEvent(StanzaReceived, sessionObject);
                if (response != null) {
                        event.setStanza(response);
                }
                this.observable.fireEvent(event.getType(), event);

        }

        protected void fireOnTerminate(SessionObject sessionObject) throws JaxmppException {
                ConnectorEvent event = new ConnectorEvent(StreamTerminated, sessionObject);
                this.observable.fireEvent(event.getType(), event);
        }
}
