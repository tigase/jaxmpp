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

import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.InitializingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ObservableAware;
import tigase.jaxmpp.core.client.xmpp.modules.PacketWriterAware;

/**
 * XMPP Modules Manager. This manager finds correct module to handle given
 * incoming stanza.
 * 
 * @author bmalkow
 */
public class XmppModulesManager {

	private boolean initialized = false;

	private final ArrayList<XmppModule> modules = new ArrayList<XmppModule>();

	private final HashMap<Class<XmppModule>, XmppModule> modulesByClasses = new HashMap<Class<XmppModule>, XmppModule>();

	private PacketWriter packetWriter;

	private Observable parentObservable;

	public XmppModulesManager(Observable parent, PacketWriter packetWriter) {
		this.parentObservable = parent;
		this.packetWriter = packetWriter;
	}

	/**
	 * Finds collection of modules that can handle stanza.
	 * 
	 * @param element
	 *            incoming stanza.
	 * @return list of modules that can handle stanza.
	 * @throws XMLException
	 */
	public List<XmppModule> findModules(final Element element) throws XMLException {
		List<XmppModule> results = null;
		for (XmppModule plugin : modules) {
			if (plugin.getCriteria() != null && plugin.getCriteria().match(element)) {
				if (results == null) {
					results = new ArrayList<XmppModule>();
				}
				results.add(plugin);
			}
		}
		return results;
	}

	/**
	 * Returns all features registered by modules.
	 * 
	 * @return Set of features.
	 */
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

	/**
	 * Return module implementation by module class.
	 * 
	 * @param moduleClass
	 *            module class
	 * @return module implementation
	 */
	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T getModule(Class<T> moduleClass) {
		return (T) this.modulesByClasses.get(moduleClass);
	}

	public void init() {
		this.initialized = true;
		for (XmppModule mod : this.modules) {
			if (mod instanceof InitializingModule) {
				((InitializingModule) mod).afterRegister();
			}
		}
	}

	/**
	 * Register XmppModule.
	 * 
	 * @param plugin
	 *            module
	 * @return module
	 */
	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T register(T plugin) {
		if (plugin instanceof ObservableAware) {
			Observable observable = ObservableFactory.instance(parentObservable);
			((ObservableAware) plugin).setObservable(observable);
		}

		if (plugin instanceof PacketWriterAware) {
			((PacketWriterAware) plugin).setPacketWriter(packetWriter);
		}

		if (plugin instanceof InitializingModule) {
			((InitializingModule) plugin).beforeRegister();
		}

		this.modulesByClasses.put((Class<XmppModule>) plugin.getClass(), plugin);
		this.modules.add(plugin);

		if (initialized && plugin instanceof InitializingModule) {
			((InitializingModule) plugin).afterRegister();
		}

		return plugin;
	}

	/**
	 * Unregisters module.
	 * 
	 * @param plugin
	 *            module to unregister
	 * @return unregistered module. <code>null</code> if module wasn't
	 *         registered.
	 */
	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T unregister(final T plugin) {
		if (plugin instanceof InitializingModule) {
			((InitializingModule) plugin).beforeUnregister();
		}
		this.modules.remove(plugin);
		return (T) this.modulesByClasses.remove(plugin.getClass());
	}

}