package tigase.jaxmpp.j2se.connectors.socket;

import java.util.Map;

public interface StreamListener {

	void xmppStreamClosed();

	void xmppStreamOpened(Map<String, String> attribs);

}
