/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
package tigase.jaxmpp.j2se.filetransfer;

import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;

/**
 *
 * @author andrzej
 */
public interface FileTransferNegotiator extends ObservableAware {
        
        public static final EventType NEGOTIATION_FAILURE = new EventType();
        public static final EventType NEGOTIATION_REJECTED = new EventType();
        public static final EventType NEGOTIATION_REQUEST = new EventType();
		public static final EventType NEGOTIATION_SUCCESS  =new EventType();

		String[] getFeatures();
		
		boolean isSupported(JaxmppCore jaxmpp, FileTransfer ft);
        void sendFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;
        void acceptFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;
        void rejectFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;
        
        void registerListeners(JaxmppCore jaxmpp);
        void unregisterListeners(JaxmppCore jaxmpp);
}
