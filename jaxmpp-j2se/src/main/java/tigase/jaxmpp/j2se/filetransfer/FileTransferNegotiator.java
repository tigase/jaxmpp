/*
 * FileTransferNegotiator.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.j2se.filetransfer;

import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer;

/**
 * @author andrzej
 */
public interface FileTransferNegotiator
		extends ContextAware {

	void acceptFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;

	String[] getFeatures();

	boolean isSupported(JaxmppCore jaxmpp, FileTransfer ft);

	void registerListeners(JaxmppCore jaxmpp);

	void rejectFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;

	void sendFile(JaxmppCore jaxmpp, FileTransfer ft) throws JaxmppException;

	void unregisterListeners(JaxmppCore jaxmpp);

	interface NegotiationFailureHandler
			extends EventHandler {

		void onFileTransferNegotiationFailure(SessionObject sessionObject, FileTransfer fileTransfer)
				throws JaxmppException;

		class FileTransferNegotiationFailureEvent
				extends JaxmppEvent<NegotiationFailureHandler> {

			private FileTransfer fileTransfer;

			public FileTransferNegotiationFailureEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			public void dispatch(NegotiationFailureHandler handler) throws Exception {
				handler.onFileTransferNegotiationFailure(sessionObject, fileTransfer);
			}
		}
	}

	interface NegotiationRejectHandler
			extends EventHandler {

		void onFileTransferNegotiationReject(SessionObject sessionObject, FileTransfer fileTransfer);

		class FileTransferNegotiationRejectEvent
				extends JaxmppEvent<NegotiationRejectHandler> {

			private FileTransfer fileTransfer;

			public FileTransferNegotiationRejectEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			public void dispatch(NegotiationRejectHandler handler) throws Exception {
				handler.onFileTransferNegotiationReject(sessionObject, fileTransfer);
			}
		}
	}

	interface NegotiationRequestHandler
			extends EventHandler {

		void onFileTransferNegotiationRequest(SessionObject sessionObject, FileTransfer fileTransfer);

		class FileTransferNegotiationRequestEvent
				extends JaxmppEvent<NegotiationRequestHandler> {

			private FileTransfer fileTransfer;

			public FileTransferNegotiationRequestEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			public void dispatch(NegotiationRequestHandler handler) throws Exception {
				handler.onFileTransferNegotiationRequest(sessionObject, fileTransfer);
			}
		}
	}

	interface NegotiationSuccessHandler
			extends EventHandler {

		void onFileTransferNegotiationSuccess(SessionObject sessionObject, FileTransfer fileTransfer);

		class FileTransferNegotiationSuccessEvent
				extends JaxmppEvent<NegotiationSuccessHandler> {

			private FileTransfer fileTransfer;

			public FileTransferNegotiationSuccessEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			public void dispatch(NegotiationSuccessHandler handler) throws Exception {
				handler.onFileTransferNegotiationSuccess(sessionObject, fileTransfer);
			}
		}
	}
}
