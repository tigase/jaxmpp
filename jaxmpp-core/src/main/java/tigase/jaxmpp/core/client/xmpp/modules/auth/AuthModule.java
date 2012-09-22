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
package tigase.jaxmpp.core.client.xmpp.modules.auth;

import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule.UnsupportedSaslMechanisms;

public class AuthModule implements XmppModule {

	public static class AuthEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public AuthEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

	}

	public static class DefaultCredentialsCallback implements CredentialsCallback {

		private final SessionObject sessionObject;

		public DefaultCredentialsCallback(SessionObject sessionObject) {
			this.sessionObject = sessionObject;
		}

		@Override
		public String getPassword() {
			return sessionObject.getProperty(SessionObject.PASSWORD);
		}

	}

	public final static EventType AuthFailed = new EventType();

	public static final String AUTHORIZED = "jaxmpp#authorized";

	public final static EventType AuthStart = new EventType();

	public final static EventType AuthSuccess = new EventType();

	public static final String CREDENTIALS_CALLBACK = "jaxmpp#credentialsCallback";

	/**
	 * If <code>true</code> then Non-SASL (<a
	 * href='http://xmpp.org/extensions/xep-0078.html'>XEP-0078</a>) mechanism
	 * is used.<br/>
	 * Type: {@link Boolean Boolean}
	 */
	public static final String FORCE_NON_SASL = "jaxmpp#forceNonSASL";

	public static boolean isAuthAvailable(final SessionObject sessionObject) throws XMLException {
		final Element features = sessionObject.getStreamFeatures();

		boolean saslSupported = features != null
				&& features.getChildrenNS("mechanisms", "urn:ietf:params:xml:ns:xmpp-sasl") != null;
		boolean nonSaslSupported = features != null
				&& features.getChildrenNS("auth", "http://jabber.org/features/iq-auth") != null;

		return saslSupported || nonSaslSupported;
	}

	private final Logger log;

	private XmppModulesManager modulesManager;

	private final Observable observable;

	private final SessionObject sessionObject;

	public AuthModule(Observable parentObservable, SessionObject sessionObject, XmppModulesManager modulesManager) {
		this.observable = ObservableFactory.instance(parentObservable);
		this.modulesManager = modulesManager;
		this.sessionObject = sessionObject;
		this.log = Logger.getLogger(this.getClass().getName());
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public Observable getObservable() {
		return observable;
	}

	public void login() throws JaxmppException {
		final SaslModule saslModule = modulesManager.getModule(SaslModule.class);
		final NonSaslAuthModule nonSaslModule = modulesManager.getModule(NonSaslAuthModule.class);

		final Boolean forceNonSasl = sessionObject.getProperty(FORCE_NON_SASL);

		final Element features = sessionObject.getStreamFeatures();
		boolean saslSupported = (forceNonSasl == null || !forceNonSasl.booleanValue()) && features != null
				&& features.getChildrenNS("mechanisms", "urn:ietf:params:xml:ns:xmpp-sasl") != null;
		boolean nonSaslSupported = !saslSupported || features == null
				|| features.getChildrenNS("auth", "http://jabber.org/features/iq-auth") != null;

		if (log.isLoggable(Level.FINER))
			log.finer("Authenticating with " + (saslSupported ? "SASL" : "-") + " " + (nonSaslSupported ? "Non-SASL" : "-"));

		try {
			if (saslSupported)
				saslModule.login();
			else if (nonSaslSupported)
				nonSaslModule.login();
		} catch (UnsupportedSaslMechanisms e) {
			if (nonSaslModule == null || !nonSaslSupported)
				throw e;
			nonSaslModule.login();
		}

	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}
}