package tigase.jaxmpp.j2se;

import java.net.URL;

public class Test {

	public static void main(String[] args) throws Exception {
		Jaxmpp jaxmpp = new Jaxmpp();

		jaxmpp.getConnector().getConnectorData().url = new URL("http://messenger.tigase.org/bosh");
		jaxmpp.getConnector().getConnectorData().toHost = "tigase.org";
		jaxmpp.getConnector().getConnectorData().fromUser = "bmalkow@tigase.org";

		System.out.println("// login");
		jaxmpp.login();

		Thread.sleep(1000 * 15);

		System.out.println("// disconnect");
		jaxmpp.disconnect();
	}
}
