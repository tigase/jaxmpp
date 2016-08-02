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

import java.util.List;
import java.util.Map;

public interface Element {

	/**
	 * Add child element to tree and return the added element.
	 * 
	 * @param child
	 *            Child to add.
	 * @return Added child element.
	 * @throws XMLException
	 */
	Element addChild(Element child) throws XMLException;

	public Element findChild(String[] elemPath) throws XMLException;

	/**
	 * Get this element as XML string.
	 * 
	 * @return The normalized XML describing this element.
	 */
	String getAsString() throws XMLException;

	/**
	 * Get attribute by name.
	 * 
	 * @param attName
	 *            Name of attribute to fetch.
	 * @return Attribute value or null if no such element exist.
	 * @throws XMLException
	 */
	String getAttribute(String attName) throws XMLException;

	/**
	 * Get all attributes as a Map.
	 * 
	 * @return Element's attributes.
	 * @throws XMLException
	 */
	Map<String, String> getAttributes() throws XMLException;

	/**
	 * Gets the first child after specified child in children list. Only to be
	 * used internally.
	 * 
	 * @param child
	 *            The child to look up.
	 * @return The child after specific child.
	 * @throws XMLException
	 */
	Element getChildAfter(Element child) throws XMLException;

	/**
	 * Get all children of the element.
	 * 
	 * @return A list containing all child elements.
	 * @throws XMLException
	 */
	List<Element> getChildren() throws XMLException;

	/**
	 * Get children by name.
	 * 
	 * @param name
	 *            Name of the children to get.
	 * @return A list of the Element with the given name.
	 * @throws XMLException
	 */
	List<Element> getChildren(String name) throws XMLException;

	/**
	 * Get children by namespace.
	 * 
	 * @param xmlns
	 *            Namespace of the children to get.
	 * @return A list of children with the given namespace.
	 * @throws XMLException
	 */
	List<Element> getChildrenNS(String xmlns) throws XMLException;

	/**
	 * Get children by namespace.
	 * 
	 * @param name
	 *            Name of the children to get.
	 * @param xmlns
	 *            Namespace of the children to get.
	 * @return A list of Elements with the given name and namespace.
	 * @throws XMLException
	 */
	Element getChildrenNS(String name, String xmlns) throws XMLException;

	/**
	 * Get the first child element of this element.
	 * 
	 * @return First child element or null if no children.
	 * @throws XMLException
	 */
	Element getFirstChild() throws XMLException;

	Element getFirstChild(String name) throws XMLException;

	/**
	 * Get name of this element.
	 * 
	 * @return Name of the element.
	 * @throws XMLException
	 */
	String getName() throws XMLException;

	/**
	 * Get next sibling to this element or null if no parent or no more siblings
	 * exist.
	 * 
	 * @return Next sibling element.
	 * @throws XMLException
	 */
	Element getNextSibling() throws XMLException;

	/**
	 * Get parent element of this element. Or null if no parent exist.
	 * 
	 * @return Parent element.
	 * @throws XMLException
	 */
	Element getParent() throws XMLException;

	/**
	 * Get element value.
	 * 
	 * @return Concatenated string value of element or null if none.
	 * @throws XMLException
	 */
	String getValue() throws XMLException;

	/**
	 * Get namespace of this element. traverses up to find actual namespace.
	 * 
	 * @return Namespace of this element.
	 * @throws XMLException
	 */
	String getXMLNS() throws XMLException;

	/**
	 * Remove attribute from element.
	 * 
	 * @param key
	 *            Name of attribute to remove.
	 * @throws XMLException
	 */
	void removeAttribute(String key) throws XMLException;

	/**
	 * Remove child from element.
	 * 
	 * @param child
	 *            Child element to remove.
	 * @throws XMLException
	 */
	void removeChild(Element child) throws XMLException;

	/**
	 * Set value of attribute. Add attribute if it does not exist.
	 * 
	 * @param key
	 *            Name of attribute to set.
	 * @param value
	 *            Value of attribute to set.
	 * @throws XMLException
	 */
	void setAttribute(String key, String value) throws XMLException;

	/**
	 * Set a number of attributes. Add the ones which does not exist.
	 * 
	 * @param attrs
	 *            Attributes to set.
	 * @throws XMLException
	 */
	void setAttributes(Map<String, String> attrs) throws XMLException;

	/**
	 * Set element parent. Only to be used internally.
	 * 
	 * @param parent
	 *            The parent to set for this element.
	 * @throws XMLException
	 */
	void setParent(Element parent) throws XMLException;

	/**
	 * Set value of this element.
	 * 
	 * @param value
	 *            Value to set.
	 * @throws XMLException
	 */
	void setValue(String value) throws XMLException;

	/**
	 * Change the namespace of this element.
	 * 
	 * @param xmlns
	 *            Namespace to set.
	 * @throws XMLException
	 */
	void setXMLNS(String xmlns) throws XMLException;
}