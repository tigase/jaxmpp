package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

public class OMEMOEncryptableMessage
		extends Message {

	public static enum Encryption {
		Required,
		Disabled, Default
	}
	private Encryption encryption = Encryption.Default;

	public OMEMOEncryptableMessage(Element element) throws XMLException {
		super(element);
	}

	public Encryption getEncryption() {
		return encryption;
	}

	public void setEncryption(Encryption encryption) {
		this.encryption = encryption;
	}

}
