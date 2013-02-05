package tigase.jaxmpp.android;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.factory.UniversalFactory.FactorySpi;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;

public class Jaxmpp extends tigase.jaxmpp.j2se.Jaxmpp {
	static {
		UniversalFactory.setSpi(DnsResolver.class.getName(), new FactorySpi<DnsResolver>() {

			@Override
			public DnsResolver create() {
				return new AndroidDNSResolver();
			}
		});
	}

	public Jaxmpp() {
		super();
	}

	public Jaxmpp(SessionObject sessionObject) {
		super(sessionObject);
	}

}
