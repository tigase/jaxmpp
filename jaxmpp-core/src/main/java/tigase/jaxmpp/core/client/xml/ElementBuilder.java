/*
 * ElementBuilder.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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

import java.util.Map;

public class ElementBuilder {

	private final Element rootElement;
	private Element currentElement;

	public static final ElementBuilder create(String name) throws XMLException {
		Element e = ElementFactory.create(name);
		return new ElementBuilder(e);
	}

	public static final ElementBuilder create(String name, String xmlns) throws XMLException {
		Element e = ElementFactory.create(name);
		ElementBuilder eb = new ElementBuilder(e);
		eb.setXMLNS(xmlns);
		return eb;
	}

	private ElementBuilder(Element rootElement) {
		this.rootElement = rootElement;
		this.currentElement = rootElement;
	}

	public ElementBuilder child(String name) throws XMLException {
		Element e = ElementFactory.create(name);
		currentElement.addChild(e);
		currentElement = e;
		return this;
	}

	public Element getElement() {
		return rootElement;
	}

	public ElementBuilder setAttribute(String key, String value) throws XMLException {
		currentElement.setAttribute(key, value);
		return this;
	}

	public ElementBuilder setAttributes(Map<String, String> attrs) throws XMLException {
		currentElement.setAttributes(attrs);
		return this;
	}

	public ElementBuilder setValue(String value) throws XMLException {
		currentElement.setValue(value);
		return this;
	}

	public ElementBuilder setXMLNS(String xmlns) throws XMLException {
		currentElement.setXMLNS(xmlns);
		return this;
	}

	public ElementBuilder up() throws XMLException {
		currentElement = currentElement.getParent();
		return this;
	}

}
