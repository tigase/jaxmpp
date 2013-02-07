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

import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;

/**
 *
 * @author andrzej
 */
public interface ConnectionManager extends ObservableAware {

        public static interface InitializedCallback {
                
                void initialized(JaxmppCore jaxmpp, ConnectionSession session);
                
        };
        
        public static final EventType CONNECTION_ESTABLISHED = new EventType();
        public static final EventType CONNECTION_CLOSED = new EventType();
        public static final EventType CONNECTION_FAILED = new EventType();
        
        void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback) throws JaxmppException;
        
        void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException;
        void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException;
        
        void addListener(final EventType eventType, Listener<? extends BaseEvent> listener);        
        void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener);
  
}
