/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
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