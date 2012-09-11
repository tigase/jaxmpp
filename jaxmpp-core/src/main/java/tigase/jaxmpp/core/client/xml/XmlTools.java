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