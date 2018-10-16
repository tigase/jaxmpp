/*
 * Or.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.criteria;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Criteria implementation to realize OR operator.
 *
 * @author bmalkow
 */
public class Or
		implements Criteria {

	protected Criteria[] crits;

	public Or(Criteria... criterias) {
		this.crits = criterias;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Criteria add(Criteria criteria) {
		throw new RuntimeException("Or.add() is not implemented!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean match(Element element) throws XMLException {
		for (int i = 0; i < crits.length; i++) {
			Criteria c = this.crits[i];
			if (c.match(element)) {
				return true;
			}
		}
		return false;
	}

}