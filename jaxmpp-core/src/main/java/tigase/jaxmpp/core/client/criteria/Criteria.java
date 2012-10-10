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
package tigase.jaxmpp.core.client.criteria;

import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Base interface to check if given stanza may be processed by specific module.
 * This class is used by {@linkplain XmppModule} and
 * {@linkplain XmppModulesManager} to check what modules can handle received
 * stanza.
 * 
 * @author bmalkow
 * 
 */
public interface Criteria {

	/**
	 * Adds restriction for deeper level of elements.
	 * <p>
	 * For example:<br/>
	 * If you have structure of elements:
	 * <code>&lt;A&gt;&lt;B&gt;&lt;/B&gt;&lt;/A&gt;</code><br/>
	 * then, to check elements <code>A</code> and <code>B</code> you should use
	 * construction like this: <code>critToCheckA.add(critToCheckB);</code>
	 * <p>
	 * 
	 * 
	 * @param criteria
	 *            restriction to add
	 * @return
	 */
	Criteria add(Criteria criteria);

	/**
	 * This method checks if element match to conditions.
	 * 
	 * @param element
	 *            element to check
	 * @return <code>true</code> if element match.
	 */
	boolean match(Element element) throws XMLException;

}