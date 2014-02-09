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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * ResultSet class is representing results from server returned by
 * ResultSetManagement.
 * 
 * @see http://xmpp.org/extensions/xep-0059.html
 */
public class ResultSet<T> {

	private static Element getFirstChild(Element parent, String name) throws XMLException {
		List<Element> children = parent.getChildren(name);
		if (children == null || children.isEmpty()) {
			return null;
		}

		return children.get(0);
	}

	private int count = 0;
	private String first;
	private List<T> items = new ArrayList<T>();

	private String last;

	public int getCount() {
		return count;
	}

	public String getFirst() {
		return first;
	}

	public List<T> getItems() {
		return items;
	}

	public String getLast() {
		return last;
	}

	void process(Element rsm) throws XMLException {
		Element e = getFirstChild(rsm, "count");
		if (e != null) {
			setCount(Integer.valueOf(e.getValue()));
		}

		e = getFirstChild(rsm, "first");
		if (e != null) {
			setFirst(e.getValue());
		}

		e = getFirstChild(rsm, "last");
		if (e != null) {
			setLast(e.getValue());
		}
	}

	void setCount(int count) {
		this.count = count;
	}

	void setFirst(String first) {
		this.first = first;
	}

	void setItems(List<T> items) {
		this.items = items;
	}

	void setLast(String last) {
		this.last = last;
	}
}
