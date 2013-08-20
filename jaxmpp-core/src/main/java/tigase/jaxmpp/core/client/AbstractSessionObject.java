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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

/**
 * Default representation of {@linkplain SessionObject}
 * 
 */
public abstract class AbstractSessionObject implements SessionObject {

	public static class ClearedEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Set<Scope> scopes;

		public ClearedEvent(SessionObject sessionObject, Set<Scope> scopes) {
			super(Cleared, sessionObject);
			this.scopes = scopes;
		}

		public Set<Scope> getScopes() {
			return scopes;
		}

		public void setScopes(Set<Scope> scopes) {
			this.scopes = scopes;
		}

	}

	protected class Entry {
		private Scope scope;
		private Object value;
	}

	public final static EventType Cleared = new EventType();

	private static final String STREAM_FEATURES_ELEMENT_KEY = "jaxmpp:internal:STREAM_FEATURES_ELEMENT";

	protected final Logger log = Logger.getLogger(this.getClass().getName());

	private final Observable observable = ObservableFactory.instance();

	protected PresenceStore presence;

	protected Map<String, Entry> properties;

	protected ResponseManager responseManager;

	protected RosterStore roster;

	protected AbstractSessionObject() {
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkHandlersTimeout() throws JaxmppException {
		this.responseManager.checkTimeouts();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() throws JaxmppException {
		clear((Set<Scope>) null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final Scope... scopes) throws JaxmppException {
		final Set<Scope> scopesSet = new HashSet<Scope>();
		if (scopes != null)
			for (Scope scope : scopes) {
				scopesSet.add(scope);
			}
		clear(scopesSet);
	}

	public synchronized void clear(Set<Scope> scopes) throws JaxmppException {
		log.fine("Clearing properties!");

		if (scopes == null || scopes.isEmpty()) {
			scopes = new HashSet<SessionObject.Scope>();
			scopes.add(Scope.session);
			scopes.add(Scope.stream);
		}

		if (scopes.contains(Scope.session)) {
			roster.clear();
			presence.clear();
		}

		Iterator<java.util.Map.Entry<String, Entry>> iterator = this.properties.entrySet().iterator();
		while (iterator.hasNext()) {
			java.util.Map.Entry<String, Entry> entry = iterator.next();
			if (scopes.contains(entry.getValue().scope))
				iterator.remove();
		}

		ClearedEvent event = new ClearedEvent(this, scopes);
		observable.fireEvent(event);
	}

	public void fireEvent(BaseEvent event) throws JaxmppException {
		observable.fireEvent(event);
	}

	public void fireEvent(EventType eventType, BaseEvent event) throws JaxmppException {
		observable.fireEvent(eventType, event);
	}

	public void fireEvent(EventType eventType, SessionObject sessionObject) throws JaxmppException {
		observable.fireEvent(eventType, sessionObject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JID getBindedJid() {
		return getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PresenceStore getPresence() {
		return presence;
	}

	public <T> T getProperty(Scope scope, String key) {
		Entry entry = this.properties.get(key);
		if (entry == null)
			return null;
		else if (scope == null || scope == entry.scope)
			return (T) entry.value;
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key) {
		return getProperty(null, key);
	}

	/**
	 * {@inheritDoc}
	 */
	public Runnable getResponseHandler(Element element, PacketWriter writer) throws JaxmppException {
		return responseManager.getResponseHandler(element, writer, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RosterStore getRoster() {
		return roster;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element getStreamFeatures() {
		return getProperty(Scope.stream, STREAM_FEATURES_ELEMENT_KEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BareJID getUserBareJid() {
		return this.getProperty(USER_BARE_JID);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getUserProperty(String key) {
		return getProperty(Scope.user, key);
	}

	public String registerResponseHandler(Element stanza, Long timeout, AsyncCallback callback) throws XMLException {
		return responseManager.registerResponseHandler(stanza, timeout, callback);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void removeListener(Listener<? extends BaseEvent> listener) {
		observable.removeListener(listener);
	}

	@Override
	public SessionObject setProperty(Scope scope, String key, Object value) {
		if (value == null) {
			this.properties.remove(key);
		} else {
			Entry e = this.properties.get(key);
			if (e == null) {
				e = new Entry();
				this.properties.put(key, e);
			}
			e.scope = scope;
			e.value = value;
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SessionObject setProperty(String key, Object value) {
		return setProperty(Scope.session, key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStreamFeatures(Element element) {
		setProperty(Scope.stream, STREAM_FEATURES_ELEMENT_KEY, element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserProperties setUserProperty(String key, Object value) {
		return setProperty(Scope.user, key, value);
	}

}