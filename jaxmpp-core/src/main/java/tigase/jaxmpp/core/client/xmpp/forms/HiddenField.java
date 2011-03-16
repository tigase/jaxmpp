package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class HiddenField extends AbstractField<String> {

	HiddenField(Element element) throws XMLException {
		super("hidden", element);
	}

	@Override
	public String getFieldValue() throws XMLException {
		return getChildElementValue("value");
	}

	@Override
	public void setFieldValue(String value) throws XMLException {
		setChildElementValue("value", value);
	}
}
