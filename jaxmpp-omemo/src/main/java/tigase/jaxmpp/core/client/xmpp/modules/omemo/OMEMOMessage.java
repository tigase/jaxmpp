package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

public class OMEMOMessage
		extends Message {

	private boolean secured;

	protected OMEMOMessage(boolean secured, Element element) throws XMLException {
		super(element);
		this.secured = secured;
	}

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

}
