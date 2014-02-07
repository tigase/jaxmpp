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
package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Base interface for fields.
 * 
 * @author bmalkow
 * 
 * @param <T>
 *            type of value
 */
public interface Field<T> {

	/**
	 * Returns natural-language description of field.
	 * 
	 * @return description of field
	 */
	public String getDesc() throws XMLException;

	/**
	 * Returns value of field.
	 * 
	 * @return value
	 */
	T getFieldValue() throws XMLException;

	/**
	 * Returns label of field.
	 * 
	 * @return label
	 */
	public String getLabel() throws XMLException;

	/**
	 * Returns field type. Types described in <a
	 * src='http://xmpp.org/extensions/xep-0004.html'>XEP-0004</a>:
	 * <ul>
	 * <li><b>boolean</b> - implementation class {@linkplain BooleanField}</li>
	 * <li><b>fixed</b> - implementation class {@linkplain FixedField}</li>
	 * <li><b>hidden</b> - implementation class {@linkplain HiddenField}</li>
	 * <li><b>jid-multi</b> - implementation class {@linkplain JidMultiField}</li>
	 * <li><b>jid-single</b> - implementation class {@linkplain JidSingleField}</li>
	 * <li><b>list-multi</b> - implementation class {@linkplain ListMultiField}</li>
	 * <li><b>list-single</b> - implementation class
	 * {@linkplain ListSingleField}</li>
	 * <li><b>text-multi</b> - implementation class {@linkplain TextMultiField}</li>
	 * <li><b>text-private</b> - implementation class
	 * {@linkplain TextPrivateField}</li>
	 * <li><b>text-single</b> - implementation class
	 * {@linkplain TextSingleField}</li>
	 * </ul>
	 * 
	 * @return field type.
	 */
	public String getType() throws XMLException;

	/**
	 * Returns name of field.
	 * 
	 * @return name of field
	 */
	public String getVar() throws XMLException;

	/**
	 * Get is this field is required.
	 * 
	 * @return <code>true</code> is field is required.
	 */
	public boolean isRequired() throws XMLException;

	/**
	 * Set natural-language description of field.
	 * 
	 * @param desc
	 *            description of field
	 */
	public void setDesc(String desc) throws XMLException;

	/**
	 * Sets value of field.
	 * 
	 * @param value
	 *            value of field
	 */
	void setFieldValue(T value) throws XMLException;

	/**
	 * Sets label of field.
	 * 
	 * @param label
	 *            label of field.
	 */
	public void setLabel(String label) throws XMLException;

	/**
	 * Set if this field is required one.
	 * 
	 * @param isRequired
	 *            <code>true</code> is field is required
	 */
	public void setRequired(boolean isRequired) throws XMLException;

	/**
	 * Sets name of field.
	 * 
	 * @param var
	 *            name of field.
	 */
	public void setVar(String var) throws XMLException;
}