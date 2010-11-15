package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.xml.Element;

public interface BoshRequest extends Runnable {

	Element getBody();

	String getRid();
}
