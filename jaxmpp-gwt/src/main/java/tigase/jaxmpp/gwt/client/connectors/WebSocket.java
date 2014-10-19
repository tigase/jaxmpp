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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author andrzej
 */
public class WebSocket {

	public static native boolean isSupported() /*-{
												return $wnd.WebSocket != undefined;   
												}-*/;

	private WebSocketCallback callback = null;
	private JavaScriptObject jsWebSocket = null;

	public WebSocket(String url, String protocol, WebSocketCallback callback) {
		this.callback = callback;
		JsArrayString jsProtocols = (JsArrayString) JsArrayString.createArray();
		if (protocol != null) {
			jsProtocols.push(protocol);
		}
		this.jsWebSocket = createJSWebSocket(url, jsProtocols, this);
	}

	public WebSocket(String url, String[] protocols, WebSocketCallback callback) {
		this.callback = callback;
		JsArrayString jsProtocols = (JsArrayString) JsArrayString.createArray();
		if (protocols != null) {
			for (String protocol : protocols) {
				jsProtocols.push(protocol);
			}
		}
		this.jsWebSocket = createJSWebSocket(url, jsProtocols, this);
	}	
	
	public void close() {
		callback = null;
		closeInternal();
	}

	private native void closeInternal() /*-{
										this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.close();
										}-*/;

	private native JavaScriptObject createJSWebSocket(final String url, final JsArrayString protocols, final WebSocket webSocket) /*-{
																															var jsWebSocket = new WebSocket(url, protocols);
																															
																															jsWebSocket.onopen = function() {
																															webSocket.@tigase.jaxmpp.gwt.client.connectors.WebSocket::onOpen()();
																															}
																															
																															jsWebSocket.onclose = function() {
																															webSocket.@tigase.jaxmpp.gwt.client.connectors.WebSocket::onClose()();
																															}
																															
																															jsWebSocket.onerror = function() {
																															webSocket.@tigase.jaxmpp.gwt.client.connectors.WebSocket::onError()();
																															}
																															
																															jsWebSocket.onmessage = function(socketResponse) {
																															if (socketResponse.data) {
																															webSocket.@tigase.jaxmpp.gwt.client.connectors.WebSocket::onMessage(Ljava/lang/String;)(socketResponse.data);
																															}
																															}
																															
																															return jsWebSocket;
																															}-*/;

	public native String getURL() /*-{
									return this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.url;
									}-*/;

	private void onClose() {
		if (callback != null) {
			callback.onClose(this);
		}
	}

	private void onError() {
		if (callback != null) {
			callback.onError(this);
		}
	}

	private void onMessage(String message) {
		if (callback != null) {
			try {
				callback.onMessage(this, message);
			} catch (Exception ex) {
				Logger.getLogger("WebSocket").log(Level.SEVERE, "exception processing message = " + message);
			}
		}
	}

	private void onOpen() {
		if (callback != null) {
			callback.onOpen(this);
		}
	}

	public native String getProtocol() /*-{
										return this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.protocol;
									}-*/;
	
	public native void send(String message) /*-{
											if (!message) return;
											this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.send(message);
											}-*/;
}
