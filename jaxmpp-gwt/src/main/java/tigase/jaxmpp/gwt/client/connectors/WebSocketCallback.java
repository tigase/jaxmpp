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

/**
 *
 * @author andrzej
 */
public interface WebSocketCallback {

        /**
         * Method called when WebSocket opens connection
         */
        public void onOpen(WebSocket ws);

        /**
         * Method called when WebSocket receives message
         */
        public void onMessage(WebSocket ws, String message);

        /**
         * Method called when WebSocket receives error
         */
        public void onError(WebSocket ws);

        /**
         * Method called when WebSocket connection is closed
         */
        public void onClose(WebSocket ws);
        
}
