package tigase.jaxmpp.core.client.xmpp.forms;

import java.util.List;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class ListSingleField extends AbstractField<String> {

	ListSingleField(Element element) throws XMLException {
		super("list-single", element);
	}

	public void addOption(String label, String value) throws XMLException {
		DefaultElement o = new DefaultElement("option");
		if (label != null)
			o.setAttribute("label", label);
		o.addChild(new DefaultElement("value", value, null));
		addChild(o);
	}

	public void clearOptions() throws XMLException {
		List<Element> lls = getChildren("option");
		if (lls != null)
			for (Element element : lls) {
				removeChild(element);
			}
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
