/*
 * BoshXmppSessionLogic.java
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
package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindErrorHandler;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindSuccessHandler;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthFailedHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthSuccessHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule.SaslError;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;

public class BoshXmppSessionLogic
		implements XmppSessionLogic {

	private final AuthModule.AuthFailedHandler authFailedHandler;
	private final AuthModule.AuthSuccessHandler authSuccessHandler;
	private final Connector connector;
	private final Context context;
	private final XmppModulesManager modulesManager;
	private final ResourceBindErrorHandler resourceBindErrorHandler;
	private final ResourceBindSuccessHandler resourceBindSuccessHandler;
	private final StreamFeaturesReceivedHandler streamFeaturesReceivedHandler;
	private AuthModule authModule;
	private StreamFeaturesModule featuresModule;
	private ResourceBinderModule resourceBinder;
	private SessionListener sessionListener;

	public BoshXmppSessionLogic(Context context, Connector connector, XmppModulesManager modulesManager) {
		this.context = context;
		this.connector = connector;
		this.modulesManager = modulesManager;

		this.streamFeaturesReceivedHandler = new StreamFeaturesReceivedHandler() {

			@Override
			public void onStreamFeaturesReceived(SessionObject sessionObject, Element featuresElement) {
				try {
					BoshXmppSessionLogic.this.onStreamFeaturesReceived(sessionObject, featuresElement);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};

		this.authSuccessHandler = new AuthSuccessHandler() {

			@Override
			public void onAuthSuccess(SessionObject sessionObject) {
				try {
					processAuthSucess(sessionObject);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};
		this.authFailedHandler = new AuthFailedHandler() {

			@Override
			public void onAuthFailed(SessionObject sessionObject, SaslError error) {
				try {
					processAuthFail(sessionObject, error);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};

		this.resourceBindErrorHandler = new ResourceBindErrorHandler() {

			@Override
			public void onResourceBindError(SessionObject sessionObject, ErrorCondition errorCondition) {
				BoshXmppSessionLogic.this.onResourceBindError(sessionObject, sessionObject);
			}
		};
		this.resourceBindSuccessHandler = new ResourceBindSuccessHandler() {

			@Override
			public void onResourceBindSuccess(SessionObject sessionObject, JID bindedJid) {
				try {
					BoshXmppSessionLogic.this.onResourceBindSuccess(sessionObject, bindedJid);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};
	}

	@Override
	public void beforeStart() throws JaxmppException {
	}

	protected void onResourceBindError(SessionObject sessionObject2, SessionObject sessionObject3) {
	}

	protected void onResourceBindSuccess(SessionObject sessionObject, JID bindedJid) throws JaxmppException {
		DiscoveryModule discovery = this.modulesManager.getModule(DiscoveryModule.class);
		if (discovery != null) {
			discovery.discoverServerFeatures(null);
		}

		context.getEventBus().fire(new XmppSessionEstablishedHandler.XmppSessionEstablishedEvent(sessionObject));
	}

	protected void onStreamFeaturesReceived(SessionObject sessionObject, Element featuresElement)
			throws JaxmppException {
		if (sessionObject.getProperty(AuthModule.AUTHORIZED) != Boolean.TRUE) {
			authModule.login();
		} else if (sessionObject.getProperty(AuthModule.AUTHORIZED) == Boolean.TRUE) {
			resourceBinder.bind();
		}
	}

	protected void processAuthFail(SessionObject sessionObject, SaslError error) throws JaxmppException {
		throw new JaxmppException("Unauthorized with condition=" + error);
	}

	protected void processAuthSucess(SessionObject sessionObject) throws JaxmppException {
		sessionObject.setProperty(Scope.stream, AuthModule.AUTHORIZED, Boolean.TRUE);
		connector.restartStream();
	}

	protected void processException(JaxmppException e) {
		try {
			if (sessionListener != null) {
				sessionListener.onException(e);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setSessionListener(SessionListener sessionListener) throws JaxmppException {
		this.sessionListener = sessionListener;
		featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		authModule = this.modulesManager.getModule(AuthModule.class);
		resourceBinder = this.modulesManager.getModule(ResourceBinderModule.class);

		featuresModule.addStreamFeaturesReceivedHandler(streamFeaturesReceivedHandler);
		authModule.addAuthSuccessHandler(authSuccessHandler);
		authModule.addAuthFailedHandler(authFailedHandler);

		resourceBinder.addResourceBindSuccessHandler(resourceBindSuccessHandler);
		resourceBinder.addResourceBindErrorHandler(resourceBindErrorHandler);
	}

	@Override
	public void unbind() throws JaxmppException {
		featuresModule.removeStreamFeaturesReceivedHandler(streamFeaturesReceivedHandler);
		authModule.removeAuthSuccessHandler(authSuccessHandler);
		authModule.removeAuthFailedHandler(authFailedHandler);
		resourceBinder.removeResourceBindSuccessHandler(resourceBindSuccessHandler);
		resourceBinder.removeResourceBindErrorHandler(resourceBindErrorHandler);
	}

}