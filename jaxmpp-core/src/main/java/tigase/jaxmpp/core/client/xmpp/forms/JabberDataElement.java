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
package tigase.jaxmpp.core.client.xmpp.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Implementation of Data Form.
 * 
 */
public class JabberDataElement extends ElementWrapper {

	private static AbstractField<?> create(Element element) throws XMLException {
		final String type = element.getAttribute("type");
		if (type.equals("boolean")) {
			return new BooleanField(element);
		} else if (type.equals("fixed")) {
			return new FixedField(element);
		} else if (type.equals("hidden")) {
			return new HiddenField(element);
		} else if (type.equals("jid-multi")) {
			return new JidMultiField(element);
		} else if (type.equals("jid-single")) {
			return new JidSingleField(element);
		} else if (type.equals("list-multi")) {
			return new ListMultiField(element);
		} else if (type.equals("list-single")) {
			return new ListSingleField(element);
		} else if (type.equals("text-multi")) {
			return new TextMultiField(element);
		} else if (type.equals("text-private")) {
			return new TextPrivateField(element);
		} else {
			return new TextSingleField(element);
		}
	}

	private final ArrayList<AbstractField<?>> fields = new ArrayList<AbstractField<?>>();

	private final Map<String, AbstractField<?>> fieldsMap = new HashMap<String, AbstractField<?>>();

	/**
	 * Creates instance of JabberDataElement and parse fields.
	 * 
	 * @param x
	 *            &lt;x xmlns='jabber:x:data'/&gt; element.
	 */
	public JabberDataElement(Element x) throws JaxmppException {
		super(x);
		try {
			if (!"x".equals(x.getName()) || !"jabber:x:data".equals(x.getXMLNS()))
				throw new JaxmppException("Invalid jabber:x:form element");

			List<Element> fs = x.getChildren("field");
			if (fs != null)
				for (Element element : fs) {
					AbstractField<?> af = create(element);
					if (af != null) {
						this.fields.add(af);
						String var = af.getVar();
						if (var != null)
							this.fieldsMap.put(var, af);
					}
				}

		} catch (XMLException e) {
			throw new JaxmppException(e);
		}
	}

	/**
	 * Creates empty form instance.
	 * 
	 * @param type
	 *            type of data.
	 */
	public JabberDataElement(XDataType type) throws XMLException {
		super(new DefaultElement("x", null, "jabber:x:data"));
		setAttribute("type", type.name());
	}

