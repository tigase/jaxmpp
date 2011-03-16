package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.XMLException;

public interface Field<T> {

	public String getDesc() throws XMLException;

	T getFieldValue() throws XMLException;

	public String getLabel() throws XMLException;

	public String getType() throws XMLException;

	public String getVar() throws XMLException;

	public boolean isRequired() throws XMLException;

	public void setDesc(String desc) throws XMLException;

	void setFieldValue(T value) throws XMLException;

	public void setLabel(String label) throws XMLException;

	public void setRequired(boolean value) throws XMLException;

	public void setVar(String var) throws XMLException;
}
