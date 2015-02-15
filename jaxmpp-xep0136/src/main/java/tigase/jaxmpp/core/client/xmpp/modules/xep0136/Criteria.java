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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.core.client.xmpp.utils.RSM;

/**
 *
 * @author andrzej
 */
public class Criteria {

	private static final String QUERY_XMLNS = "http://tigase.org/protocol/archive#query";
	
	private static final DateTimeFormat format = new DateTimeFormat();
	
	private JID with;
	private Date startTimestamp;
	private Date endTimestamp;
	private RSM rsm = new RSM();
	private Set<String> tags = new HashSet<String>();
	private Set<String> contains = new HashSet<String>();
	
	public Criteria() {
		
	}
	
	public Criteria setWith(JID jid) {
		this.with = jid;
		return this;
	}
	
	public JID getWith() {
		return with;
	}
	
	public Criteria setStart(Date date) {
		this.startTimestamp = date;
		return this;
	}
	
	public Date getStart() {
		return startTimestamp;
	}
	
	public Criteria setEnd(Date date) {
		this.endTimestamp = date;
		return this;
	}
	
	public Date getEnd() {
		return endTimestamp;
	}

	public Criteria addContains(String... contains) {
		for (String contain : contains) {
			this.contains.add(contain);
		}
		return this;
	}
	
	public Criteria setContains(Collection<String> contains) {
		this.contains.clear();
		if (contains != null)
			this.contains.addAll(contains);
		return this;
	}
	
	public Criteria addTags(String... tags) {
		for (String tag : tags) {
			this.tags.add(tag);
		}
		return this;
	}
	
	public Criteria setTags(Collection<String> tags) {
		this.tags.clear();
		if (tags != null)
			this.tags.addAll(tags);
		return this;
	}
	
	public Criteria setBefore(String before) {
		rsm.setBefore(before);
		return this;
	}
	
	public Criteria setAfter(String after) {
		rsm.setAfter(after);
		return this;
	}
	
	public Criteria setLastPage(Boolean val) {
		rsm.setLastPage(val);
		return this;
	}

	public Criteria setIndex(Integer index) {
		rsm.setIndex(index);
		return this;
	}	
	
	public Integer getLimit() {
		return rsm.getMax();
	}
	
	public Criteria setLimit(Integer limit) {
		rsm.setMax(limit);
		return this;
	}
	
	public void toElement(Element e) throws XMLException {
		if (with != null)
			e.setAttribute("with", with.toString());
		
		if (startTimestamp != null)
			e.setAttribute("start", format.format(startTimestamp));
		if (endTimestamp != null)
			e.setAttribute("end", format.format(endTimestamp));
		
		if (!tags.isEmpty() || !contains.isEmpty()) {
			Element query = ElementFactory.create("query", null, QUERY_XMLNS);
			for (String tag : tags) {
				query.addChild(ElementFactory.create("tag", tag, null));
			}
			for (String contain : contains) {
				query.addChild(ElementFactory.create("contains", contain, null));
			}
			e.addChild(query);
		}
		
		Element rsmEl = rsm.toElement();
		if (rsmEl != null)
			e.addChild(rsmEl);
	}
}
