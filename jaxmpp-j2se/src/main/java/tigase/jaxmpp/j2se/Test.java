package tigase.jaxmpp.j2se;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Test {

	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("tigase.jaxmpp");
		// create a ConsoleHandler
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		Jaxmpp jaxmpp = new Jaxmpp();
		jaxmpp.getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL, "http://messenger.tigase.org/bosh");
		jaxmpp.getProperties().setUserProperty(SessionObject.USER_JID, JID.jidInstance(args[0]));
		jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, args[1]);

		System.out.println("// login");
		// not necessary. it allows to set own status on sending initial
		// presence
		jaxmpp.getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BEFORE_INITIAL_PRESENCE,
				new Listener<PresenceEvent>() {

					@Override
					public void handleEvent(PresenceEvent be) {
						be.setPriority(-1);
						be.setStatus("jaxmpp2 based Bot!");
						be.setShow(Show.away);
					}
				});

		// listener of incoming messages
		jaxmpp.getModulesManager().getModule(MessageModule.class).addListener(MessageModule.MESSAGE_RECEIVED,
				new Listener<MessageModule.MessageEvent>() {

					@Override
					public void handleEvent(MessageEvent be) {
						try {
							System.out.println("Received message: " + be.getMessage().getAsString());
						} catch (XMLException e) {
							e.printStackTrace();
						}
					}
				});

		jaxmpp.login(true);

		// ping example
		IQ pingIq = IQ.create();
		pingIq.setTo(JID.jidInstance("tigase.org"));
		pingIq.setType(StanzaType.get);
		pingIq.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));
		jaxmpp.send(pingIq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				System.out.println("Ping Error response " + error);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
				System.out.println("PONG");
			}

			@Override
			public void onTimeout() throws XMLException {
				System.out.println("No ping response");
			}
		});

		Thread.sleep(1000 * 5);

		jaxmpp.getPresence().setPresence(null, "Bot changed status", 1);

		jaxmpp.sendMessage(JID.jidInstance("bmalkow@malkowscy.net"), "Test", "Wiadomosc ");

		System.out.println("????????????????");

		Thread.sleep(1000 * 15);

		System.out.println("// disconnect");
		jaxmpp.disconnect();
		System.out.println(".");
	}
}
