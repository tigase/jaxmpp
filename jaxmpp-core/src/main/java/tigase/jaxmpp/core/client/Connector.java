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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.StreamPacket;

/**
 * Main Connector interface.
 */
public interface Connector {

	/**
	 * Name of property that specify if connection is already compressed. <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	String COMPRESSED_KEY = "CONNECTOR#COMPRESSED_KEY";
	/**
	 * Name of property that specify current state of connector. <br/>
	 * Type: {@linkplain State State}.
	 */
	String CONNECTOR_STAGE_KEY = "CONNECTOR#STAGE_KEY";

	/**
	 * Name of property that specify timestamp (type: {@link java.util.Date}) of
	 * last state change.
	 */
	String CONNECTOR_STAGE_TIMESTAMP_KEY = "CONNECTOR#CONNECTOR_STAGE_TIMESTAMP_KEY";

	/**
	 * Name of property that allows disable keep alive feature. Keep alive is
	 * turned on by default. <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	String DISABLE_KEEPALIVE_KEY = "CONNECTOR#DISABLEKEEPALIVE";
	/**
	 * Name of property that specify if connection is encrypted. <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	String ENCRYPTED_KEY = "CONNECTOR#ENCRYPTED_KEY";
	/**
	 * <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	String EXTERNAL_KEEPALIVE_KEY = "CONNECTOR#EXTERNAL_KEEPALIVE_KEY";

	String RECONNECTING_KEY = "s:reconnecting";

	/**
	 * <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	String SEE_OTHER_HOST_KEY = "BOSH#SEE_OTHER_HOST_KEY";

	// public final static String DISABLE_SOCKET_TIMEOUT_KEY =
	// "CONNECTOR#DISABLE_SOCKET_TIMEOUT_KEY";
	/**
	 * Name of property to define
	 * {@linkplain SessionObject#setUserProperty(String, Object) property}.
	 * Custom array of {@link javax.net.ssl.TrustManager TrustManagers[]}
	 * instead of default Java TrustManager.
	 */
	String TRUST_MANAGERS_KEY = "TRUST_MANAGERS_KEY";

	String PROXY_HOST = "PROXY_HOST_KEY";

	String PROXY_PORT = "PROXY_PORT_KEY";

	String PROXY_TYPE = "PROXY_TYPE_KEY";

