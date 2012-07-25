package tigase.jaxmpp.core.client.xmpp.modules.vcard;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class VCardModule extends AbstractStanzaModule<Stanza> {

	public static abstract class VCardAsyncCallback implements AsyncCallback {

		@Override
		public void onSuccess(final Stanza responseStanza) throws XMLException {
			Element query = responseStanza.getChildrenNS("vCard", "vcard-temp");
			if (query != null) {
				VCard v = new VCard();
				v.loadData(query);
				onVCardReceived(v);
			}
		}

		protected abstract void onVCardReceived(VCard vcard) throws XMLException;

	}

	public VCardModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return new String[] { "vcard-temp" };
	}

	@Override
	public void process(Stanza element) throws JaxmppException {
	}

	public void retrieveVCard(JID jid, AsyncCallback asyncCallback) throws JaxmppException {
		retrieveVCard(jid, null, asyncCallback);
	}

	public void retrieveVCard(JID jid, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		iq.setTo(jid);
		iq.addChild(new DefaultElement("vCard", null, "vcard-temp"));

		writer.write(iq, timeout, asyncCallback);
	}

	public void retrieveVCard(JID jid, VCardAsyncCallback asyncCallback) throws JaxmppException {
		retrieveVCard(jid, (AsyncCallback) asyncCallback);
	}
}
