package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

public abstract class AbstractField<T> extends ElementWrapper implements Field<T> {

	AbstractField(String elementType, Element element) throws XMLException {
		super(element);
		if (elementType != null)
			setAttribute("type", elementType);
	}

	@Override
	public String getDesc() throws XMLException {
		return getChildElementValue("desc");
	}

	@Override
	public String getLabel() throws XMLException {
		return getAttribute("label");
	}

	@Override
	public String getType() throws XMLException {
		return getAttribute("type");
	}

	@Override
	public String getVar() throws XMLException {
		return getAttribute("var");
	}

	@Override
	public boolean isRequired() throws XMLException {
		return getFirstChild("required") != null;
	}

	@Override
	public void setDesc(String desc) throws XMLException {
		setChildElementValue("desc", desc);
	}

	@Override
	public void setLabel(String label) throws XMLException {
		setAttribute("label", label);
	}

	@Override
	public void setRequired(boolean value) throws XMLException {
		Element b = getFirstChild("required");
		if (value == false && b != null)
			removeChild(b);
		else if (value == true && b == null) {
			b = new DefaultElement("required");
			addChild(b);
		}
	}

	@Override
	public void setVar(String var) throws XMLException {
		setAttribute("var", var);
	}

}
