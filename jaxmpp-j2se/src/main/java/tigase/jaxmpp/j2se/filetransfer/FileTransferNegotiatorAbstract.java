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
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;

/**
 *
 * @author andrzej
 */
public abstract class FileTransferNegotiatorAbstract implements FileTransferNegotiator {

	private final Logger log;
	protected FileTransferManager ftManager = null;
	protected Context context = null;

	public FileTransferNegotiatorAbstract() {
		log = Logger.getLogger(this.getClass().getCanonicalName());
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	protected void fireOnRequest(SessionObject sessionObject, tigase.jaxmpp.j2se.filetransfer.FileTransfer fileTransfer) {
		fileTransfer.setNegotiator(this);
		context.getEventBus().fire(new FileTransferNegotiator.NegotiationRequestHandler.FileTransferNegotiationRequestEvent(fileTransfer.getSessionObject(), fileTransfer));
	}

	protected void fireOnSuccess(FileTransfer ft) {
		log.log(Level.FINER, "firing file transfer negotiation success");
		context.getEventBus().fire(new FileTransferNegotiator.NegotiationSuccessHandler.FileTransferNegotiationSuccessEvent(ft.getSessionObject(), ft));
	}

	protected void fireOnFailure(FileTransfer ft, Throwable ex) {
		log.log(Level.FINER, "firing file transfer negotiation error", ex);
		context.getEventBus().fire(new FileTransferNegotiator.NegotiationFailureHandler.FileTransferNegotiationFailureEvent(ft.getSessionObject(), ft));
	}

	protected void fireOnReject(FileTransfer ft) {
		log.log(Level.FINER, "firing file transfer rejected {0}", ft.toString());
		context.getEventBus().fire(new FileTransferNegotiator.NegotiationRejectHandler.FileTransferNegotiationRejectEvent(ft.getSessionObject(), ft));
	}
}
