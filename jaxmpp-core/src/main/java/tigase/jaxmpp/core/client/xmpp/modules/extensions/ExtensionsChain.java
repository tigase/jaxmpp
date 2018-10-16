/*
 * ExtensionsChain.java
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

package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import tigase.jaxmpp.core.client.xml.Element;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtensionsChain {

	// any method which tries to modify this collection needs to be synchronized and replace collection instead modify it directly
	private Collection<Extension> extensions = Collections.emptyList();

	private Logger log;

	public ExtensionsChain() {
		this.log = Logger.getLogger(this.getClass().getName());
	}

	public synchronized void addExtension(Extension f) {
		ArrayList<Extension> tmp = new ArrayList<>(extensions);
		tmp.add(f);
		this.extensions = Collections.unmodifiableCollection(tmp);
	}

	public Element executeAfterReceiveChain(final Element element) {
		Iterator<Extension> it = getExtensionIterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension x = it.next();
			try {
				e = x.afterReceive(e);
			} catch (Exception ex) {
				log.log(Level.WARNING, "Problem on calling afterReceive: " + ex.getMessage(), ex);
			}
		}
		return e;
	}

	public Element executeBeforeSendChain(final Element element) {
		Iterator<Extension> it = getExtensionIterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension x = it.next();
			try {
				e = x.beforeSend(e);
			} catch (Exception ex) {
				log.log(Level.WARNING, "Problem on calling beforeSend: " + ex.getMessage(), ex);
			}
		}
		return e;
	}

	public synchronized Collection<Extension> getExtension() {
		return extensions;
	}

	public <T extends Extension> T getExtension(Class<T> cls) {
		Iterator<Extension> it = getExtensionIterator();
		while (it.hasNext()) {
			Extension x = it.next();
			// in GWT there is no support for isAssignableFrom so following
			// breaks compilation of GWT project
			// if (cls.isAssignableFrom(x.getClass()))
			// I think that following will be ok for now
			if (cls.equals(x.getClass())) {
				return (T) x;
			}
		}
		return null;
	}

	public Collection<String> getFeatures() {
		HashSet<String> result = new HashSet<String>();
		Iterator<Extension> it = getExtensionIterator();
		while (it.hasNext()) {
			final String[] fs = it.next().getFeatures();
			if (fs != null) {
				for (String string : fs) {
					result.add(string);
				}
			}
		}
		return result;
	}

	public synchronized void removeExtension(Extension f) {
		ArrayList<Extension> tmp = new ArrayList<>(extensions);
		tmp.remove(f);
		this.extensions = Collections.unmodifiableCollection(tmp);
	}

	private synchronized Iterator<Extension> getExtensionIterator() {
		return this.extensions.iterator();
	}

}
