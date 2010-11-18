package tigase.jaxmpp.j2se;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		jaxmpp.getSessionObject().setProperty(BoshConnector.BOSH_SERVICE_URL, "http://messenger.tigase.org/bosh");
		jaxmpp.getSessionObject().setProperty(SessionObject.USER_JID, "bmalkow@tigase.org");
		jaxmpp.getSessionObject().setProperty(SessionObject.SERVER_NAME, "tigase.org");

		System.out.println("// login");
		jaxmpp.login();

		Thread.sleep(1000 * 15);

		System.out.println("// disconnect");
		jaxmpp.disconnect();
	}
}
