package muc;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.j2se.J2SEPresenceStore;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomJoinSample {

	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("tigase.jaxmpp");
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		if (logger.isLoggable(Level.CONFIG))
			logger.config("Logger successfully initialized");

		final Jaxmpp jaxmpp = new Jaxmpp();

		// ignoring all certificate exceptions!
		jaxmpp.getProperties().setUserProperty(SocketConnector.TRUST_MANAGERS_KEY, new TrustManager[]{new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}});

		PresenceModule.setPresenceStore(jaxmpp.getSessionObject(), new J2SEPresenceStore());
		jaxmpp.getModulesManager().register(new PresenceModule());
		jaxmpp.getModulesManager().register(new CapabilitiesModule());
		jaxmpp.getModulesManager().register(new RosterModule());
		jaxmpp.getModulesManager().register(new PingModule());

		jaxmpp.getModulesManager().register(new MucModule());
		jaxmpp.getModulesManager().register(new MessageModule());

		// anonymous login
		jaxmpp.getConnectionConfiguration().setDomain("tigase.org");

		jaxmpp.login();


		final MucModule mucModule = jaxmpp.getModule(MucModule.class);
		jaxmpp.getEventBus().addHandler(MucModule.MucMessageReceivedHandler.MucMessageReceivedEvent.class, new MucModule.MucMessageReceivedHandler() {
			@Override
			public void onMucMessageReceived(SessionObject sessionObject, Message message, Room room, String nickname, Date timestamp) {
				try {
					System.out.println(timestamp + "  " + nickname + ": " + message.getBody());
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		});
		jaxmpp.getEventBus().addHandler(MucModule.YouJoinedHandler.YouJoinedEvent.class, new MucModule.YouJoinedHandler() {
			@Override
			public void onYouJoined(SessionObject sessionObject, Room room, String asNickname) {
				System.out.println("You joined to room!");

				// sending sample message
				try {
					room.sendMessage("test");
				} catch (JaxmppException e) {
					e.printStackTrace();
				}

				// inviting
				try {
					mucModule.invite(room, JID.jidInstance("somebody@somewhere.com"), "I want to invite you");
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}
		});

		jaxmpp.getEventBus().addHandler(MucModule.InvitationReceivedHandler.InvitationReceivedEvent.class, new MucModule.InvitationReceivedHandler() {
			@Override
			public void onInvitationReceived(SessionObject sessionObject, MucModule.Invitation invitation, JID inviterJID, BareJID roomJID) {
				// someone invited me
				System.out.println(inviterJID + " invited you to room " + roomJID);

				// we are happy and we are joinging
				try {
					mucModule.join(invitation, "OurUniqueNickname");
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}
		});

		mucModule.join("test000", "muc.tigase.org", "TesterBot");


		Thread.sleep(5000);
		mucModule.leave(mucModule.getRoom(BareJID.bareJIDInstance("test000", "muc.tigase.org")));
		Thread.sleep(1000);

		jaxmpp.disconnect();

		System.out.println("end");

	}

}
