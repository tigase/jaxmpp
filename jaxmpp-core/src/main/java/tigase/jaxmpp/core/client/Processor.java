package tigase.jaxmpp.core.client;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.xml.Element;

public class Processor {

	private final ArrayList<XmppModule> modules = new ArrayList<XmppModule>();

	private final SessionObject sessionObject;

	private final PacketWriter writer;

	public Processor(final SessionObject sessionObject, final PacketWriter writer) {
		this.sessionObject = sessionObject;
		this.writer = writer;
	}

	public <T extends XmppModule> T add(T aplugin) {
		this.modules.add(aplugin);
		return aplugin;
	}

	public void process(final Element stanza) {

	}

}
