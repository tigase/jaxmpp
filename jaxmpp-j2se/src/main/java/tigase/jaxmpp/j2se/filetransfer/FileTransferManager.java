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

import tigase.jaxmpp.j2se.connection.ConnectionEvent;
import tigase.jaxmpp.j2se.connection.ConnectionManager;
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
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.DiscoInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferEvent;
import tigase.jaxmpp.j2se.connection.socks5bytestream.J2SEStreamhostsResolver;
import tigase.jaxmpp.j2se.connection.socks5bytestream.StreamhostsResolver;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.j2se.Jaxmpp;

/**
 *
 * @author andrzej
 */
public class FileTransferManager implements ObservableAware {

		static {
                UniversalFactory.setSpi(StreamhostsResolver.class.getCanonicalName(), new UniversalFactory.FactorySpi<J2SEStreamhostsResolver>() {

                        @Override
                        public J2SEStreamhostsResolver create() {
                                return new J2SEStreamhostsResolver();
                        }
                        
                });			
		}

		public static final EventType FILE_TRANSFER_FAILURE = new EventType();
		public static final EventType FILE_TRANSFER_PROGRESS = new EventType();
		public static final EventType FILE_TRANSFER_REJECTED = new EventType();
		public static final EventType FILE_TRANSFER_REQUEST = new EventType();
		public static final EventType FILE_TRANSFER_SUCCESS = new EventType();		
		
        private static final Logger log = Logger.getLogger(FileTransferManager.class.getCanonicalName());

		private Jaxmpp jaxmpp = null;
		
        private final List<FileTransferNegotiator> negotiators = new ArrayList<FileTransferNegotiator>();

        protected Observable observable = null;

        protected Listener<ConnectionEvent> connectionEventListener = new Listener<ConnectionEvent>() {

                @Override
                public void handleEvent(ConnectionEvent be) throws JaxmppException {
                        if (be.getType() == ConnectionManager.CONNECTION_ESTABLISHED) {
                                if (be.getSocket() == null) {
                                        // this should not happen
                                        throw new JaxmppException("SOCKET IS NULL");
                                }
                                connectionEstablished((FileTransfer) be.getConnectionSession(), be.getSocket());
                        }
                }

        };
        
		protected Listener<FileTransferEvent> negotiationListener = new Listener<FileTransferEvent>() {
					@Override
					public void handleEvent(FileTransferEvent be) throws JaxmppException {
						if (FileTransferNegotiator.NEGOTIATION_FAILURE == be.getType()) {
							FileTransfer ft = (FileTransfer) be.getFileTransfer();
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
						else if (FileTransferNegotiator.NEGOTIATION_REJECTED == be.getType()) {
							fireEvent(FILE_TRANSFER_REJECTED, new FileTransferEvent(FILE_TRANSFER_REJECTED, be.getSessionObject(), be.getFileTransfer()));
						}
						else if (FileTransferNegotiator.NEGOTIATION_REQUEST == be.getType()) {
							fireEvent(FILE_TRANSFER_REQUEST, new FileTransferEvent(FILE_TRANSFER_REQUEST, be.getSessionObject(), be.getFileTransfer()));
						}
					}
		};
		
		public void setJaxmpp(Jaxmpp jaxmpp) {
				this.jaxmpp = jaxmpp;
				
				DiscoInfoModule discoInfoModule = jaxmpp.getModule(DiscoInfoModule.class);
				if (discoInfoModule != null) {
					discoInfoModule.addListener(new Listener<DiscoInfoEvent>() {
						@Override
						public void handleEvent(DiscoInfoEvent be) throws JaxmppException {
							if (be.getType() != DiscoInfoModule.InfoRequested) 
								return;
							
							HashSet<String> features = new HashSet<String>();
							
							if (be.getFeatures() != null) {
								features.addAll(Arrays.asList(be.getFeatures()));
							}
							
							for (FileTransferNegotiator negotiator : negotiators) {
								String[] negFeatures = negotiator.getFeatures();
								if (negFeatures != null) {
									for (String negFeature : negFeatures) {
										features.add(negFeature);
									}
								}
							}
							//
							
							be.setFeatures(features.toArray(new String[features.size()]));
						}						
					});
				}
		}
		
        @Override
        public void setObservable(Observable observableParent) {
                observable = ObservableFactory.instance(observableParent);
                observable.addListener(ConnectionManager.CONNECTION_ESTABLISHED, connectionEventListener);
                observable.addListener(ConnectionManager.CONNECTION_CLOSED, connectionEventListener);
                observable.addListener(ConnectionManager.CONNECTION_FAILED, connectionEventListener);
				observable.addListener(FileTransferNegotiator.NEGOTIATION_FAILURE, negotiationListener);
				observable.addListener(FileTransferNegotiator.NEGOTIATION_REJECTED, negotiationListener);
				observable.addListener(FileTransferNegotiator.NEGOTIATION_REQUEST, negotiationListener);
				observable.addListener(FileTransferNegotiator.NEGOTIATION_SUCCESS, negotiationListener);
        }
        
        public void addListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
                observable.addListener(eventType, listener);
        }
        
