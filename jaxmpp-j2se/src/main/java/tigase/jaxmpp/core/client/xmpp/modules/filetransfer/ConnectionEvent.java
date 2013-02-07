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
package tigase.jaxmpp.core.client.xmpp.modules.filetransfer;

import java.net.Socket;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;

/**
 *
 * @author andrzej
 */
public class ConnectionEvent extends BaseEvent {
        
        private final ConnectionSession session;
        private Socket socket = null;
        
        public ConnectionEvent(EventType type, SessionObject sessionObject, ConnectionSession session, Socket socket) {
                this(type, sessionObject, session);
                this.socket = socket;
        }        
        
        public ConnectionEvent(EventType type, SessionObject sessionObject, ConnectionSession session) {
                super(type, sessionObject);
                this.session = session;                
        }
        
        public Socket getSocket() {
                return socket;
        }
        
        public ConnectionSession getConnectionSession() {
                return session;
        }
}
