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
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;

/**
 *
 * @author andrzej
 */
public interface FileTransferNegotiator extends ContextAware {
        
	public interface NegotiationFailureHandler extends EventHandler {
		
		public static class FileTransferNegotiationFailureEvent extends JaxmppEvent<NegotiationFailureHandler> {

			private FileTransfer fileTransfer;
			
			public FileTransferNegotiationFailureEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}
			
			@Override
			protected void dispatch(NegotiationFailureHandler handler) throws Exception {
				handler.onFileTransferNegotiationFailure(sessionObject, fileTransfer);
			}
			
		}		
		
		void onFileTransferNegotiationFailure(SessionObject sessionObject, FileTransfer fileTransfer) throws JaxmppException;
		
	}

	public interface NegotiationRejectHandler extends EventHandler {
		
		public static class FileTransferNegotiationRejectEvent extends JaxmppEvent<NegotiationRejectHandler> {

			private FileTransfer fileTransfer;
			
			public FileTransferNegotiationRejectEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}
			
			@Override
			protected void dispatch(NegotiationRejectHandler handler) throws Exception {
				handler.onFileTransferNegotiationReject(sessionObject, fileTransfer);
			}
			
		}		
		
		void onFileTransferNegotiationReject(SessionObject sessionObject, FileTransfer fileTransfer);
		
	}

	public interface NegotiationRequestHandler extends EventHandler {
		
		public static class FileTransferNegotiationRequestEvent extends JaxmppEvent<NegotiationRequestHandler> {

			private FileTransfer fileTransfer;
			
			public FileTransferNegotiationRequestEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}
			
			@Override
			protected void dispatch(NegotiationRequestHandler handler) throws Exception {
				handler.onFileTransferNegotiationRequest(sessionObject, fileTransfer);
			}
			
		}		
		
		void onFileTransferNegotiationRequest(SessionObject sessionObject, FileTransfer fileTransfer);
		
	}
	
	public interface NegotiationSuccessHandler extends EventHandler {
		
		public static class FileTransferNegotiationSuccessEvent extends JaxmppEvent<NegotiationSuccessHandler> {

			private FileTransfer fileTransfer;
			
			public FileTransferNegotiationSuccessEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}
			
			@Override
			protected void dispatch(NegotiationSuccessHandler handler) throws Exception {
				handler.onFileTransferNegotiationSuccess(sessionObject, fileTransfer);
			}
			
		}		
		
		void onFileTransferNegotiationSuccess(SessionObject sessionObject, FileTransfer fileTransfer);
		
	}	

		String[] getFeatures();
		
		boolean isSupported(JaxmppCore jaxmpp, FileTransfer ft);
        void sendFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;
        void acceptFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;
        void rejectFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;
        
        void registerListeners(JaxmppCore jaxmpp);
        void unregisterListeners(JaxmppCore jaxmpp);
}
