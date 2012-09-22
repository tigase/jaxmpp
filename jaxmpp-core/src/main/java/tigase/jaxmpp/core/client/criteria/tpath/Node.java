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
package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.Collection;
import java.util.List;

import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Node {

	private Criteria criteria;

	private Function function;

	private Node subnode;

	public Node(Criteria criteria, Node subnode) {
		this(criteria, subnode, null);
	}

	public Node(Criteria criteria, Node subnode, Function f) {
		super();
		this.criteria = criteria;
		this.subnode = subnode;
		this.function = f;
	}

	public void evaluate(final Collection<Object> result, Element src) throws XMLException {
		if (criteria != null && !criteria.match(src))
			return;

		if (subnode == null && function != null) {
			Object r = function.value(src);
			if (r != null)
				result.add(r);
			return;
		} else if (subnode == null && function == null) {
			Object r = src;
			if (r != null)
				result.add(r);
			return;
		}

		List<Element> children = src.getChildren();
		for (Element element : children) {
			subnode.evaluate(result, element);
		}

		return;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public Function getFunction() {
		return function;
	}

	public Node getSubnode() {
		return subnode;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public void setSubnode(Node subnode) {
		this.subnode = subnode;
	}

}