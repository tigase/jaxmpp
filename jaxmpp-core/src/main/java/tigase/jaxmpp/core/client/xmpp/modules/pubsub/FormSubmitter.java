package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public abstract class FormSubmitter {

	protected final JabberDataElement form;

	protected final JID serviceJID;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public FormSubmitter(SessionObject sessionObject, PacketWriter packetWriter, JID serviceJID, JabberDataElement form) {
		this.form = form;
		this.serviceJID = serviceJID;
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public JabberDataElement getForm() {
		return form;
	}

	protected abstract Element prepareIqPayload() throws XMLException;

	public void submit(final AsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(serviceJID);

		Element payload = prepareIqPayload();
		iq.addChild(payload);

		writer.write(iq, callback);
	}

}