	/**
	 * Returns instance of {@linkplain XmppSessionLogic} to work with this
	 * connector.
	 *
	 * @param modulesManager
	 *            module manager
	 * @param writer
	 *            writer
	 * @return {@linkplain XmppSessionLogic}
	 */
	XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer);

	/**
	 * Returns current {@linkplain State State} of connector.
	 *
	 * @return {@linkplain State State} of connector.
	 */
	State getState();

	/**
	 * Returns XML Stream compression state.
	 *
	 * @return <code>true> if XML Stream is compressed.
	 */
	boolean isCompressed();

	/**
	 * Returns connection security state.
	 *
	 * @return <code>true> if connection is secured and encrypted.
	 */
	boolean isSecure();

	/**
	 * Whitespace ping.
	 */
	void keepalive() throws JaxmppException;

	/**
	 * Sends new XML Stream header.
	 */
	void restartStream() throws JaxmppException;

	/**
	 * Sends given XML Element to server.
	 *
	 * @param stanza
	 *            XML element to send.
	 */
	void send(final Element stanza) throws JaxmppException;

	/**
	 * Starts connector. If connector is properly configured it will tries to
	 * establsh connection with server.
	 */
	void start() throws JaxmppException;

	/**
	 * Stops connector and closes connections.
	 */
	void stop() throws JaxmppException;

	/**
	 * Stops connector.
	 *
	 * @param terminate
	 *            if <code>true<code> then connection will be terminated
	 *            immediatelly and connector will be stopped.
	 */
	void stop(boolean terminate) throws JaxmppException;

	/**
	 * States of Connector.
	 */
	enum State {
		/**
		 * Connection is established.
		 */
		connected,
		/**
		 * Connector started establishing connection.
		 */
		connecting,
		/**
		 * Connector is disconnected.
		 */
		disconnected,
		/**
		 * Connector is closing connection and stopping workers.
		 */
		disconnecting
	}

	/**
	 * Implemented by handlers of {@linkplain ConnectedEvent LoggedInEvent}.
	 */
	interface ConnectedHandler extends EventHandler {

		/**
		 * Called when {@linkplain ConnectedEvent LoggedInEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onConnected(SessionObject sessionObject);

		/**
		 * Fired after creates XMPP Stream
		 */
		class ConnectedEvent extends JaxmppEvent<ConnectedHandler> {

			public ConnectedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(ConnectedHandler handler) {
				handler.onConnected(sessionObject);
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain DisconnectedEvent}.
	 */
	interface DisconnectedHandler extends EventHandler {

		/**
		 * Called when {@linkplain DisconnectedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onDisconnected(SessionObject sessionObject);

		/**
		 * Fired when Connector is permanently stopped.
		 */
		class DisconnectedEvent extends JaxmppEvent<DisconnectedHandler> {

			public DisconnectedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(DisconnectedHandler handler) throws Exception {
				handler.onDisconnected(sessionObject);
			}
		}
	}

	/**
	 * Implemented by handlers of {@linkplain EncryptionEstablishedEvent
	 * EncryptionEstablishedEvent}.
	 */
	interface EncryptionEstablishedHandler extends EventHandler {

		/**
		 * Called when {@linkplain EncryptionEstablishedEvent
		 * EncryptionEstablishedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onEncryptionEstablished(SessionObject sessionObject);

		/**
		 * Fired after encrypted connection is established.
		 */
		class EncryptionEstablishedEvent extends JaxmppEvent<EncryptionEstablishedHandler> {

			public EncryptionEstablishedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(EncryptionEstablishedHandler handler) {
				handler.onEncryptionEstablished(sessionObject);
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain ErrorEvent ErrorEvent}.
	 */
	interface ErrorHandler extends EventHandler {

		/**
		 * Called when {@linkplain ErrorEvent ErrorEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 * @param condition
		 *            XMPP error condition. <code>null</code> if error is not
		 *            caused by stream.
		 * @param caught
		 *            exception. <cude>null</code> if error was caused by
		 *            stream.
		 */
		void onError(SessionObject sessionObject, StreamError condition, Throwable caught) throws JaxmppException;

		/**
		 * Fired on connection error.
		 */
		class ErrorEvent extends JaxmppEvent<ErrorHandler> {

			private Throwable caught;

			private StreamError condition;

			public ErrorEvent(SessionObject sessionObject, StreamError condition, Throwable caught) {
				super(sessionObject);
				this.condition = condition;
				this.caught = caught;
			}

			@Override
			public void dispatch(ErrorHandler handler) throws JaxmppException {
				handler.onError(sessionObject, condition, caught);
			}

			public Throwable getCaught() {
				return caught;
			}

			public StreamError getCondition() {
				return condition;
			}

			@Override
			public String toString() {
				return "ErrorEvent{" + "condition=" + condition + ", sessionObject=" + sessionObject + ", caught=" + caught
						+ '}';
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain StanzaReceivedEvent
	 * StanzaReceivedEvent}.
	 */
	interface StanzaReceivedHandler extends EventHandler {

		/**
		 * Called when {@linkplain StanzaReceivedEvent StanzaReceivedEvent} is
		 * fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 * @param stanza
		 *            received stanza.
		 */
		void onStanzaReceived(SessionObject sessionObject, StreamPacket stanza);

		/**
		 * Fired when stanza is received.
		 */
		class StanzaReceivedEvent extends JaxmppEvent<StanzaReceivedHandler> {

			private StreamPacket stanza;

			public StanzaReceivedEvent(SessionObject sessionObject, StreamPacket stanza) {
				super(sessionObject);
				this.stanza = stanza;
			}

			@Override
			public void dispatch(StanzaReceivedHandler handler) {
				handler.onStanzaReceived(sessionObject, stanza);
			}

			public StreamPacket getStanza() {
				return stanza;
			}

			@Override
			public String toString() {
				return "StanzaReceivedEvent{" + "stanza=" + stanza + '}';
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain StanzaSendingEvent
	 * StanzaSendingEvent}.
	 */
	interface StanzaSendingHandler extends EventHandler {

		/**
		 * Called when {@linkplain StanzaSendingEvent StanzaSendingEvent} is
		 * fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 * @param stanza
		 *            stanza to be sent.
		 */
		void onStanzaSending(SessionObject sessionObject, Element stanza) throws JaxmppException;

		/**
		 * Fired when stanza is sending.
		 */
		class StanzaSendingEvent extends JaxmppEvent<StanzaSendingHandler> {

			private Element stanza;

			public StanzaSendingEvent(SessionObject sessionObject, Element stanza) {
				super(sessionObject);
				this.stanza = stanza;
			}

			@Override
			public void dispatch(StanzaSendingHandler handler) throws JaxmppException {
				handler.onStanzaSending(sessionObject, stanza);
			}

			public Element getStanza() {
				return stanza;
			}

			public void setStanza(Element stanza) {
				this.stanza = stanza;
			}

			@Override
			public String toString() {
				return "StanzaSendingEvent{" + "stanza=" + stanza + '}';
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain StateChangedEvent
	 * StateChangedEvent}.
	 */
	interface StateChangedHandler extends EventHandler {

		/**
		 * Called when {@linkplain StateChangedEvent StateChangedEvent} is
		 * fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 * @param oldState
		 *            previous connector state.
		 * @param newState
		 *            new connector state.
		 */
		void onStateChanged(SessionObject sessionObject, State oldState, State newState) throws JaxmppException;

		/**
		 * Fired after connection state is changed.
		 */
		class StateChangedEvent extends JaxmppEvent<StateChangedHandler> {

			private State newState;

			private State oldState;

			public StateChangedEvent(SessionObject sessionObject, State oldState, State newState) {
				super(sessionObject);
				this.oldState = oldState;
				this.newState = newState;
			}

			@Override
			public void dispatch(StateChangedHandler handler) throws JaxmppException {
				handler.onStateChanged(sessionObject, oldState, newState);
			}

			public State getNewState() {
				return newState;
			}

			public State getOldState() {
				return oldState;
			}

			@Override
			public String toString() {
				return "StateChangedEvent{" + "oldState=" + oldState + ", newState=" + newState + ", sessionObject="
						+ sessionObject + '}';
			}

		}
	}

	/**
	 * Implemented by handlers of {@linkplain StreamTerminatedEvent
	 * StreamTerminatedEvent}.
	 */
	interface StreamTerminatedHandler extends EventHandler {

		/**
		 * Called when when {@linkplain StreamTerminatedEvent
		 * StreamTerminatedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onStreamTerminated(SessionObject sessionObject) throws JaxmppException;

		/**
		 * Fired after XMPP Stream is terminated.
		 */
		class StreamTerminatedEvent extends JaxmppEvent<StreamTerminatedHandler> {

			public StreamTerminatedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(StreamTerminatedHandler handler) throws JaxmppException {
				handler.onStreamTerminated(sessionObject);
			}

		}
	}

}