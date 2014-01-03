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

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.Event;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventListener;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule.SaslError;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule.UnsupportedSaslMechanisms;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

/**
 * Module used for authentication. This module automatically selects better
 * authentication module to use. Currently it choose between
 * {@linkplain SaslModule} and {@linkplain NonSaslAuthModule}.
 */
public class AuthModule implements XmppModule {

	public interface AuthFailedHandler extends EventHandler {

		public static class AuthFailedEvent extends JaxmppEvent<AuthFailedHandler> {

			private SaslError error;

			public AuthFailedEvent(SessionObject sessionObject, SaslError error) {
				super(sessionObject);
				this.error = error;
			}

			@Override
			protected void dispatch(AuthFailedHandler handler) {
				handler.onAuthFailed(sessionObject, error);
			}

			public SaslError getError() {
				return error;
			}

			public void setError(SaslError error) {
				this.error = error;
			}

		}

		void onAuthFailed(SessionObject sessionObject, SaslError error);
	}

	public interface AuthStartHandler extends EventHandler {

		public static class AuthStartEvent extends JaxmppEvent<AuthStartHandler> {

			public AuthStartEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(AuthStartHandler handler) {
				handler.onAuthStart(sessionObject);
			}

		}

		void onAuthStart(SessionObject sessionObject);
	}

	public interface AuthSuccessHandler extends EventHandler {

		public static class AuthSuccessEvent extends JaxmppEvent<AuthSuccessHandler> {

			public AuthSuccessEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(AuthSuccessHandler handler) {
				handler.onAuthSuccess(sessionObject);
			}

		}

		void onAuthSuccess(SessionObject sessionObject);
	}

	public static class DefaultCredentialsCallback implements CredentialsCallback {

		private final SessionObject sessionObject;

		public DefaultCredentialsCallback(SessionObject sessionObject) {
			this.sessionObject = sessionObject;
		}

		@Override
		public String getCredential() {
			return sessionObject.getProperty(SessionObject.PASSWORD);
		}

	}

	public static final String AUTHORIZED = "jaxmpp#authorized";

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

	private final Context context;

	private final Logger log;

	private final XmppModulesManager moduleManager;

	public AuthModule(Context context, XmppModulesManager modulesManager) {
		this.context = context;
		this.moduleManager = modulesManager;
		this.log = Logger.getLogger(this.getClass().getName());

		context.getEventBus().addHandler(SaslModule.SaslAuthFailedHandler.SaslAuthFailedEvent.class,
				new SaslModule.SaslAuthFailedHandler() {

					@Override
					public void onAuthFailed(SessionObject sessionObject, SaslError error) {
						AuthModule.this.context.getEventBus().fire(
								new AuthFailedHandler.AuthFailedEvent(AuthModule.this.context.getSessionObject(), error));
					}
				});
		context.getEventBus().addHandler(SaslModule.SaslAuthStartHandler.SaslAuthStartEvent.class,
				new SaslModule.SaslAuthStartHandler() {

					@Override
					public void onAuthStart(SessionObject sessionObject, String mechanismName) {
						AuthModule.this.context.getEventBus().fire(
								new AuthStartHandler.AuthStartEvent(AuthModule.this.context.getSessionObject()));
					}
				});
		context.getEventBus().addHandler(SaslModule.SaslAuthSuccessHandler.SaslAuthSuccessEvent.class,
				new SaslModule.SaslAuthSuccessHandler() {

					@Override
					public void onAuthSuccess(SessionObject sessionObject) {
						AuthModule.this.context.getEventBus().fire(
								new AuthSuccessHandler.AuthSuccessEvent(AuthModule.this.context.getSessionObject()));
					}
				});

		context.getEventBus().addHandler(NonSaslAuthModule.NonSaslAuthFailedHandler.NonSaslAuthFailedEvent.class,
				new NonSaslAuthModule.NonSaslAuthFailedHandler() {

					@Override
					public void onAuthFailed(SessionObject sessionObject, ErrorCondition errorCondition) {
						SaslError error;

						if (errorCondition == ErrorCondition.not_authorized) {
							error = SaslError.not_authorized;
						} else {
							error = SaslError.temporary_auth_failure;
						}

						AuthModule.this.context.getEventBus().fire(
								new AuthFailedHandler.AuthFailedEvent(AuthModule.this.context.getSessionObject(), error));
					}
				});
		context.getEventBus().addHandler(NonSaslAuthModule.NonSaslAuthStartHandler.NonSaslAuthStartEvent.class,
				new NonSaslAuthModule.NonSaslAuthStartHandler() {

					@Override
					public void onAuthStart(SessionObject sessionObject, IQ iq) {
						AuthModule.this.context.getEventBus().fire(
								new AuthStartHandler.AuthStartEvent(AuthModule.this.context.getSessionObject()));
					}
				});
		context.getEventBus().addHandler(NonSaslAuthModule.NonSaslAuthSuccessHandler.NonSaslAuthSuccessEvent.class,
				new NonSaslAuthModule.NonSaslAuthSuccessHandler() {

					@Override
					public void onAuthSuccess(SessionObject sessionObject) {
						AuthModule.this.context.getEventBus().fire(
								new AuthSuccessHandler.AuthSuccessEvent(AuthModule.this.context.getSessionObject()));
					}
				});
	}

