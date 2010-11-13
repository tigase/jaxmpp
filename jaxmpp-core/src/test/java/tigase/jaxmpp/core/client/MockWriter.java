package tigase.jaxmpp.core.client;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.xml.Element;

public class MockWriter implements PacketWriter {

	private final ArrayList<Element> elements = new ArrayList<Element>();

	public Element poll() {
		if (elements.size() == 0)
			return null;
		return elements.remove(0);
	}

	@Override
	public void write(Element stanza) {
		elements.add(stanza);
	}

}
