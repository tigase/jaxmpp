package tigase.jaxmpp.core.client.xmpp.forms;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class TextMultiField extends AbstractField<String[]> {

	TextMultiField(Element element) throws XMLException {
		super("text-multi", element);
	}

	public void addFieldValue(String... value) throws XMLException {
		if (value != null)
			for (String string : value) {
				addChild(new DefaultElement("value", string, null));
			}
	}

	public void clearValues() throws XMLException {
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				removeChild(element);
			}
	}

	@Override
	public String[] getFieldValue() throws XMLException {
		ArrayList<String> result = new ArrayList<String>();
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				result.add(element.getValue());
			}
		return result.toArray(new String[] {});
	}

	@Override
	public void setFieldValue(String[] value) throws XMLException {
		clearValues();
		if (value != null) {
			for (String string : value) {
				addChild(new DefaultElement("value", string, null));
			}
		}
	}

}
