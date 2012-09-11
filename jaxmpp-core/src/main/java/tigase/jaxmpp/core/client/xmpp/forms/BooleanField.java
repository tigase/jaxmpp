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

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class BooleanField extends AbstractField<Boolean> {

	BooleanField(Element element) throws XMLException {
		super("boolean", element);
	}

	@Override
	public Boolean getFieldValue() throws XMLException {
		final String t = getChildElementValue("value");
		if (t == null)
			return Boolean.FALSE;
		else if (t.equals("1") || t.equals("true"))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}

	@Override
	public void setFieldValue(Boolean value) throws XMLException {
		if (value == null)
			setChildElementValue("value", "0");
		else if (value.booleanValue())
			setChildElementValue("value", "1");
		else
			setChildElementValue("value", "0");
	}
}