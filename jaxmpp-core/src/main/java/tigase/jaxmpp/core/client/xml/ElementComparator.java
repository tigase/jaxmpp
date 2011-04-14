package tigase.jaxmpp.core.client.xml;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public class ElementComparator implements Comparator<Element> {

	private static final int compareInternal(final Element e1, final Element e2) {
		try {
			if (e1 == e2)
				return 0;
			int tmp = e1.getName().compareTo(e2.getName());
			if (tmp != 0)
				return tmp;

			Map<String, String> e1Attr = e1.getAttributes();
			Map<String, String> e2Attr = e2.getAttributes();
			tmp = Integer.valueOf(e1Attr.size()).compareTo(Integer.valueOf(e2Attr.size()));
			if (tmp != 0)
				return tmp;

			for (Entry<String, String> en : e1Attr.entrySet()) {
				String x = e2Attr.get(en.getKey());
				if (x == null)
					return -1;
				tmp = x.compareTo(en.getValue());
				if (tmp != 0)
					return tmp;
			}

			tmp = Integer.valueOf(e1.getChildren().size()).compareTo(e2.getChildren().size());
			if (tmp != 0)
				return tmp;

			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1000;
		}
	}

	public static boolean equal(final Element e1, final Element e2) {
		return compareInternal(e1, e2) == 0;

	}

	@Override
	public int compare(final Element e1, final Element e2) {
		return compareInternal(e1, e2);
	}

}
