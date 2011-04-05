package tigase.jaxmpp.core.client.criteria.tpath;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public interface Function {

	public class Attr implements Function {

		private final String attName;

		public Attr(String attName) {
			this.attName = attName;
		}

		@Override
		public Object value(Element element) throws XMLException {
			return element.getAttribute(attName);
		}

	}

	public static final class Value implements Function {

		@Override
		public Object value(Element element) throws XMLException {
			return element.getValue();
		}
	}

	Object value(Element element) throws XMLException;

}
