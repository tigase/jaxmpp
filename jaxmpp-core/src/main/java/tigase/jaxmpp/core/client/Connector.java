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
package tigase.jaxmpp.core.client;

import javax.net.ssl.TrustManager;

import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Main Connector interface.
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>{@link Connector#Connected Connected}</b> : {@link ConnectorEvent
 * ConnectorEvent} ()<br>
 * <div>Fires after creates XMPP Stream</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link Connector#EncryptionEstablished EncryptionEstablished}</b> :
 * {@link ConnectorEvent ConnectorEvent} ()<br>
 * <div>Fires after encrypted connection is established.</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link Connector#Error Error}</b> : {@link ConnectorEvent
 * ConnectorEvent} (caught)<br>
 * <div>Fires on XMPP Stream error</div>
 * <ul>
 * <li>caught : exception</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>{@link Connector#StateChanged StateChanged}</b> :
 * {@link ConnectorEvent ConnectorEvent} ()<br>
 * <div>Fires after connection state is changed</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link Connector#StanzaReceived StanzaReceived}</b> :
 * {@link ConnectorEvent ConnectorEvent} (stanza)<br>
 * <div>Fires after next stanza is received</div>
 * <ul>
 * <li>stanza : received stanza</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>{@link Connector#StreamTerminated StreamTerminated}</b> :
 * {@link ConnectorEvent ConnectorEvent} ()<br>
 * <div>Fires after XMPP Stream is terminated</div>
 * <ul>
 * </ul></dd>
 * 
 * 
 * <br/>
 * <dt><b>Properties:</b></dt>
 * 
 * <dd><b>{@link Connector#TRUST_MANAGERS_KEY TRUST_MANAGER}</b>: Custom
 * {@link TrustManager TrustManager} instead of dummy (accespts all
 * certificates) builded in.</dd>
 * 
 * 
 * </dl>
 * 
 */
public interface Connector {

	public interface BodyReceivedHandler extends EventHandler {

		public static class BodyReceivedvent extends JaxmppEvent<BodyReceivedHandler> {

			private String receivedData;

			private Element response;

			private int responseCode;

			public BodyReceivedvent(SessionObject sessionObject, int responseCode, Element response, String responseData) {
				super(sessionObject);
				this.responseCode = responseCode;
				this.response = response;
				this.receivedData = responseData;
			}

			@Override
			protected void dispatch(BodyReceivedHandler handler) {
				handler.onBodyReceived(sessionObject, responseCode, response);
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

		void onBodyReceived(SessionObject sessionObject, int responseCode, Element response);
	}

	/**
	 * Event fires after creates XMPP Stream
	 */
	public interface ConnectedHandler extends EventHandler {

		public static class ConnectedEvent extends JaxmppEvent<ConnectedHandler> {

			public ConnectedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(ConnectedHandler handler) {
				handler.onConnected(sessionObject);
			}

		}

		void onConnected(SessionObject sessionObject);
	}

	/**
	 * Event fires after encrypted connection is established.
	 */
	public interface EncryptionEstablishedHandler extends EventHandler {

		public static class EncryptionEstablishedEvent extends JaxmppEvent<EncryptionEstablishedHandler> {

			public EncryptionEstablishedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(EncryptionEstablishedHandler handler) {
				handler.onEncryptionEstablished(sessionObject);
			}

		}

		void onEncryptionEstablished(SessionObject sessionObject);
	}

	/**
	 * Event fires on XMPP Stream error.
	 * <p>
	 * Filled fields:
	 * <ul>
	 * <li>caught : exception</li>
	 * </ul>
	 * </p>
	 */
	public interface ErrorHandler extends EventHandler {

		public static class ErrorEvent extends JaxmppEvent<ErrorHandler> {

			private Throwable caught;

			private StreamError condition;

			public ErrorEvent(SessionObject sessionObject, StreamError condition, Throwable caught) {
				super(sessionObject);
				this.condition = condition;
				this.caught = caught;
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

		void onError(SessionObject sessionObject, StreamError condition, Throwable caught) throws JaxmppException;
	}

	/**
	 * Event fires after creates XMPP Stream.
	 * <p>
	 * Filled fields:
	 * <ul>
	 * <li>{@link ConnectorEvent#getStanza() stanza} : received stanza</li>
	 * </ul>
	 * </p>
	 */
	public interface StanzaReceivedHandler extends EventHandler {

		public static class StanzaReceivedEvent extends JaxmppEvent<StanzaReceivedHandler> {

			private Element stanza;

			public StanzaReceivedEvent(SessionObject sessionObject, Element stanza) {
				super(sessionObject);
				this.stanza = stanza;
			}

			@Override
			protected void dispatch(StanzaReceivedHandler handler) {
				handler.onStanzaReceived(sessionObject, stanza);
			}

			public Element getStanza() {
				return stanza;
			}

		}

		void onStanzaReceived(SessionObject sessionObject, Element stanza);
	}

	public interface StanzaSendingHandler extends EventHandler {

		public static class StanzaSendingEvent extends JaxmppEvent<StanzaSendingHandler> {

			private Element stanza;

			public StanzaSendingEvent(SessionObject sessionObject, Element stanza) {
				super(sessionObject);
				this.stanza = stanza;
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

		void onStanzaSending(SessionObject sessionObject, Element stanza) throws JaxmppException;
	}

	// public final static String DISABLE_SOCKET_TIMEOUT_KEY =
	// "CONNECTOR#DISABLE_SOCKET_TIMEOUT_KEY";

	public static enum State {
		connected,
		connecting,
		disconnected,
		disconnecting
	}

	/**
	 * Event fires after connection state is changed.
	 */
	public interface StateChangedHandler extends EventHandler {

		public static class StateChangedEvent extends JaxmppEvent<StateChangedHandler> {

			private State newState;

			private State oldState;

			public StateChangedEvent(SessionObject sessionObject, State oldState, State newState) {
				super(sessionObject);
				this.oldState = oldState;
				this.newState = newState;
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

		void onStateChanged(SessionObject sessionObject, State oldState, State newState) throws JaxmppException;
	}

	/**
	 * Event fires after XMPP Stream is terminated.
	 */
	public interface StreamTerminatedHandler extends EventHandler {

		public static class StreamTerminatedEvent extends JaxmppEvent<StreamTerminatedHandler> {

			public StreamTerminatedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(StreamTerminatedHandler handler) throws JaxmppException {
				handler.onStreamTerminated(sessionObject);
			}

		}

		void onStreamTerminated(SessionObject sessionObject) throws JaxmppException;
	}

	/**
	 * Key set to true to determine if connection is already compressed
	 */
	public final static String COMPRESSED_KEY = "CONNECTOR#COMPRESSED_KEY";

	public final static String CONNECTOR_STAGE_KEY = "CONNECTOR#STAGE_KEY";

	public final static String DISABLE_KEEPALIVE_KEY = "CONNECTOR#DISABLEKEEPALIVE";

	public final static String ENCRYPTED_KEY = "CONNECTOR#ENCRYPTED_KEY";

	public final static String EXTERNAL_KEEPALIVE_KEY = "CONNECTOR#EXTERNAL_KEEPALIVE_KEY";

	public static final String SEE_OTHER_HOST_KEY = "BOSH#SEE_OTHER_HOST_KEY";

	/**
	 * Key for define {@linkplain SessionObject#setUserProperty(String, Object)
	 * property}. Custom array of {@link TrustManager TrustManagers[]} instead
	 * of dummy (accepts all certificates) builded in.
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

	public State getState();

	boolean isCompressed();

	boolean isSecure();

	/**
	 * Whitespace ping.
	 * 
	 * @throws JaxmppException
	 */
	public void keepalive() throws JaxmppException;

	public void restartStream() throws XMLException, JaxmppException;

	public void send(final Element stanza) throws XMLException, JaxmppException;

	public void start() throws XMLException, JaxmppException;

	public void stop() throws XMLException, JaxmppException;

	public void stop(boolean terminate) throws XMLException, JaxmppException;

}