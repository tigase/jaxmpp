/*
 * AbstractSocketXmppSessionLogic.java
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
package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindSuccessHandler;
import tigase.jaxmpp.core.client.xmpp.modules.SessionEstablishmentModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthFailedHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthSuccessHandler;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule.SaslError;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule;

public class AbstractSocketXmppSessionLogic<T extends Connector>
		implements XmppSessionLogic,
				   StreamManagementModule.StreamManagementFailedHandler,
				   StreamManagementModule.StreamResumedHandler {

	protected final T connector;
	protected final Context context;
	private final AuthFailedHandler authFailedHandler;
	private final AuthSuccessHandler authSuccessHandler;
	private final Connector.ErrorHandler connectorListener;
	private final XmppModulesManager modulesManager;
	private final SessionEstablishmentModule.SessionEstablishmentErrorHandler sessionEstablishmentErrorHandler;
	private final SessionEstablishmentModule.SessionEstablishmentSuccessHandler sessionEstablishmentSuccessHandler;
	private final StreamFeaturesReceivedHandler streamFeaturesEventListener;
	private AuthModule authModule;
	private StreamFeaturesModule featuresModule;
	private ResourceBindSuccessHandler resourceBindListener;
	private ResourceBinderModule resourceBinder;
	private SessionEstablishmentModule sessionEstablishmentModule;
	private SessionListener sessionListener;
	private StreamManagementModule streamManaegmentModule;

	static Throwable extractCauseException(Throwable ex) {
		Throwable th = ex.getCause();
		if (th == null) {
			return ex;
		}

		for (int i = 0; i < 4; i++) {
			if (!(th instanceof JaxmppException)) {
				return th;
			}
			if (th.getCause() == null) {
				return th;
			}
			th = th.getCause();
		}
		return ex;
	}

	protected AbstractSocketXmppSessionLogic(T connector, XmppModulesManager modulesManager, Context context) {
		this.connector = connector;
		this.modulesManager = modulesManager;
		this.context = context;

		this.connectorListener = new Connector.ErrorHandler() {

			@Override
			public void onError(SessionObject sessionObject, StreamError condition, Throwable caught)
					throws JaxmppException {
				AbstractSocketXmppSessionLogic.this.processConnectorErrors(condition, caught);
			}
		};

		this.streamFeaturesEventListener = new StreamFeaturesReceivedHandler() {

			@Override
			public void onStreamFeaturesReceived(SessionObject sessionObject, Element featuresElement)
					throws JaxmppException {
				AbstractSocketXmppSessionLogic.this.processStreamFeatures(featuresElement);
			}
		};

		this.authFailedHandler = new AuthFailedHandler() {

			@Override
			public void onAuthFailed(SessionObject sessionObject, SaslError error) throws JaxmppException {
				try {
					AbstractSocketXmppSessionLogic.this.processAuthFailed(error);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};
		this.authSuccessHandler = new AuthSuccessHandler() {

			@Override
			public void onAuthSuccess(SessionObject sessionObject) throws JaxmppException {
				AbstractSocketXmppSessionLogic.this.processAuthSuccess();
			}
		};
		this.resourceBindListener = new ResourceBindSuccessHandler() {

			@Override
			public void onResourceBindSuccess(SessionObject sessionObject, JID bindedJid) throws JaxmppException {
				AbstractSocketXmppSessionLogic.this.processResourceBindEvent(sessionObject, bindedJid);
			}
		};
		this.sessionEstablishmentErrorHandler = new SessionEstablishmentModule.SessionEstablishmentErrorHandler() {

			@Override
			public void onSessionEstablishmentError(SessionObject sessionObject, ErrorCondition error)
					throws JaxmppException {
				// FIXME
				sessionBindedAndEstablished(sessionObject);
			}
		};
		this.sessionEstablishmentSuccessHandler = new SessionEstablishmentModule.SessionEstablishmentSuccessHandler() {

			@Override
			public void onSessionEstablishmentSuccess(SessionObject sessionObject) throws JaxmppException {
				sessionBindedAndEstablished(sessionObject);
			}
		};
	}

	@Override
	public void beforeStart() throws JaxmppException {
		if (context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME) == null &&
				context.getSessionObject().getProperty(SessionObject.USER_BARE_JID) == null) {
			throw new JaxmppException("No user JID or server name specified");
		}

		if (context.getSessionObject().getProperty(SessionObject.DOMAIN_NAME) == null) {
			context.getSessionObject()
					.setProperty(SessionObject.DOMAIN_NAME, ((BareJID) context.getSessionObject()
							.getProperty(SessionObject.USER_BARE_JID)).getDomain());
		}
	}

	@Override
	public void onStreamManagementFailed(SessionObject sessionObject, ErrorCondition condition) {
		try {
			resourceBinder.bind();
		} catch (JaxmppException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStreamResumed(SessionObject sessionObject, Long h, String previd) throws JaxmppException {
		AbstractSocketXmppSessionLogic.this.context.getEventBus()
				.fire(new XmppSessionEstablishedHandler.XmppSessionEstablishedEvent(sessionObject));
	}

	protected void processAuthFailed(SaslError error) throws JaxmppException {
		throw new JaxmppException("Unauthorized with condition=" + error);
	}

	protected void processAuthSuccess() throws JaxmppException {
		connector.restartStream();
	}

	protected void processConnectorErrors(StreamError condition, Throwable caught) throws JaxmppException {
		if (caught != null) {
			Throwable e1 = extractCauseException(caught);
			JaxmppException e = (JaxmppException) (e1 instanceof JaxmppException ? e1 : new JaxmppException(e1));
			processException(e);
		}
	}

	protected void processException(JaxmppException e) throws JaxmppException {
		if (sessionListener != null) {
			sessionListener.onException(e);
		}
	}

	protected void processResourceBindEvent(SessionObject sessionObject, JID bindedJid) throws JaxmppException {
		if (SessionEstablishmentModule.isSessionEstablishingAvailable(context.getSessionObject())) {
			modulesManager.getModule(SessionEstablishmentModule.class).establish();
		} else {
			sessionBindedAndEstablished(sessionObject);
		}
	}

	protected void processStreamFeatures(Element featuresElement) throws JaxmppException {
		// final Boolean tlsDisabled =
		// context.getSessionObject().getProperty(SocketConnector.TLS_DISABLED_KEY);
		final boolean authAvailable = AuthModule.isAuthAvailable(context.getSessionObject());
		// final boolean tlsAvailable =
		// SocketConnector.isTLSAvailable(context.getSessionObject());
		// final Boolean compressionDisabled =
		// context.getSessionObject().getProperty(SocketConnector.COMPRESSION_DISABLED_KEY);
		// final boolean zlibAvailable =
		// SocketConnector.isZLibAvailable(context.getSessionObject());

		final boolean isAuthorized = context.getSessionObject().getProperty(AuthModule.AUTHORIZED) == Boolean.TRUE;
		final boolean isConnectionSecure = connector.isSecure();
		final boolean isConnectionCompressed = connector.isCompressed();

		final boolean resumption = StreamManagementModule.isStreamManagementAvailable(context.getSessionObject()) &&
				StreamManagementModule.isResumptionEnabled(context.getSessionObject());

		// if (!isConnectionSecure && tlsAvailable && (tlsDisabled == null ||
		// !tlsDisabled)) {
		// connector.startTLS();
		// } else if (!isConnectionCompressed && zlibAvailable &&
		// (compressionDisabled == null || !compressionDisabled)) {
		// connector.startZLib();
		// } else
		if (!isAuthorized && authAvailable) {
			authModule.login();
		} else if (isAuthorized && resumption) {
			streamManaegmentModule.resume();
		} else if (isAuthorized) {
			resourceBinder.bind();
		}
	}

	private void sessionBindedAndEstablished(SessionObject sessionObject) throws JaxmppException {
		DiscoveryModule discoInfo = this.modulesManager.getModule(DiscoveryModule.class);
		if (discoInfo != null) {
			discoInfo.discoverServerFeatures(null);
		}

		context.getEventBus().fire(new XmppSessionEstablishedHandler.XmppSessionEstablishedEvent(sessionObject));

		if (StreamManagementModule.isStreamManagementAvailable(context.getSessionObject())) {
			if (context.getSessionObject().getProperty(StreamManagementModule.STREAM_MANAGEMENT_DISABLED_KEY) == null ||
					!((Boolean) context.getSessionObject()
							.getProperty(StreamManagementModule.STREAM_MANAGEMENT_DISABLED_KEY)).booleanValue()) {
				StreamManagementModule streamManagement = this.modulesManager.getModule(StreamManagementModule.class);
				streamManagement.enable();
			}
		}
	}

	@Override
	public void setSessionListener(SessionListener sessionListener) throws JaxmppException {
		this.sessionListener = sessionListener;
		featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		authModule = this.modulesManager.getModule(AuthModule.class);
		resourceBinder = this.modulesManager.getModule(ResourceBinderModule.class);
		this.sessionEstablishmentModule = this.modulesManager.getModule(SessionEstablishmentModule.class);
		this.streamManaegmentModule = this.modulesManager.getModule(StreamManagementModule.class);

		context.getEventBus().addHandler(Connector.ErrorHandler.ErrorEvent.class, connectorListener);
		featuresModule.addStreamFeaturesReceivedHandler(streamFeaturesEventListener);
		authModule.addAuthSuccessHandler(authSuccessHandler);
		authModule.addAuthFailedHandler(authFailedHandler);
		resourceBinder.addResourceBindSuccessHandler(resourceBindListener);

		this.sessionEstablishmentModule.addSessionEstablishmentErrorHandler(sessionEstablishmentErrorHandler);
		this.sessionEstablishmentModule.addSessionEstablishmentSuccessHandler(sessionEstablishmentSuccessHandler);

		this.context.getEventBus()
				.addHandler(StreamManagementModule.StreamResumedHandler.StreamResumedEvent.class, this);
		this.context.getEventBus()
				.addHandler(StreamManagementModule.StreamManagementFailedHandler.StreamManagementFailedEvent.class,
							this);
	}

	@Override
	public void unbind() throws JaxmppException {
		context.getEventBus().remove(Connector.ErrorHandler.ErrorEvent.class, connectorListener);
		featuresModule.removeStreamFeaturesReceivedHandler(streamFeaturesEventListener);
		authModule.removeAuthSuccessHandler(authSuccessHandler);
		authModule.removeAuthFailedHandler(authFailedHandler);
		resourceBinder.removeResourceBindSuccessHandler(resourceBindListener);

		this.sessionEstablishmentModule.removeSessionEstablishmentErrorHandler(sessionEstablishmentErrorHandler);
		this.sessionEstablishmentModule.removeSessionEstablishmentSuccessHandler(sessionEstablishmentSuccessHandler);

		this.context.getEventBus().remove(StreamManagementModule.StreamResumedHandler.StreamResumedEvent.class, this);
		this.context.getEventBus()
				.remove(StreamManagementModule.StreamManagementFailedHandler.StreamManagementFailedEvent.class, this);
	}

}