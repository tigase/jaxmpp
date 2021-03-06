/*
 * JabberDataElement.java
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
package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Data Form.
 */
public class JabberDataElement
		extends ElementWrapper {

	private final ArrayList<AbstractField<?>> fields = new ArrayList<AbstractField<?>>();
	private final Map<String, AbstractField<?>> fieldsMap = new HashMap<String, AbstractField<?>>();
	private Element currentRowItem;

	private static AbstractField<?> create(Element element) throws XMLException {
		final String type = element.getAttribute("type");
		return create(type, element);
	}

	private static AbstractField<?> create(final String type, Element element) throws XMLException {
		if ("boolean".equals(type)) {
			return new BooleanField(element);
		} else if ("fixed".equals(type)) {
			return new FixedField(element);
		} else if ("hidden".equals(type)) {
			return new HiddenField(element);
		} else if ("jid-multi".equals(type)) {
			return new JidMultiField(element);
		} else if ("jid-single".equals(type)) {
			return new JidSingleField(element);
		} else if ("list-multi".equals(type)) {
			return new ListMultiField(element);
		} else if ("list-single".equals(type)) {
			return new ListSingleField(element);
		} else if ("text-multi".equals(type)) {
			return new TextMultiField(element);
		} else if ("text-private".equals(type)) {
			return new TextPrivateField(element);
		} else {
			return new TextSingleField(element);
		}
	}

	public static String[] getFieldValueAsStringArray(AbstractField field) throws XMLException {
		switch (field.getType()) {
			case "boolean":
				return new String[]{String.valueOf(((BooleanField) field).getFieldValue())};
			case "fixed":
				return new String[]{((FixedField) field).getFieldValue()};
			case "hidden":
				return new String[]{((HiddenField) field).getFieldValue()};
			case "jid-multi":
				return Arrays.stream(((JidMultiField) field).getFieldValue()).map(JID::toString).toArray(String[]::new);
			case "jid-single":
				return new String[]{((JidSingleField) field).getFieldValue().toString()};
			case "list-multi":
				return ((ListMultiField) field).getFieldValue();
			case "list-single":
				return new String[]{((ListSingleField) field).getFieldValue()};
			case "text-multi":
				return ((TextMultiField) field).getFieldValue();
			case "text-private":
				return new String[]{((TextPrivateField) field).getFieldValue()};
			default:
				return new String[]{((TextSingleField) field).getFieldValue()};
		}
	}

	/**
	 * Creates instance of JabberDataElement and parse fields.
	 *
	 * @param x &lt;x xmlns='jabber:x:data'/&gt; element.
	 */
	public JabberDataElement(Element x) throws JaxmppException {
		super(x);
		try {
			if (!"x".equals(x.getName()) || !"jabber:x:data".equals(x.getXMLNS())) {
				throw new JaxmppException("Invalid jabber:x:form element");
			}

			List<Element> fs = x.getChildren("field");
			if (fs != null) {
				for (Element element : fs) {
					AbstractField<?> af = create(element);
					if (af != null) {
						this.fields.add(af);
						String var = af.getVar();
						if (var != null) {
							this.fieldsMap.put(var, af);
						}
					}
				}
			}

		} catch (XMLException e) {
			throw new JaxmppException(e);
		}
	}

	/**
	 * Creates empty form instance.
	 *
	 * @param type type of data.
	 */
	public JabberDataElement(XDataType type) throws XMLException {
		super(ElementFactory.create("x", null, "jabber:x:data"));
		setAttribute("type", type.name());
	}

	/**
	 * Adds boolean field to form.
	 *
	 * @param var name of field
	 * @param value value of field
	 *
	 * @return {@linkplain BooleanField}
	 */
	public final BooleanField addBooleanField(String var, Boolean value) throws XMLException {
		BooleanField result = new BooleanField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds field "FORM_TYPE" hidded field to form.
	 *
	 * @param value value of field
	 */
	public void addFORM_TYPE(String value) throws XMLException {
		addHiddenField("FORM_TYPE", value);
	}

	protected void addField(final AbstractField<?> f) throws XMLException {
		String var = f.getVar();
		if (var != null) {
			AbstractField<?> old = this.fieldsMap.get(var);
			if (old != null) {
				this.fields.remove(old);
				this.fieldsMap.remove(var);
				removeChild(old);
			}
			this.fieldsMap.put(var, f);
		}
		this.fields.add(f);

		if (currentRowItem != null) {
			addToReported(f);
			currentRowItem.addChild(f);
		} else {
			addChild(f);
		}
	}

	/**
	 * Adds fixed field to form.
	 *
	 * @param value value of field
	 *
	 * @return {@linkplain FixedField}
	 */
	public final FixedField addFixedField(String value) throws XMLException {
		return addFixedField(null, value);
	}

	/**
	 * Adds fixed field to form.
	 *
	 * @param value value of field
	 *
	 * @return {@linkplain FixedField}
	 */
	public final FixedField addFixedField(String var, String value) throws XMLException {
		FixedField result = new FixedField(ElementFactory.create("field"));
		if (null != var) {
			result.setVar(var);
		}
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds hidden field to form.
	 *
	 * @param var name of field
	 * @param value value of field
	 *
	 * @return {@linkplain HiddenField}
	 */
	public final HiddenField addHiddenField(String var, String value) throws XMLException {
		HiddenField result = new HiddenField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds jid-multi field to form.
	 *
	 * @param var name of field
	 * @param value values of field
	 *
	 * @return {@linkplain JidMultiField}
	 */
	public final JidMultiField addJidMultiField(String var, JID... value) throws XMLException {
		JidMultiField result = new JidMultiField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds jid-single field to form.
	 *
	 * @param var name of field
	 * @param value value of field
	 *
	 * @return {@linkplain JidSingleField}
	 */
	public final JidSingleField addJidSingleField(String var, JID value) throws XMLException {
		JidSingleField result = new JidSingleField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds list-multi field to form.
	 *
	 * @param var name of field
	 * @param value values of field
	 *
	 * @return {@linkplain ListMultiField}
	 */
	public final ListMultiField addListMultiField(String var, String... value) throws XMLException {
		ListMultiField result = new ListMultiField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds list-single field to form.
	 *
	 * @param var name of field.
	 * @param value value of field
	 *
	 * @return {@linkplain ListSingleField}
	 */
	public final ListSingleField addListSingleField(String var, String value) throws XMLException {
		ListSingleField result = new ListSingleField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	void addRow() throws XMLException {
		if (currentRowItem == null) {
			addChild(ElementFactory.create("reported"));
		}
		currentRowItem = ElementFactory.create("item");
		addChild(currentRowItem);
	}

	/**
	 * Adds text-multi field to form.
	 *
	 * @param var name of field
	 * @param value values of field
	 *
	 * @return {@linkplain TextMultiField}
	 */
	public final TextMultiField addTextMultiField(String var, String... value) throws XMLException {
		TextMultiField result = new TextMultiField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds text-private field to form.
	 *
	 * @param var name of field.
	 * @param value value of field
	 *
	 * @return {@link TextPrivateField}
	 */
	public final TextPrivateField addTextPrivateField(String var, String value) throws XMLException {
		TextPrivateField result = new TextPrivateField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	/**
	 * Adds text-single field to form.
	 *
	 * @param var name of field.
	 * @param value value od field.
	 *
	 * @return {@linkplain TextSingleField}
	 */
	public final TextSingleField addTextSingleField(String var, String value) throws XMLException {
		TextSingleField result = new TextSingleField(ElementFactory.create("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	private void addToReported(final AbstractField<?> field) throws XMLException {
		Element r = getFirstChild("reported");
		if (r == null) {
			r = ElementFactory.create("reported");
			addChild(r);
		}

		Element dec = findReportedField(r, field.getVar());
		if (dec == null) {
			dec = ElementFactory.create("field");
			r.addChild(dec);
			dec.setAttribute("var", field.getVar());
			dec.setAttribute("type", field.getType());
		}
		if (field.getLabel() != null) {
			dec.setAttribute("label", field.getLabel());
		}

	}

	public void cleanUpForm() throws XMLException {
		if (currentRowItem != null) {
			updateReported();
		}
	}

	/**
	 * Creates {@linkplain Element XML Element} contains only values of fields.
	 *
	 * @param type data type
	 *
	 * @return &lt;x xmlns='jabber:x:data'/&gt; {@linkplain Element XML Element} with form
	 */
	public Element createSubmitableElement(final XDataType type) throws JaxmppException {
		Element e = ElementFactory.create(this);
		e.setAttribute("type", type.name());
		return e;
	}

	private Element findReportedField(Element reportedElement, String var) throws XMLException {
		for (Element child : reportedElement.getChildren()) {
			String v = child.getAttribute("var");
			if (var != null && var.equals(v)) {
				return child;
			}
		}
		return null;
	}

	@Override
	public String getAsString() throws XMLException {

		if (currentRowItem != null) {
			updateReported();

		}

		return super.getAsString();
	}

	/**
	 * Returns field with given name.
	 *
	 * @param var name of field
	 *
	 * @return field or <code>null</code> is field with given name doesn't exists in form.
	 */
	@SuppressWarnings("unchecked")
	public <X extends AbstractField<?>> X getField(final String var) {
		return (X) this.fieldsMap.get(var);
	}

	public <X extends AbstractField<?>> X getField(int row, final String var) throws XMLException {
		Element reported = getFirstChild("reported");
		Element e = getChildren("item").get(row);

		Element fe = findReportedField(e, var);
		Element fd = findReportedField(reported, var);

		AbstractField<?> f = create(fd.getAttribute("type"), fe);
		f.setLabel(fd.getAttribute("label"));

		return (X) f;
	}

	/**
	 * Returns all fields of form.
	 *
	 * @return list of all fields in form.
	 */
	public ArrayList<AbstractField<?>> getFields() {
		return fields;
	}

	public List<AbstractField<?>> getFields(boolean includeHidden) {
		return fields.stream().filter(field -> !(includeHidden || field instanceof HiddenField)).collect(Collectors.toList());
	}

	/**
	 * Returns natural language instruction.
	 *
	 * @return natural language instruction.
	 */
	public String getInstructions() throws XMLException {
		return getChildElementValue("instructions");
	}

	/**
	 * Sets natural-language instruction.
	 *
	 * @param instructions instruction.
	 */
	public void setInstructions(String instructions) throws XMLException {
		setChildElementValue("instructions", instructions);
	}

	public int getRowsCount() throws XMLException {
		return getChildren("item").size();
	}

	/**
	 * Returns title.
	 *
	 * @return title
	 */
	public String getTitle() throws XMLException {
		return getChildElementValue("title");
	}

	/**
	 * Sets form title.
	 *
	 * @param title title
	 */
	public void setTitle(String title) throws XMLException {
		setChildElementValue("title", title);
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

	private void updateReported() throws XMLException {
		final Element r = getFirstChild("reported");
		if (r == null) {
			return;
		}

		for (Element item : getChildren("item")) {
			for (Element field : item.getChildren("field")) {
				Element rf = findReportedField(r, field.getAttribute("var"));
				rf.setAttribute("label", field.getAttribute("label"));
				field.removeAttribute("label");
			}
		}
	}

}