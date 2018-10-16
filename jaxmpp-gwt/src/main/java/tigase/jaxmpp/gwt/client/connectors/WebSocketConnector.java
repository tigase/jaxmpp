/*
 * WebSocketConnector.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.AbstractWebSocketConnector;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author andrzej
 */
public class WebSocketConnector
		extends AbstractWebSocketConnector {

	private final WebSocketCallback socketCallback;
	private int SOCKET_TIMEOUT = 1000 * 60 * 3;
	private Timer closeTimer;
	private Timer pingTimer = null;
	private WebSocket socket = null;

	public WebSocketConnector(Context context) {
		super(context);

		socketCallback = new WebSocketCallback() {
			@Override
			public void onClose(WebSocket ws) {
				try {
					if (getState() == State.connected && !rfcCompatible && StreamFeaturesModule.getStreamFeatures(
							WebSocketConnector.this.context.getSessionObject()) == null) {
						if (pingTimer != null) {
							pingTimer.cancel();
							pingTimer = null;
						}
						rfcCompatible = true;
						start();
						return;
					}
					if (getState() != State.disconnected && getState() != State.disconnecting) {
						if (pingTimer != null) {
							pingTimer.cancel();
							pingTimer = null;
						}
						fireOnError(null, null, WebSocketConnector.this.context.getSessionObject());
					} else {
						onStreamTerminate();
					}
				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				} finally {
					workerTerminated();
				}
			}

			@Override
			public void onError(WebSocket ws) {
				log.warning("received WebSocket error - terminating");
				try {
					if (pingTimer != null) {
						pingTimer.cancel();
						pingTimer = null;
					}
					fireOnError(null, null, WebSocketConnector.this.context.getSessionObject());
				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				}
			}

			@Override
			public void onMessage(WebSocket ws, String message) {
				try {
					if (getState() == State.disconnected) {
						return;
					}
					parseSocketData(message);
				} catch (JaxmppException ex) {
					WebSocketConnector.this.onError(null, ex);
				}
			}

			@Override
			public void onOpen(WebSocket ws) {
				try {
					if ("xmpp-framing".equals(ws.getProtocol())) {
						rfcCompatible = true;
					}
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

					if (WebSocketConnector.this.context.getSessionObject().getProperty(EXTERNAL_KEEPALIVE_KEY) ==
							null || ((Boolean) WebSocketConnector.this.context.getSessionObject()
							.getProperty(EXTERNAL_KEEPALIVE_KEY) == false)) {
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
	public boolean isSecure() {
		return socket.isSecure();
	}

	@Override
	protected void onStreamTerminate() throws JaxmppException {
		super.onStreamTerminate();
		if (getState() == State.disconnecting || getState() == State.disconnected) {
			socket.close();
			workerTerminated();
		}
	}

	private void parseSocketData(String x) throws JaxmppException {
		// ignore keep alive "whitespace"
		if (x == null || x.length() == 1) {
			x = x.trim();
			if (x.length() == 0) {
				return;
			}
		}

		log.finest("received = " + x);

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
		} else {
			x = "<root>" + x + "</root>";
		}

		Element response = new GwtElement(XMLParser.parse(x).getDocumentElement());
		List<Element> received = null;
		if ("stream:stream".equals(response.getName()) || "stream".equals(response.getName()) ||
				"root".equals(response.getName())) {
			received = response.getChildren();
		} else if (response != null) {
			received = new ArrayList<Element>();
			received.add(response);
		}

		if (received != null) {
			for (Element child : received) {
				if ("parsererror".equals(child.getName())) {
					continue;
				}

				processElement(child);
			}
		}
	}

	@Override
	public void send(final String data) throws JaxmppException {
		//if (getState() == State.connected) {
		try {
			socket.send(data);
		} catch (Throwable ex) {
			throw new JaxmppException(ex);
		}
		//} else {
		//	throw new JaxmppException("Not connected");
		//}
	}

	@Override
	public void start() throws JaxmppException {
		super.start();

		String url = context.getSessionObject().getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
		setStage(State.connecting);
		// maybe we should add other "protocols" to indicate which version of xmpp-over-websocket is used?
		// if we would ask for "xmpp" and "xmpp-framing" new server would (at least Tigase) would respond
		// with "xmpp-framing" to try newer version of protocol and older with "xmpp" suggesting to try
		// older protocol at first but in both cases we should be ready for error and failover!!
		// This would only reduce number of roundtrips in case of an error.
		// WARNING: old Tigase will not throw an exception when new protocol is tried - it will just hang.
		// Good idea would be also to allow user to pass protocol version (new, old, autodetection [old and if failed try the new one])
		try {
			socket = new WebSocket(url, new String[]{"xmpp", "xmpp-framing"}, socketCallback);
		} catch (Exception ex) {
			setStage(State.disconnected);
			throw ex;
		}
	}

	@Override
	public void stop() throws JaxmppException {
		super.stop();
		//context.getEventBus().fire(new DisconnectedHandler.DisconnectedEvent(context.getSessionObject()));
	}

	@Override
	protected void terminateAllWorkers() throws JaxmppException {
		if (this.pingTimer != null) {
			this.pingTimer.cancel();
			this.pingTimer = null;
		}

		if (closeTimer != null) {
			closeTimer.cancel();
		}
		final WebSocket socket = this.socket;
		closeTimer = new Timer() {
			@Override
			public void run() {
				socket.close();
				workerTerminated();
			}
		};
		closeTimer.schedule(3 * 1000);
		//setStage(State.disconnected);
		//socket.close();
		//super.terminateAllWorkers();
	}

	protected void workerTerminated() {
		try {
			if (closeTimer != null) {
				closeTimer.cancel();
				closeTimer = null;
			}
			setStage(State.disconnected);
		} catch (JaxmppException ex) {
		}
		context.getEventBus().fire(new DisconnectedHandler.DisconnectedEvent(context.getSessionObject()));
	}
}