        public void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
                observable.removeListener(eventType, listener);
        }
                
        public void addNegotiator(FileTransferNegotiator negotiator) {
                negotiator.setObservable(observable);
				negotiator.registerListeners(jaxmpp);
                negotiators.add(negotiator);                
        }
        
        public void removeNegotiator(FileTransferNegotiator negotiator) {
                negotiators.remove(negotiator);
                negotiator.unregisterListeners(jaxmpp);
                negotiator.setObservable(observable);
        }

        public FileTransfer sendFile(JID peer, File file) throws JaxmppException {
                FileTransfer ft = new FileTransfer(jaxmpp.getSessionObject(), peer, generateSid());
                ft.setIncoming(false);
                ft.setFileInfo(file.getName(), file.length(), new Date(file.lastModified()), null);
                ft.setFile(file);
				
                return sendFile(ft);
        }
        		
        public FileTransfer sendFile(JID peer, String filename, long fileSize, InputStream is, Date lastModified) throws JaxmppException {
                FileTransfer ft = new FileTransfer(jaxmpp.getSessionObject(), peer, generateSid());
                ft.setIncoming(false);
                ft.setFileInfo(filename, fileSize, lastModified, null);
                ft.setInputStream(is);
       
                return sendFile(ft);
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
		
		public void acceptFile(FileTransfer ft) throws JaxmppException {
                ft.setIncoming(true);

				ft.getNegotiator().acceptFile(jaxmpp, ft);
        }

        public void rejectFile(FileTransfer ft) throws JaxmppException {
                ft.setIncoming(true);
                
				ft.getNegotiator().rejectFile(jaxmpp, ft);
        }

        protected static String getCapsNode(Presence presence) throws XMLException {
                if (presence == null) return null;
		Element c = presence.getChildrenNS("c", "http://jabber.org/protocol/caps");
		if (c == null)
			return null;
        
		String node = c.getAttribute("node");
		String ver = c.getAttribute("ver");
		if (node == null || ver == null)
			return null;
		
		return node + "#" + ver;
        }
        
        private String generateSid() {
                return UUID.randomUUID().toString();
        }
        
        private void connectionEstablished(FileTransfer fileTransfer, Socket socket) {
                if (fileTransfer.isIncoming()) {
                        startReceiving(fileTransfer, socket);
                }
                else {
                        startSending(fileTransfer, socket);
                }
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
                                }
                                catch (IOException ex) {
                                        log.log(Level.SEVERE, "exception transfering data", ex);
                                }
                        }
                }.start();
        }

        private void startReceiving(final FileTransfer fileTransfer, final Socket socket) {
                new Thread() {
                        @Override
                        public void run() {
                                try {
//                                        try {
//                                                Thread.sleep(1000);
//                                        } catch (Exception ex) {}
                                        File f = fileTransfer.getFile();
                                        FileOutputStream fos = new FileOutputStream(f);
//                                        transferData(socket.getChannel(), fos.getChannel());
										transferData(fileTransfer, socket.getInputStream(), new BufferedOutputStream(fos));
                                        fos.close();
//                                        try {
//                                                Thread.sleep(1000);
//                                        } catch (Exception ex) {}
                                        socket.close();
										fireOnSuccess(fileTransfer);
                                }
                                catch (IOException ex) {
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
		
		private void fireOnFailure(final FileTransfer ft) {
			fireEvent(FILE_TRANSFER_FAILURE, new FileTransferEvent(FILE_TRANSFER_FAILURE, ft.getSessionObject(), ft));
		}

		private void fireOnSuccess(final FileTransfer ft) {
			fireEvent(FILE_TRANSFER_SUCCESS, new FileTransferEvent(FILE_TRANSFER_SUCCESS, ft.getSessionObject(), ft));
		}

		private void fireOnProgress(final FileTransfer ft) {
			fireEvent(FILE_TRANSFER_PROGRESS, new FileTransferEvent(FILE_TRANSFER_PROGRESS, ft.getSessionObject(), ft));
		}
		
		private void fireEvent(EventType type, BaseEvent event) {
			try {
				observable.fireEvent(type, event);
			} catch (JaxmppException ex) {
				log.log(Level.SEVERE, "could not fire event for " + event);
			}
		}
}
