package tigase.jaxmpp.core.client.xml;

import java.util.Map;
import java.util.Map.Entry;

public class ElementComparator {

	public static boolean equal(final Element e1, final Element e2) {
		try {
			if (e1 == e2)
				return true;
			if (!e1.getName().equals(e2.getName()))
				return false;

			Map<String, String> e1Attr = e1.getAttributes();
			Map<String, String> e2Attr = e2.getAttributes();
			if (e1Attr.size() != e2Attr.size())
				return false;

			for (Entry<String, String> en : e1Attr.entrySet()) {
				String x = e2Attr.get(en.getKey());
				if (x == null || !x.equals(en.getValue()))
					return false;
			}

			if (e1.getChildren().size() != e2.getChildren().size())
				return false;

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