	public void addAuthFailedHandler(AuthFailedHandler handler) {
		context.getEventBus().addHandler(AuthFailedHandler.AuthFailedEvent.class, handler);
	}

	public void addAuthStartHandler(AuthStartHandler handler) {
		context.getEventBus().addHandler(AuthStartHandler.AuthStartEvent.class, handler);
	}

	public void addAuthSuccessHandler(AuthSuccessHandler handler) {
		context.getEventBus().addHandler(AuthSuccessHandler.AuthSuccessEvent.class, handler);
	}

	public <H extends EventHandler> void addListener(Class<? extends Event<H>> type, EventListener listener) {
		context.getEventBus().addListener(type, listener);
	}

	public <H extends EventHandler> void addListener(EventListener listener) {
		context.getEventBus().addListener(listener);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public void login() throws JaxmppException {
		final SaslModule saslModule = moduleManager.getModule(SaslModule.class);
		final NonSaslAuthModule nonSaslModule = moduleManager.getModule(NonSaslAuthModule.class);

		final Boolean forceNonSasl = context.getSessionObject().getProperty(FORCE_NON_SASL);

		final Element features = context.getSessionObject().getStreamFeatures();
		boolean saslSupported = saslModule != null && (forceNonSasl == null || !forceNonSasl.booleanValue())
				&& features != null && features.getChildrenNS("mechanisms", "urn:ietf:params:xml:ns:xmpp-sasl") != null;
		boolean nonSaslSupported = nonSaslModule != null
				&& (!saslSupported || features == null || features.getChildrenNS("auth", "http://jabber.org/features/iq-auth") != null);

		if (log.isLoggable(Level.FINER))
			log.finer("Authenticating with " + (saslSupported ? "SASL" : "-") + " " + (nonSaslSupported ? "Non-SASL" : "-"));

		try {
			if (saslSupported) {
				saslModule.login();
			} else if (nonSaslSupported) {
				nonSaslModule.login();
			} else
				throw new JaxmppException("Both authentication methods are forbidden");
		} catch (UnsupportedSaslMechanisms e) {
			if (nonSaslModule == null || !nonSaslSupported)
				throw e;
			nonSaslModule.login();
		}

	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
	}

	public void remove(Class<? extends Event<?>> type, EventHandler handler) {
		context.getEventBus().remove(type, handler);
	}

	public void remove(EventHandler handler) {
		context.getEventBus().remove(handler);
	}

	public void removeAuthFailedHandler(AuthFailedHandler handler) {
		context.getEventBus().remove(AuthFailedHandler.AuthFailedEvent.class, handler);
	}

	public void removeAuthStartHandler(AuthStartHandler handler) {
		context.getEventBus().remove(AuthStartHandler.AuthStartEvent.class, handler);
	}

	public void removeAuthSuccessHandler(AuthSuccessHandler handler) {
		context.getEventBus().remove(AuthSuccessHandler.AuthSuccessEvent.class, handler);
	}

}