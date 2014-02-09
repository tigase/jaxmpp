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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.Property;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferModule;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule;
import tigase.jaxmpp.core.client.xmpp.modules.socks5.Socks5BytestreamsModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.j2se.J2SECapabiliesCache;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
import tigase.jaxmpp.j2se.connection.socks5bytestream.J2SEStreamhostsResolver;
import tigase.jaxmpp.j2se.connection.socks5bytestream.StreamhostsResolver;

/**
 * 
 * @author andrzej
 */
public class FileTransferManager implements ContextAware, FileTransferNegotiator.NegotiationFailureHandler,
		FileTransferNegotiator.NegotiationRejectHandler, FileTransferNegotiator.NegotiationRequestHandler,
		ConnectionManager.ConnectionEstablishedHandler, Property {

	public interface FileTransferFailureHandler extends EventHandler {

		public static class FileTransferFailureEvent extends JaxmppEvent<FileTransferFailureHandler> {

			private FileTransfer fileTransfer;

			public FileTransferFailureEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			protected void dispatch(FileTransferFailureHandler handler) throws Exception {
				handler.onFileTransferFailure(sessionObject, fileTransfer);
			}
		}

		void onFileTransferFailure(SessionObject sessionObject, FileTransfer fileTransfer);
	}

	public interface FileTransferProgressHandler extends EventHandler {

		public static class FileTransferProgressEvent extends JaxmppEvent<FileTransferProgressHandler> {

			private FileTransfer fileTransfer;

			public FileTransferProgressEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			protected void dispatch(FileTransferProgressHandler handler) throws Exception {
				handler.onFileTransferProgress(sessionObject, fileTransfer);
			}
		}

		void onFileTransferProgress(SessionObject sessionObject, FileTransfer fileTransfer);
	}

	public interface FileTransferRejectedHandler extends EventHandler {

		public static class FileTransferRejectedEvent extends JaxmppEvent<FileTransferRejectedHandler> {

			private FileTransfer fileTransfer;

			public FileTransferRejectedEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			protected void dispatch(FileTransferRejectedHandler handler) throws Exception {
				handler.onFileTransferRejected(sessionObject, fileTransfer);
			}
		}

		void onFileTransferRejected(SessionObject sessionObject, FileTransfer fileTransfer);
	}

	public interface FileTransferRequestHandler extends EventHandler {

		public static class FileTransferRequestEvent extends JaxmppEvent<FileTransferRequestHandler> {

			private FileTransfer fileTransfer;

			public FileTransferRequestEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			protected void dispatch(FileTransferRequestHandler handler) throws Exception {
				handler.onFileTransferRequest(sessionObject, fileTransfer);
			}
		}

		void onFileTransferRequest(SessionObject sessionObject, FileTransfer fileTransfer);
	}

	public interface FileTransferSuccessHandler extends EventHandler {

		public static class FileTransferSuccessEvent extends JaxmppEvent<FileTransferSuccessHandler> {

			private FileTransfer fileTransfer;

			public FileTransferSuccessEvent(SessionObject sessionObject, FileTransfer fileTransfer) {
				super(sessionObject);
				this.fileTransfer = fileTransfer;
			}

			@Override
			protected void dispatch(FileTransferSuccessHandler handler) throws Exception {
				handler.onFileTransferSuccess(sessionObject, fileTransfer);
			}
		}

		void onFileTransferSuccess(SessionObject sessionObject, FileTransfer fileTransfer);
	}

	private static final Logger log = Logger.getLogger(FileTransferManager.class.getCanonicalName());

	static {
		UniversalFactory.setSpi(StreamhostsResolver.class.getCanonicalName(),
				new UniversalFactory.FactorySpi<J2SEStreamhostsResolver>() {
					@Override
					public J2SEStreamhostsResolver create() {
						return new J2SEStreamhostsResolver();
					}
				});
	}

	public static void initialize(JaxmppCore jaxmpp, boolean experimental) {
		CapabilitiesModule capsModule = jaxmpp.getModule(CapabilitiesModule.class);
		if (capsModule != null && capsModule.getCache() == null) {
			capsModule.setCache(new J2SECapabiliesCache());
		}

		FileTransferManager fileTransferManager = new FileTransferManager();
		fileTransferManager.setContext(jaxmpp.getContext());
		fileTransferManager.setJaxmpp(jaxmpp);

		jaxmpp.getModulesManager().register(new FileTransferModule(jaxmpp.getContext()));
		jaxmpp.getModulesManager().register(new Socks5BytestreamsModule(jaxmpp.getContext()));

		if (experimental) {
			jaxmpp.getModulesManager().register(new JingleModule(jaxmpp.getContext()));
			fileTransferManager.addNegotiator(new JingleFileTransferNegotiator());
		}
		fileTransferManager.addNegotiator(new Socks5FileTransferNegotiator());
	}
	
	protected static String getCapsNode(Presence presence) throws XMLException {
		if (presence == null) {
			return null;
		}
		Element c = presence.getChildrenNS("c", "http://jabber.org/protocol/caps");
		if (c == null) {
			return null;
		}

		String node = c.getAttribute("node");
		String ver = c.getAttribute("ver");
		if (node == null || ver == null) {
			return null;
		}

		return node + "#" + ver;
	}

	protected Context context = null;

	private JaxmppCore jaxmpp = null;
	private final List<FileTransferNegotiator> negotiators = new ArrayList<FileTransferNegotiator>();

	public void acceptFile(FileTransfer ft) throws JaxmppException {
		ft.setIncoming(true);

		ft.getNegotiator().acceptFile(jaxmpp, ft);
	}

	public void addNegotiator(FileTransferNegotiator negotiator) {
		negotiator.setContext(context);
		negotiator.registerListeners(jaxmpp);
		negotiators.add(negotiator);
	}

	private void fireOnFailure(final FileTransfer ft) {
		context.getEventBus().fire(new FileTransferFailureHandler.FileTransferFailureEvent(ft.getSessionObject(), ft));
	}

	private void fireOnProgress(final FileTransfer ft) {
		context.getEventBus().fire(new FileTransferProgressHandler.FileTransferProgressEvent(ft.getSessionObject(), ft));
	}

	private void fireOnSuccess(final FileTransfer ft) {
		context.getEventBus().fire(new FileTransferSuccessHandler.FileTransferSuccessEvent(ft.getSessionObject(), ft));
	}

	private String generateSid() {
		return UUID.randomUUID().toString();
	}

	@Override
	public Class<FileTransferManager> getPropertyClass() {
		return FileTransferManager.class;
	}
	
	@Override
	public void onConnectionEstablished(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket)
			throws JaxmppException {
		if (socket == null) {
			// this should not happen
			throw new JaxmppException("SOCKET IS NULL");
		}

		if (!(connectionSession instanceof FileTransfer)) {
			// we ignore this as it may be handled by other handler
			return;
		}

		FileTransfer fileTransfer = (FileTransfer) connectionSession;
		if (fileTransfer.isIncoming()) {
			startReceiving(fileTransfer, socket);
		} else {
			startSending(fileTransfer, socket);
		}
	}

	@Override
	public void onFileTransferNegotiationFailure(SessionObject sessionObject,
			tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer fileTransfer) throws JaxmppException {
		FileTransfer ft = (FileTransfer) fileTransfer;
		if (!ft.isIncoming()) {
			FileTransferNegotiator oldNegotiator = ft.getNegotiator();
			boolean start = false;
			for (FileTransferNegotiator negotiator : negotiators) {
				if (negotiator == oldNegotiator) {
					start = true;
					continue;
				} else if (!start) {
					continue;
				} else if (negotiator.isSupported(jaxmpp, ft)) {
					ft.setNegotiator(negotiator);
					negotiator.sendFile(jaxmpp, ft);
					return;
				}
			}
		}
		fireOnFailure(ft);
	}

	@Override
	public void onFileTransferNegotiationReject(SessionObject sessionObject,
			tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer fileTransfer) {
		context.getEventBus().fire(
				new FileTransferRejectedHandler.FileTransferRejectedEvent(sessionObject, (FileTransfer) fileTransfer));
	}

	@Override
	public void onFileTransferNegotiationRequest(SessionObject sessionObject,
			tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransfer fileTransfer) {
		context.getEventBus().fire(
				new FileTransferRequestHandler.FileTransferRequestEvent(sessionObject, (FileTransfer) fileTransfer));
	}

	public void rejectFile(FileTransfer ft) throws JaxmppException {
		ft.setIncoming(true);

		ft.getNegotiator().rejectFile(jaxmpp, ft);
	}

	public void removeNegotiator(FileTransferNegotiator negotiator) {
		negotiators.remove(negotiator);
		negotiator.unregisterListeners(jaxmpp);
		negotiator.setContext(context);
	}

	private FileTransfer sendFile(FileTransfer ft) throws JaxmppException {
		boolean send = false;
		for (FileTransferNegotiator negotiator : negotiators) {
			if (negotiator.isSupported(jaxmpp, ft)) {
				ft.setNegotiator(negotiator);
				negotiator.sendFile(jaxmpp, ft);
				send = true;
				break;
			}
		}

		if (!send) {
			throw new JaxmppException("No file transfer methods supported by recipient = " + ft.getPeer().toString());
		}

		return ft;
	}

	public FileTransfer sendFile(JID peer, File file) throws JaxmppException {
		FileTransfer ft = new FileTransfer(jaxmpp.getSessionObject(), peer, generateSid());
		ft.setIncoming(false);
		ft.setFileInfo(file.getName(), file.length(), new Date(file.lastModified()), null);
		ft.setFile(file);

		return sendFile(ft);
	}

	public FileTransfer sendFile(JID peer, String filename, long fileSize, InputStream is, Date lastModified)
			throws JaxmppException {
		FileTransfer ft = new FileTransfer(jaxmpp.getSessionObject(), peer, generateSid());
		ft.setIncoming(false);
		ft.setFileInfo(filename, fileSize, lastModified, null);
		ft.setInputStream(is);

		return sendFile(ft);
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
		this.context.getEventBus().addHandler(ConnectionManager.ConnectionEstablishedHandler.ConnectionEstablishedEvent.class,
				this);
		this.context.getEventBus().addHandler(
				FileTransferNegotiator.NegotiationFailureHandler.FileTransferNegotiationFailureEvent.class, this);
		this.context.getEventBus().addHandler(
				FileTransferNegotiator.NegotiationRejectHandler.FileTransferNegotiationRejectEvent.class, this);
		this.context.getEventBus().addHandler(
				FileTransferNegotiator.NegotiationRequestHandler.FileTransferNegotiationRequestEvent.class, this);
	}

	public void setJaxmpp(JaxmppCore jaxmpp) {
		this.jaxmpp = jaxmpp;
		jaxmpp.set(this);

		DiscoveryModule discoveryModule = jaxmpp.getModule(DiscoveryModule.class);
		if (discoveryModule != null) {
			discoveryModule.setNodeCallback(null, new DiscoveryModule.DefaultNodeDetailsCallback(discoveryModule) {
				@Override
				public String[] getFeatures(SessionObject sessionObject, IQ requestStanza, String node) {
					HashSet<String> features = new HashSet<String>();
					String[] defaultFeatures = super.getFeatures(sessionObject, requestStanza, node);

					if (defaultFeatures == null) {
						features.addAll(Arrays.asList(defaultFeatures));
					}

					for (FileTransferNegotiator negotiator : negotiators) {
						String[] negFeatures = negotiator.getFeatures();
						if (negFeatures != null) {
							for (String negFeature : negFeatures) {
								features.add(negFeature);
							}
						}
					}

					return features.toArray(new String[features.size()]);
				}
			});
		}
	}

	private void startReceiving(final FileTransfer fileTransfer, final Socket socket) {
		new Thread() {
			@Override
			public void run() {
				try {
					// try {
					// Thread.sleep(1000);
					// } catch (Exception ex) {}
					File f = fileTransfer.getFile();
					FileOutputStream fos = new FileOutputStream(f);
					// transferData(socket.getChannel(), fos.getChannel());
					transferData(fileTransfer, socket.getInputStream(), new BufferedOutputStream(fos));
					fos.close();
					// try {
					// Thread.sleep(1000);
					// } catch (Exception ex) {}
					socket.close();
					fireOnSuccess(fileTransfer);
				} catch (IOException ex) {
					log.log(Level.SEVERE, "exception transfering data", ex);
				}
			}
		}.start();
	}

	private void startSending(final FileTransfer fileTransfer, final Socket socket) {
		new Thread() {
			@Override
			public void run() {
				try {
					InputStream fis = fileTransfer.getInputStream();
					if (fis == null) {
						File f = fileTransfer.getFile();
						fis = new BufferedInputStream(new FileInputStream(f));
					}
					transferData(fileTransfer, fis, socket.getOutputStream());
					fis.close();

					socket.close();
					fireOnSuccess(fileTransfer);
				} catch (IOException ex) {
					log.log(Level.SEVERE, "exception transfering data", ex);
				}
			}
		}.start();
	}

	private void transferData(FileTransfer ft, InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[16 * 1024];

		int read;

		while ((read = in.read(data)) > -1) {
			out.write(data, 0, read);

			ft.transferredBytes(read);

			if (log.isLoggable(Level.FINEST)) {
				log.log(Level.FINEST, "transferred bytes = {0}", ft.getTransferredBytes());
			}

			// maybe we should not send this event every time?
			fireOnProgress(ft);
		}
	}
}
