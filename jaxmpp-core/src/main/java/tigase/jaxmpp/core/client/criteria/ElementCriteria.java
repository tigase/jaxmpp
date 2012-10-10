/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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
package tigase.jaxmpp.core.client.criteria;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Simple implementation of Criteria to check element name and its attributes..
 * 
 */
public class ElementCriteria implements Criteria {

	/**
	 * Makes criteria object what accepts all elements.
	 * 
	 * @return criteria
	 */
	public static final ElementCriteria empty() {
		return new ElementCriteria(null, null, null);
	}

	/**
	 * Makes criteria object to check name of element.
	 * 
	 * @param name
	 *            expected element name.
	 * @return criteria
	 */
	public static final ElementCriteria name(String name) {
		return new ElementCriteria(name, null, null);
	}

	/**
	 * Makes criteria object to check name of element and its namespace.
	 * 
	 * @param name
	 *            expected name of element.
	 * @param xmlns
	 *            expected xmlns
	 * @return criteria
	 */
	public static final ElementCriteria name(String name, String xmlns) {
		return new ElementCriteria(name, new String[] { "xmlns" }, new String[] { xmlns });
	}

	public static final ElementCriteria name(String name, String[] attNames, String[] attValues) {
		return new ElementCriteria(name, attNames, attValues);
	}

	/**
	 * Makes criteria checking only xmlns attribute.
	 * 
	 * @param xmlns
	 *            expected xmlns
	 * @return criteria
	 */
	public static final ElementCriteria xmlns(String xmlns) {
		return new ElementCriteria(null, new String[] { "xmlns" }, new String[] { xmlns });
	}

	protected HashMap<String, String> attrs = new HashMap<String, String>();

	protected String name;

	protected Criteria nextCriteria;

	/**
	 * Construct criteria.
	 * 
	 * @param name
	 *            name of element. If <code>null</code> then any elements name
	 *            will be accepted;
	 * @param attname
	 *            names of required attributes
	 * @param attValue
	 *            value of requird attrivutes
	 */
	public ElementCriteria(String name, String[] attname, String[] attValue) {
		this.name = name;
		if (attname != null && attValue != null) {
			for (int i = 0; i < attname.length; i++) {
				attrs.put(attname[i], attValue[i]);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Criteria add(Criteria criteria) {
		if (this.nextCriteria == null) {
			this.nextCriteria = criteria;
		} else {
			Criteria c = this.nextCriteria;
			c.add(criteria);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean match(Element element) throws XMLException {
		if (name != null && !name.equals(element.getName())) {
			return false;
		}
		boolean result = true;
		Iterator<Entry<String, String>> attrIterator = this.attrs.entrySet().iterator();
		while (result && attrIterator.hasNext()) {
			Entry<String, String> entry = attrIterator.next();

			String aName = entry.getKey().toString();
			String at = "xmlns".equals(aName) ? element.getXMLNS() : element.getAttribute(aName);
			if (at != null) {
				if (at == null || !at.equals(entry.getValue())) {
					result = false;
					break;
				}
			} else {
				result = false;
			}
		}

		if (this.nextCriteria != null) {
			final List<? extends Element> children = element.getChildren();
			boolean subres = false;
			if (children != null) {
				for (Element sub : children) {
					if (this.nextCriteria.match(sub)) {
						subres = true;
						break;
					}
				}
			}
			result &= subres;
		}

		return result;
	}
}