	/**
	 * Adds boolean field to form.
	 * 
	 * @param var
	 *            name of field
	 * @param value
	 *            value of field
	 * @return {@linkplain BooleanField}
	 */
	public final BooleanField addBooleanField(String var, Boolean value) throws XMLException {
		BooleanField result = new BooleanField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	protected void addField(final AbstractField<?> f) throws XMLException {
		String var = f.getVar();
		if (var != null)
			this.fieldsMap.put(var, f);
		this.fields.add(f);
		addChild(f);
	}

	/**
	 * Adds fixed field to form.
	 * 
	 * @param value
	 *            value of field
	 * @return {@linkplain FixedField}
	 */
	public final FixedField addFixedField(String value) throws XMLException {
		FixedField result = new FixedField(new DefaultElement("field"));
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds field "FORM_TYPE" hidded field to form.
	 * 
	 * @param value
	 *            value of field
	 */
	public void addFORM_TYPE(String value) throws XMLException {
		addHiddenField("FORM_TYPE", value);
	}

	/**
	 * Adds hidden field to form.
	 * 
	 * @param var
	 *            name of field
	 * @param value
	 *            value of field
	 * @return {@linkplain HiddenField}
	 */
	public final HiddenField addHiddenField(String var, String value) throws XMLException {
		HiddenField result = new HiddenField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds jid-multi field to form.
	 * 
	 * @param var
	 *            name of field
	 * @param value
	 *            values of field
	 * @return {@linkplain JidMultiField}
	 */
	public final JidMultiField addJidMultiField(String var, JID... value) throws XMLException {
		JidMultiField result = new JidMultiField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds jid-single field to form.
	 * 
	 * @param var
	 *            name of field
	 * @param value
	 *            value of field
	 * @return {@linkplain JidSingleField}
	 */
	public final JidSingleField addJidSingleField(String var, JID value) throws XMLException {
		JidSingleField result = new JidSingleField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds list-multi field to form.
	 * 
	 * @param var
	 *            name of field
	 * @param value
	 *            values of field
	 * @return {@linkplain ListMultiField}
	 */
	public final ListMultiField addListMultiField(String var, String... value) throws XMLException {
		ListMultiField result = new ListMultiField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds list-single field to form.
	 * 
	 * @param var
	 *            name of field.
	 * @param value
	 *            value of field
	 * @return {@linkplain ListSingleField}
	 */
	public final ListSingleField addListSingleField(String var, String value) throws XMLException {
		ListSingleField result = new ListSingleField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds text-multi field to form.
	 * 
	 * @param var
	 *            name of field
	 * @param value
	 *            values of field
	 * @return {@linkplain TextMultiField}
	 */
	public final TextMultiField addTextMultiField(String var, String... value) throws XMLException {
		TextMultiField result = new TextMultiField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds text-private field to form.
	 * 
	 * @param var
	 *            name of field.
	 * @param value
	 *            value of field
	 * @return {@link TextPrivateField}
	 */
	public final TextPrivateField addTextPrivateField(String var, String value) throws XMLException {
		TextPrivateField result = new TextPrivateField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds text-single field to form.
	 * 
	 * @param var
	 *            name of field.
	 * @param value
	 *            value od field.
	 * @return {@linkplain TextSingleField}
	 */
	public final TextSingleField addTextSingleField(String var, String value) throws XMLException {
		TextSingleField result = new TextSingleField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Creates {@linkplain Element XML Element} contains only values of fields.
	 * 
	 * @param type
	 *            data type
	 * @return &lt;x xmlns='jabber:x:data'/&gt; {@linkplain Element XML Element}
	 *         with form
	 */
	public Element createSubmitableElement(final XDataType type) throws XMLException {
		DefaultElement e = DefaultElement.create(this, -1);
		e.setAttribute("type", type.name());

		return e;
	}

	/**
	 * Returns field with given name.
	 * 
	 * @param var
	 *            name of field
	 * @return field or <code>null</code> is field with given name doesn't
	 *         exists in form.
	 */
	@SuppressWarnings("unchecked")
	public <X extends AbstractField<?>> X getField(final String var) {
		return (X) this.fieldsMap.get(var);
	}

	/**
	 * Returns all fields of form.
	 * 
	 * @return list of all fields in form.
	 */
	public ArrayList<AbstractField<?>> getFields() {
		return fields;
	}

	/**
	 * Returns natural language instruction.
	 * 
	 * @return natural language instruction.
	 */
	public String getInstructions() throws XMLException {
		return getAttribute("instructions");
	}

	/**
	 * Returns title.
	 * 
	 * @return title
	 */
	public String getTitle() throws XMLException {
		return getAttribute("title");
	}

	/**
	 * Return data type.
	 * 
	 * @return {@linkplain XDataType}
	 */
	public XDataType getType() throws XMLException {
		String x = getAttribute("type");
		return x == null ? null : XDataType.valueOf(x);
	}

	/**
	 * Sets natural-language instruction.
	 * 
	 * @param instructions
	 *            instruction.
	 */
	public void setInstructions(String instructions) throws XMLException {
		setAttribute("instructions", instructions);
	}

	/**
	 * Sets form title.
	 * 
	 * @param title
	 *            title
	 */
	public void setTitle(String title) throws XMLException {
		setAttribute("title", title);
	}

}