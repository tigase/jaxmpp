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

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class TPathExpression {

	private Node node;

	TPathExpression(Node rootNode) {
		this.node = rootNode;
	}

	public Object evaluate(Element element) throws XMLException {
		List<Object> r = evaluateAsArray(element);
		if (r.isEmpty())
			return null;
		else if (r.size() == 1) {
			return r.get(0);
		} else
			return r;
	}

	public List<Object> evaluateAsArray(Element element) throws XMLException {
		ArrayList<Object> x = new ArrayList<Object>();
		this.node.evaluate(x, element);

		return x;
	}

}