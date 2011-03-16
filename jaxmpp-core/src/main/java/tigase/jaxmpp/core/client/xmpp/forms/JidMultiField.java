package tigase.jaxmpp.core.client.xmpp.forms;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class JidMultiField extends AbstractField<JID[]> {

	JidMultiField(Element element) throws XMLException {
		super("jid-multi", element);
	}

	public void addFieldValue(JID... value) throws XMLException {
		if (value != null)
			for (JID string : value) {
				addChild(new DefaultElement("value", string == null ? null : string.toString(), null));
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
	public JID[] getFieldValue() throws XMLException {
		ArrayList<JID> result = new ArrayList<JID>();
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				String x = element.getValue();
				result.add(x == null ? null : JID.jidInstance(x));
			}
		return result.toArray(new JID[] {});
	}

	@Override
	public void setFieldValue(JID[] value) throws XMLException {
		clearValues();
		if (value != null) {
			for (JID string : value) {
				addChild(new DefaultElement("value", string == null ? null : string.toString(), null));
			}
		}
	}

}
