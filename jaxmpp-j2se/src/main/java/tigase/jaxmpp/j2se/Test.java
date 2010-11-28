package tigase.jaxmpp.j2se;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;

public class Test {

	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("tigase.jaxmpp");
		// create a ConsoleHandler
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		Jaxmpp jaxmpp = new Jaxmpp();
		jaxmpp.getProperties().setUserProperty(BoshConnector.BOSH_SERVICE_URL, "http://messenger.tigase.org/bosh");
		jaxmpp.getProperties().setUserProperty(SessionObject.USER_JID, JID.jidInstance(args[0]));
		jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, args[1]);

		System.out.println("// login");
		jaxmpp.login();

		jaxmpp.sendMessage(JID.jidInstance("bmalkow@malkowscy.net"), "Test", "Historyczna wiadomość wysłana z jaxmpp2 :)");

		System.out.println("????????????????");

		Thread.sleep(1000 * 15);

		System.out.println("// disconnect");
		jaxmpp.disconnect();
		System.out.println(".");
	}
}
