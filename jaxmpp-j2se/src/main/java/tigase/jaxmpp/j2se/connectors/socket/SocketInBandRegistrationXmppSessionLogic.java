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
package tigase.jaxmpp.j2se.connectors.socket;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedHandler;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.NotSupportedErrorHandler;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.ReceivedErrorHandler;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule.ReceivedTimeoutHandler;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

public class SocketInBandRegistrationXmppSessionLogic implements XmppSessionLogic {

	private final SocketConnector connector;

	private StreamFeaturesModule featuresModule;

	private final XmppModulesManager modulesManager;

	private NotSupportedErrorHandler notSupportedErrorHandler;

	private ReceivedErrorHandler receivedErrorHandler;

	private ReceivedTimeoutHandler receivedTimeoutHandler;

	private InBandRegistrationModule registrationModule;

	private SessionListener sessionListener;

	private final StreamFeaturesReceivedHandler streamFeaturesEventHandler;

	private final Context context;
	
	public SocketInBandRegistrationXmppSessionLogic(SocketConnector connector, XmppModulesManager modulesManager,
			Context context) {
		this.connector = connector;
		this.modulesManager = modulesManager;
		this.context = context;

		this.streamFeaturesEventHandler = new StreamFeaturesReceivedHandler() {

			@Override
			public void onStreamFeaturesReceived(SessionObject sessionObject, Element featuresElement) throws JaxmppException {
				SocketInBandRegistrationXmppSessionLogic.this.processStreamFeatures(sessionObject, featuresElement);
			}
		};

		this.receivedErrorHandler = new ReceivedErrorHandler() {

			@Override
			public void onReceivedError(SessionObject sessionObject, IQ responseStanza, ErrorCondition errorCondition)
					throws JaxmppException {
				SocketInBandRegistrationXmppSessionLogic.this.connector.stop();
			}
		};
		this.receivedTimeoutHandler = new ReceivedTimeoutHandler() {

			@Override
			public void onReceivedTimeout(SessionObject sessionObject) throws JaxmppException {
				SocketInBandRegistrationXmppSessionLogic.this.connector.stop();
			}
		};
		this.notSupportedErrorHandler = new NotSupportedErrorHandler() {

			@Override
			public void onNotSupportedError(SessionObject sessionObject) throws JaxmppException {
				SocketInBandRegistrationXmppSessionLogic.this.connector.stop();
			}
		};

	}

	@Override
	public void beforeStart() throws JaxmppException {
		// TODO Auto-generated method stub

	}

	protected void processException(JaxmppException e) throws JaxmppException {
		if (sessionListener != null)
			sessionListener.onException(e);
	}

	protected void processStreamFeatures(SessionObject sessionObject, Element featuresElement) throws JaxmppException {
		try {
			final Boolean tlsDisabled = sessionObject.getProperty(SocketConnector.TLS_DISABLED_KEY);
			final boolean tlsAvailable = SocketConnector.isTLSAvailable(sessionObject);

			final boolean isConnectionSecure = connector.isSecure();

			if (!isConnectionSecure && tlsAvailable && (tlsDisabled == null || !tlsDisabled)) {
				connector.startTLS();
			} else {
				registrationModule.start();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setSessionListener(SessionListener sessionListener) throws JaxmppException {
		this.sessionListener = sessionListener;
		featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		registrationModule = this.modulesManager.getModule(InBandRegistrationModule.class);

		registrationModule.addNotSupportedErrorHandler(notSupportedErrorHandler);
		registrationModule.addReceivedErrorHandler(receivedErrorHandler);
		registrationModule.addReceivedTimeoutHandler(receivedTimeoutHandler);

		featuresModule.addStreamFeaturesReceivedHandler(streamFeaturesEventHandler);
	}

	@Override
	public void unbind() throws JaxmppException {
		featuresModule.removeStreamFeaturesReceivedHandler(streamFeaturesEventHandler);

		registrationModule.removeNotSupportedErrorHandler(notSupportedErrorHandler);
		registrationModule.removeReceivedErrorHandler(receivedErrorHandler);
		registrationModule.removeReceivedTimeoutHandler(receivedTimeoutHandler);
	}

}