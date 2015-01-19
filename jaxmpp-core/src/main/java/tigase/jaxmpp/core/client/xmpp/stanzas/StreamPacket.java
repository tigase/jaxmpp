package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xmpp.stream.XMPPStream;

public abstract class StreamPacket extends ElementWrapper {

	private XMPPStream xmppStream;

	public StreamPacket(Element element) {
		super(element);
	}

	public XMPPStream getXmppStream() {
		return xmppStream;
	}

	public void setXmppStream(XMPPStream xmppStream) {
		this.xmppStream = xmppStream;
	}

}
