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

/**
 *
 * @author andrzej
 */
public class WebSocket {
          
        public static native boolean isSupported() /*-{
             return $wnd.WebSocket != undefined;   
        }-*/;
  
        private JavaScriptObject jsWebSocket = null;
        private WebSocketCallback callback = null;
        
        public WebSocket(String url, String protocol, WebSocketCallback callback) {
                this.callback = callback;
                this.jsWebSocket = createJSWebSocket(url, protocol, this);
        }

        public native void send(String message) /*-{
                if (!message) return;
                this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.send(message);
        }-*/;
        
        public void close() {
               callback = null;
               closeInternal(); 
        }
        
        private native void closeInternal() /*-{
                this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.close();
        }-*/;

        public native String getURL() /*-{
                return this.@tigase.jaxmpp.gwt.client.connectors.WebSocket::jsWebSocket.url;
        }-*/;
       
        private native JavaScriptObject createJSWebSocket(final String url, final String protocol, final WebSocket webSocket) /*-{
                var jsWebSocket = new WebSocket(url, protocol);
                
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
        
        private void onOpen() {
                if (callback != null) {
                        callback.onOpen(this);
                }
        }
        
        private void onMessage(String message) {
                if (callback != null) {
                        callback.onMessage(this, message);
                }
        }
        
        private void onError() {
                if (callback != null) {
                        callback.onError(this);
                }
        }
        
        private void onClose() {
                if (callback != null) {
                        callback.onClose(this);
                }
        }        
}
