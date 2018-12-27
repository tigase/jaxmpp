/*
 * RSM.java
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
package tigase.jaxmpp.core.client.xmpp.utils;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.List;

/**
 * @author andrzej
 */
public class RSM {

	protected static final String XMLNS = "http://jabber.org/protocol/rsm";
	private static final int DEFAULT_MAX = 100;

	//~--- fields ---------------------------------------------------------------
	String after = null;
	String before = null;
	Integer count = null;
	String first = null;
	Integer index = null;
	String last = null;
	Integer max = null;
	boolean retrieveLastPage = false;

	//~--- constructors ---------------------------------------------------------

	/**
	 * Constructs ...
	 *
	 * @param e
	 */
	public RSM(int defaultMax) {
		this.max = defaultMax;
	}

	public RSM() {
	}

	//~--- get methods ----------------------------------------------------------

	public RSM fromElement(Element e) throws XMLException {
		if (e == null) {
			reset();
			return this;
		}
		if (e.getXMLNS() != XMLNS) {
			Element x = e.getChildrenNS("set", RSM.XMLNS);
			return fromElement(x);
		}
		reset();
		List<Element> children = e.getChildren();
		if (children != null) {
			for (Element child : children) {
				if (child.getName() == "first") {
					first = child.getValue();
					String indexStr = child.getAttribute("index");
					if (indexStr != null) {
						index = Integer.parseInt(indexStr);
					}
				} else if (child.getName() == "last") {
					last = child.getValue();
				} else if (child.getName() == "count") {
					count = Integer.parseInt(child.getValue());
				} else if (child.getName() == "max") {
					max = Integer.parseInt(child.getValue());
					after = child.getAttribute("after");
				}
			}
		}

		return this;
	}

	public Integer getCount() {
		return count;
	}

	/**
	 * Method description
	 *
	 * @return
	 */
	public String getFirst() {
		return first;
	}

	/**
	 * Method description
	 *
	 * @return
	 */
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	//~--- set methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 * @return
	 */
	public String getLast() {
		return last;
	}

	/**
	 * Method description
	 *
	 * @return
	 */
	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public void reset() {
		this.after = null;
		this.before = null;
		this.count = null;
		this.first = null;
		this.index = null;
		this.last = null;
		this.max = null;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	//~--- methods --------------------------------------------------------------

	public void setBefore(String before) {
		this.before = before;
	}

	public void setLastPage(boolean val) {
		this.retrieveLastPage = val;
	}

	/**
	 * Method description
	 *
	 * @return
	 *
	 * @throws tigase.jaxmpp.core.client.xml.XMLException
	 */
	public Element toElement() throws XMLException {
		Element set = ElementFactory.create("set");
		set.setXMLNS(XMLNS);

		if (after != null) {
			set.addChild(ElementFactory.create("after", after, null));
		}
		if (before != null || retrieveLastPage) {
			set.addChild(ElementFactory.create("before", before, null));
		}
		if (index != null) {
			set.addChild(ElementFactory.create("index", String.valueOf(index), null));
		}
		if (max != null) {
			set.addChild(ElementFactory.create("max", String.valueOf(max), null));
		}

		return set;
	}

}
