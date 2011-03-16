package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class BooleanField extends AbstractField<Boolean> {

	BooleanField(Element element) throws XMLException {
		super("boolean", element);
	}

	@Override
	public Boolean getFieldValue() throws XMLException {
		final String t = getChildElementValue("value");
		if (t == null)
			return Boolean.FALSE;
		else if (t.equals("1") || t.equals("true"))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}

	@Override
	public void setFieldValue(Boolean value) throws XMLException {
		if (value == null)
			setChildElementValue("value", "0");
		else if (value.booleanValue())
			setChildElementValue("value", "1");
		else
			setChildElementValue("value", "0");
	}
}
