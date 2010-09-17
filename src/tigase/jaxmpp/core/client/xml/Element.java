package tigase.jaxmpp.core.client.xml;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Element {
	Element addChild(Element child);

	void addChildren(Collection<Element> children);

	String getAttribute(String attName);

	Map<String, String> getAttributes();

	String getCData();

	Element getChild(String name, String child_xmlns);

	List<Element> getChildren();

	String getName();

	String getXMLNS();

	void removeAttribute(String key);

	void removeChild(Element child);

	void setAttribute(String key, String value);

	void setAttributes(Map<String, String> attrs);

	void setCData(String cData);

	void setDefXMLNS(String xmlns);

	void setXMLNS(String xmlns);
}
