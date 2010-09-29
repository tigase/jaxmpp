package tigase.jaxmpp.core.client.xml;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Element {
	Element addChild(Element child);

//	List<Element> addChildren(Collection<Element> children);

	String getAttribute(String attName);

	Map<String, String> getAttributes();

	String getCData();

	List<Element> getChildrenNS(String name, String xmlns);

	List<Element> getChildren();

	List<Element> getChildren(String name);

        List<Element> getChildrenNS(String xmlns);

	String getName();

	String getXMLNS();

	void removeAttribute(String key);

	void removeChild(Element child);

	void setAttribute(String key, String value);

	void setAttributes(Map<String, String> attrs);

	void setCData(String cData);

        String getAsString();

//	void setDefXMLNS(String xmlns);

//	void setXMLNS(String xmlns);
}
