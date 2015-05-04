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
	 * Implemented by handlers of {@linkplain ConnectedEvent ConnectedEvent}.
	 */
	public interface ConnectedHandler extends EventHandler {

		/**
		 * Fired after creates XMPP Stream
		 */
		public static class ConnectedEvent extends JaxmppEvent<ConnectedHandler> {

			public ConnectedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(ConnectedHandler handler) {
				handler.onConnected(sessionObject);
			}

		}

		/**
		 * Called when {@linkplain ConnectedEvent ConnectedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onConnected(SessionObject sessionObject);
	}

	/**
	 * Implemented by handlers of {@linkplain DisconnectedEvent}.
	 */
	public interface DisconnectedHandler extends EventHandler {

		/**
		 * Fired when Connector is permanently stopped.
		 */
		public static class DisconnectedEvent extends JaxmppEvent<DisconnectedHandler> {

			public DisconnectedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(DisconnectedHandler handler) throws Exception {
				handler.onDisconnected(sessionObject);
			}
		}

		/**
		 * Called when {@linkplain DisconnectedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onDisconnected(SessionObject sessionObject);
	}

	/**
	 * Implemented by handlers of {@linkplain EncryptionEstablishedEvent
	 * EncryptionEstablishedEvent}.
	 */
	public interface EncryptionEstablishedHandler extends EventHandler {

		/**
		 * Fired after encrypted connection is established.
		 */
		public static class EncryptionEstablishedEvent extends JaxmppEvent<EncryptionEstablishedHandler> {

			public EncryptionEstablishedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(EncryptionEstablishedHandler handler) {
				handler.onEncryptionEstablished(sessionObject);
			}

		}

		/**
		 * Called when {@linkplain EncryptionEstablishedEvent
		 * EncryptionEstablishedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onEncryptionEstablished(SessionObject sessionObject);
	}

	/**
	 * Implemented by handlers of {@linkplain ErrorEvent ErrorEvent}.
	 */
	public interface ErrorHandler extends EventHandler {

		/**
		 * Fired on connection error.
		 */
		public static class ErrorEvent extends JaxmppEvent<ErrorHandler> {

			private Throwable caught;

			private StreamError condition;

			public ErrorEvent(SessionObject sessionObject, StreamError condition, Throwable caught) {
				super(sessionObject);
				this.condition = condition;
				this.caught = caught;
			}

			@Override
			public String toString() {
				return "ErrorEvent{" + "caught=" + caught + ", condition=" + condition + '}';
			}

			@Override
			protected void dispatch(ErrorHandler handler) throws JaxmppException {
				handler.onError(sessionObject, condition, caught);
			}

			public Throwable getCaught() {
				return caught;
			}

			public StreamError getCondition() {
				return condition;
			}

		}

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
	}

	/**
	 * Implemented by handlers of {@linkplain StanzaReceivedEvent
	 * StanzaReceivedEvent}.
	 */
	public interface StanzaReceivedHandler extends EventHandler {

		/**
		 * Fired when stanza is received.
		 */
		public static class StanzaReceivedEvent extends JaxmppEvent<StanzaReceivedHandler> {

			private StreamPacket stanza;

			public StanzaReceivedEvent(SessionObject sessionObject, StreamPacket stanza) {
				super(sessionObject);
				this.stanza = stanza;
			}

			@Override
			public String toString() {
				return "StanzaReceivedEvent{" + "stanza=" + stanza + '}';
			}

			@Override
			protected void dispatch(StanzaReceivedHandler handler) {
				handler.onStanzaReceived(sessionObject, stanza);
			}

			public StreamPacket getStanza() {
				return stanza;
			}

		}

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
	}

	/**
	 * Implemented by handlers of {@linkplain StanzaSendingEvent
	 * StanzaSendingEvent}.
	 */
	public interface StanzaSendingHandler extends EventHandler {

		/**
		 * Fired when stanza is sending.
		 */
		public static class StanzaSendingEvent extends JaxmppEvent<StanzaSendingHandler> {

			private Element stanza;

			public StanzaSendingEvent(SessionObject sessionObject, Element stanza) {
				super(sessionObject);
				this.stanza = stanza;
			}

			@Override
			public String toString() {
				return "StanzaSendingEvent{" + "stanza=" + stanza + '}';
			}

			@Override
			protected void dispatch(StanzaSendingHandler handler) throws JaxmppException {
				handler.onStanzaSending(sessionObject, stanza);
			}

			public Element getStanza() {
				return stanza;
			}

			public void setStanza(Element stanza) {
				this.stanza = stanza;
			}

		}

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
	}

	// public final static String DISABLE_SOCKET_TIMEOUT_KEY =
	// "CONNECTOR#DISABLE_SOCKET_TIMEOUT_KEY";

	/**
	 * States of Connector.
	 */
	public static enum State {
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
	 * Implemented by handlers of {@linkplain StateChangedEvent
	 * StateChangedEvent}.
	 */
	public interface StateChangedHandler extends EventHandler {

		/**
		 * Fired after connection state is changed.
		 */
		public static class StateChangedEvent extends JaxmppEvent<StateChangedHandler> {

			private State newState;

			private State oldState;

			public StateChangedEvent(SessionObject sessionObject, State oldState, State newState) {
				super(sessionObject);
				this.oldState = oldState;
				this.newState = newState;
			}

			@Override
			public String toString() {
				return "StateChangedEvent{" + "newState=" + newState + ", oldState=" + oldState + '}';
			}

			@Override
			protected void dispatch(StateChangedHandler handler) throws JaxmppException {
				handler.onStateChanged(sessionObject, oldState, newState);
			}

			public State getNewState() {
				return newState;
			}

			public State getOldState() {
				return oldState;
			}

		}

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
	}

	/**
	 * Implemented by handlers of {@linkplain StreamTerminatedEvent
	 * StreamTerminatedEvent}.
	 */
	public interface StreamTerminatedHandler extends EventHandler {

		/**
		 * Fired after XMPP Stream is terminated.
		 */
		public static class StreamTerminatedEvent extends JaxmppEvent<StreamTerminatedHandler> {

			public StreamTerminatedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(StreamTerminatedHandler handler) throws JaxmppException {
				handler.onStreamTerminated(sessionObject);
			}

		}

		/**
		 * Called when when {@linkplain StreamTerminatedEvent
		 * StreamTerminatedEvent} is fired.
		 *
		 * @param sessionObject
		 *            session object related to connection.
		 */
		void onStreamTerminated(SessionObject sessionObject) throws JaxmppException;
	}

	/**
	 * Name of property that specify if connection is already compressed. <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	public final static String COMPRESSED_KEY = "CONNECTOR#COMPRESSED_KEY";

	/**
	 * Name of property that specify current state of connector. <br/>
	 * Type: {@linkplain State State}.
	 */
	public final static String CONNECTOR_STAGE_KEY = "CONNECTOR#STAGE_KEY";

	/**
	 * Name of property that allows disable keep alive feature. Keep alive is
	 * turned on by default. <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	public final static String DISABLE_KEEPALIVE_KEY = "CONNECTOR#DISABLEKEEPALIVE";

	/**
	 * Name of property that specify if connection is encrypted. <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	public final static String ENCRYPTED_KEY = "CONNECTOR#ENCRYPTED_KEY";

	/**
	 * <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	public final static String EXTERNAL_KEEPALIVE_KEY = "CONNECTOR#EXTERNAL_KEEPALIVE_KEY";

	/**
	 * <br/>
	 * Type: {@linkplain Boolean Boolean}.
	 */
	public static final String SEE_OTHER_HOST_KEY = "BOSH#SEE_OTHER_HOST_KEY";

	/**
	 * Name of property to define
	 * {@linkplain SessionObject#setUserProperty(String, Object) property}.
	 * Custom array of {@link TrustManager TrustManagers[]} instead of dummy
	 * (accepts all certificates) builded in.
	 */
	public static final String TRUST_MANAGERS_KEY = "TRUST_MANAGERS_KEY";

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
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer);

	/**
	 * Returns current {@linkplain State State} of connector.
	 *
	 * @return {@linkplain State State} of connector.
	 */
	public State getState();

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
	public void keepalive() throws JaxmppException;

	/**
	 * Sends new XML Stream header.
	 */
	public void restartStream() throws JaxmppException;

	/**
	 * Sends given XML Element to server.
	 *
	 * @param stanza
	 *            XML element to send.
	 */
	public void send(final Element stanza) throws JaxmppException;

	/**
	 * Starts connector. If connector is properly configured it will tries to
	 * establsh connection with server.
	 */
	public void start() throws JaxmppException;

	/**
	 * Stops connector and closes connections.
	 */
	public void stop() throws JaxmppException;

	/**
	 * Stops connector.
	 *
	 * @param terminate
	 *            if
	 *            <code>true<code> then connection will be terminated immediatelly and connector will be stopped.
	 */
	public void stop(boolean terminate) throws JaxmppException;

}