package tigase.jaxmpp.core.client.xml;

public class XmlTools {

	public static Element makeResult(final Element element) throws XMLException {
		String t = element.getAttribute("to");
		String f = element.getAttribute("from");
		Element result = DefaultElement.create(element, 0);
		result.removeAttribute("from");
		result.removeAttribute("to");

		result.setAttribute("type", "result");

		if (f != null)
			result.setAttribute("to", f);
		if (t != null)
			result.setAttribute("from", t);

		return result;
	}

}
