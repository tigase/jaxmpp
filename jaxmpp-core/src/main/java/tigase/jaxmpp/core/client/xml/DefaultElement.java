package tigase.jaxmpp.core.client.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultElement implements Element {

	private final HashMap<String, String> attributes = new HashMap<String, String>();

	private String cData;

	private final ArrayList<Element> children = new ArrayList<Element>();

	private String name;

	/** {@inheritDoc} */
	@Override
	public Element addChild(Element child) {
		this.children.add(child);
		return child;
	}

	/** {@inheritDoc} */
	@Override
	public void addChildren(Collection<Element> children) {
		this.children.addAll(children);
	}

	/** {@inheritDoc} */
	@Override
	public String getAttribute(String attName) {
		return this.attributes.get(attName);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	/** {@inheritDoc} */
	@Override
	public String getCData() {
		return cData;
	}

	/** {@inheritDoc} */
	@Override
	public Element getChild(String name, String childXmlns) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public List<Element> getChildren() {
		return this.children;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String getXMLNS() {
		return getAttribute("xmlns");
	}

	/** {@inheritDoc} */
	@Override
	public void removeAttribute(String attName) {
		this.attributes.remove(attName);
	}

	/** {@inheritDoc} */
	@Override
	public void removeChild(Element child) {
		this.children.remove(child);
	}

	/** {@inheritDoc} */
	@Override
	public void setAttribute(String attName, String value) {
		this.attributes.put(attName, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setAttributes(Map<String, String> attrs) {
		this.attributes.putAll(attrs);
	}

	/** {@inheritDoc} */
	@Override
	public void setCData(String cData) {
		this.cData = cData;
	}

	/** {@inheritDoc} */
	@Override
	public void setDefXMLNS(String xmlns) {
		// TODO Auto-generated method stub

	}

	public DefaultElement(String name) {
		this.name = name;
	}

	/** {@inheritDoc} */
	@Override
	public void setXMLNS(String xmlns) {
		setAttribute("xmlns", xmlns);
	}

}
