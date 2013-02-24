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
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.MultiJaxmpp;
import tigase.jaxmpp.core.client.SessionObject;
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
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
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
        
		public void setJaxmpp(Jaxmpp jaxmpp) {
				this.jaxmpp = jaxmpp;
		}
		
        @Override
        public void setObservable(Observable observableParent) {
                observable = ObservableFactory.instance(observableParent);
                observable.addListener(ConnectionManager.CONNECTION_ESTABLISHED, connectionEventListener);
                observable.addListener(ConnectionManager.CONNECTION_CLOSED, connectionEventListener);
                observable.addListener(ConnectionManager.CONNECTION_FAILED, connectionEventListener);
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
        
                negotiators.get(0).sendFile(jaxmpp, ft);
                
                return ft;
        }
        		
        public FileTransfer sendFile(JID peer, String filename, long fileSize, InputStream is, Date lastModified) throws JaxmppException {
                FileTransfer ft = new FileTransfer(jaxmpp.getSessionObject(), peer, generateSid());
                ft.setIncoming(false);
                ft.setFileInfo(filename, fileSize, lastModified, null);
                ft.setInputStream(is);
        
                negotiators.get(0).sendFile(jaxmpp, ft);
                
                return ft;
        }

		public void acceptFile(FileTransfer ft) throws JaxmppException {
                ft.setIncoming(true);
                
                negotiators.get(0).acceptFile(jaxmpp, ft);
        }

        public void rejectFile(FileTransfer ft) throws JaxmppException {
                ft.setIncoming(true);
                
                negotiators.get(0).rejectFile(jaxmpp, ft);
        }

        protected static String getCapsNode(Presence presence) throws XMLException {
                if (presence == null) return null;
		Element c = presence.getChildrenNS("c", "http://jabber.org/protocol/caps");
		if (c == null)
			return null;
                return c.getValue();
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
                                        try {
                                                Thread.sleep(1000);
                                        } catch (Exception ex) {}
                                        File f = fileTransfer.getFile();
                                        FileOutputStream fos = new FileOutputStream(f);
//                                        transferData(socket.getChannel(), fos.getChannel());
										transferData(fileTransfer, socket.getInputStream(), new BufferedOutputStream(fos));
                                        fos.close();
                                        try {
                                                Thread.sleep(1000);
                                        } catch (Exception ex) {}
                                        socket.close();
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
                }
        }
}
