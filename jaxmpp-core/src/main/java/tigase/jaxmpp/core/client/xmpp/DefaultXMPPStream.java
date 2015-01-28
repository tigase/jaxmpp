package tigase.jaxmpp.core.client.xmpp;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stream.XMPPStream;

public abstract class DefaultXMPPStream implements XMPPStream {

	private Element features;

	@Override
	public Element getFeatures() {
		return features;
	}

	@Override
	public void setFeatures(Element features) {
		this.features = features;
	}

	@Override
	public String toString() {
		return "DefaultXMPPStream";
	}
}
