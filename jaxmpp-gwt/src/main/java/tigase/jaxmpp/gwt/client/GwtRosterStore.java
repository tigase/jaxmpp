/*
 * GwtRosterStore.java
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
package tigase.jaxmpp.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.roster.DefaultRosterStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem.Subscription;

import java.util.List;

/**
 * @author andrzej
 */
public class GwtRosterStore
		extends DefaultRosterStore
		implements JsonSerializationHelper.Serializable {

	@Override
	public Object fromJSON(JsonSerializationHelper helper, JavaScriptObject obj) throws JaxmppException {
		String type = JsonSerializationHelper.getStringFromObject(obj, "type");
		if (!getJsonType().equals(type)) {
			return null;
		}

		JavaScriptObject items = JsonSerializationHelper.getObjectFromObject(obj, "items");
		JsArrayString jids = JsonSerializationHelper.getKeysFromObject(items);
		for (int i = 0; i < jids.length(); i++) {
			String key = jids.get(i);
			BareJID jid = BareJID.bareJIDInstance(key);
			JavaScriptObject item = JsonSerializationHelper.getObjectFromObject(items, key);
			RosterItem ri = new RosterItem(jid, helper.getSessionObject());
			JsArrayString propNames = JsonSerializationHelper.getKeysFromObject(item);
			for (int j = 0; j < propNames.length(); j++) {
				String prop = propNames.get(j);
				if ("name".equals(prop)) {
					ri.setName(JsonSerializationHelper.getStringFromObject(item, prop));
				} else if ("sub".equals(prop)) {
					ri.setSubscription(Subscription.valueOf(JsonSerializationHelper.getStringFromObject(item, prop)));
				} else if ("ask".equals(prop)) {
					ri.setAsk(true);
				} else if ("groups".equals(prop)) {
					JsArrayString groups = (JsArrayString) JsonSerializationHelper.getObjectFromObject(item, prop);
					for (int k = 0; k < groups.length(); k++) {
						String group = groups.get(k);
						ri.getGroups().add(group);
					}
				}
				this.addItem(ri);
			}
		}

		return this;
	}

	@Override
	public String getJsonType() {
		return "rosterStore";
	}

	@Override
	public JavaScriptObject toJSON() throws XMLException {
		JavaScriptObject obj = JavaScriptObject.createObject();
		JavaScriptObject items = JavaScriptObject.createObject();
		List<RosterItem> rosterItems = this.getAll();
		for (RosterItem ri : rosterItems) {
			JavaScriptObject item = JavaScriptObject.createObject();
			if (ri.getName() != null && !ri.getName().isEmpty()) {
				JsonSerializationHelper.putToObject(item, "name", ri.getName());
			}
			Subscription sub = ri.getSubscription();
			if (sub != null && sub != Subscription.none) {
				JsonSerializationHelper.putToObject(item, "sub", sub.name());
			}
			if (ri.isAsk()) {
				JsonSerializationHelper.putToObject(item, "ask", "true");
			}
			if (!ri.getGroups().isEmpty()) {
				JavaScriptObject groups = JavaScriptObject.createArray();
				JsonSerializationHelper.putToObject(item, "groups", groups);
			}
			JsonSerializationHelper.putToObject(items, ri.getJid().toString(), item);
		}
		JsonSerializationHelper.putToObject(obj, "type", getJsonType());
		JsonSerializationHelper.putToObject(obj, "items", items);
		// maybe we should add version of roster to stored info?
		return obj;
	}

}
