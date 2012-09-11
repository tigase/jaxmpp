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
package tigase.jaxmpp.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class XmppModulesManager {

	public static interface InitializingBean {

		void init() throws JaxmppException;

	}

	private final ArrayList<XmppModule> modules = new ArrayList<XmppModule>();

	private final HashMap<Class<XmppModule>, XmppModule> modulesByClasses = new HashMap<Class<XmppModule>, XmppModule>();

	public List<XmppModule> findModules(final Element element) throws XMLException {
		List<XmppModule> results = null;
		for (XmppModule plugin : modules) {
			if (plugin.getCriteria() != null && plugin.getCriteria().match(element)) {
				if (results == null) {
					results = new ArrayList<XmppModule>();
				}
				results.add(plugin);
				break;
			}
		}
		return results;
	}

	public Set<String> getAvailableFeatures() {
		HashSet<String> result = new HashSet<String>();
		for (XmppModule plugin : this.modules) {
			final String[] fs = plugin.getFeatures();
			if (fs != null) {
				for (String string : fs) {
					result.add(string);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T getModule(Class<T> moduleClass) {
		return (T) this.modulesByClasses.get(moduleClass);
	}

	public void init() {
		for (XmppModule mod : this.modules) {
			if (mod instanceof InitializingBean) {
				try {
					((InitializingBean) mod).init();
				} catch (JaxmppException e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T register(T plugin) {
		this.modulesByClasses.put((Class<XmppModule>) plugin.getClass(), plugin);
		this.modules.add(plugin);
		return plugin;
	}

	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T unregister(T plugin) {
		this.modulesByClasses.remove(plugin.getClass());
		this.modules.remove(plugin);
		return plugin;
	}

}