/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.jaxmpp.j2se;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.factory.UniversalFactory.FactorySpi;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.*;

/**
 *
 * @author andrzej
 */
public class FileTest implements Runnable {
        
        Jaxmpp jaxmpp = new Jaxmpp();
        FileTransferManager ftManager = new FileTransferManager();
        Socks5FileTransferNegotiator socks5negotiator = new Socks5FileTransferNegotiator();
        
        public FileTest() {
                
        }
        
        public static void main(String [] args) throws IOException {        
                new Thread(new FileTest()).start();
////              InetSocketAddress inet = new InetSocketAddress("fe80::a00b:baff:fece:62dc%p2p0", 1080);
//                String x = "fe80::a00b:baff:fece:62dc%p2p0";
//                int idx = x.indexOf("%");
//                if (idx >= 0) x = x.substring(0, idx);
//                InetSocketAddress inet = new InetSocketAddress(x, 1080);
//                if (inet.isUnresolved()) {
//                        System.out.println("unresolved = " + inet.toString());
//                }
//                //SocketChannel.open(inet);
//                System.out.println(inet.toString());
        }

        @Override
        public void run() {      
                UniversalFactory.setSpi(StreamhostsResolver.class.getCanonicalName(), new FactorySpi<J2SEStreamhostsResolver>() {

                        @Override
                        public J2SEStreamhostsResolver create() {
                                return new J2SEStreamhostsResolver();
                        }
                        
                });
                
                jaxmpp.getModule(CapabilitiesModule.class).setCache(new J2SECapabiliesCache());
                
                ftManager.setObservable(null);
                
                Logger.getGlobal().setLevel(Level.ALL);                
                Logger.getGlobal().addHandler(new ConsoleHandler());
                
                jaxmpp.getConnectionConfiguration().setUserJID("andrzej.wojcik@tigase.org");
                jaxmpp.getConnectionConfiguration().setUserPassword("!7iTm$b3d");
//                jaxmpp.getConnectionConfiguration().setUserJID("andrzej.wojcik@tigase.org/test");
//                jaxmpp.getConnectionConfiguration().setUserPassword("W$vve!@2gw");
                
                jaxmpp.getModulesManager().register(new FileTransferModule(jaxmpp.getSessionObject()));
                jaxmpp.getModulesManager().register(new Socks5BytestreamsModule(jaxmpp.getSessionObject()));
                
                ftManager.registerJaxmpp(jaxmpp);
                ftManager.addNegotiator(socks5negotiator);
                
                ftManager.addListener(FileTransferNegotiator.NEGOTIATION_REQUEST, new Listener<FileTransferRequestEvent>() {

                        @Override
                        public void handleEvent(FileTransferRequestEvent be) throws JaxmppException {
                                FileTransfer ft = be.getFileTransfer();
                                File f = new File("/home/andrzej/received_file");
                                if (f.exists()) f.delete();
                                try {
                                        f.createNewFile();
                                } catch (IOException ex) {
                                        Logger.getLogger(FileTest.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                ft.setFile(f);
                                
                                ftManager.acceptFile(ft);
                        }
                        
                });
                
                
                try {
                        jaxmpp.login(true);
                        Thread.sleep(2 * 1000);
                        //ftManager.sendFile(jaxmpp.getSessionObject(), JID.jidInstance("andrzej.wojcik@tigase.im/test3"), new File("/home/andrzej/s2s_tls_issue.txt"));
                } catch (Exception ex) {
                        Logger.getLogger(FileTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                        Thread.sleep(30 * 5 * 1000 * 60);
                } catch (InterruptedException ex) {
                        Logger.getLogger(FileTest.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
}
