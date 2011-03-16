package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class JidSingleField extends AbstractField<JID> {

	JidSingleField(Element element) throws XMLException {
		super("jid-single", element);
	}

	@Override
	public JID getFieldValue() throws XMLException {
		String x = getChildElementValue("value");
		return x == null ? null : JID.jidInstance(x);
	}

	@Override
	public void setFieldValue(JID value) throws XMLException {
		setChildElementValue("value", value == null ? null : value.toString());
	}

}
