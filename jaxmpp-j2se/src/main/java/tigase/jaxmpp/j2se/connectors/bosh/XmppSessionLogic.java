package tigase.jaxmpp.j2se.connectors.bosh;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;

public class XmppSessionLogic {

	public static final String AUTHORIZED = "jaxmpp#authorized";

	private final BoshConnector connector;

	private final XmppModulesManager modulesManager;

	private ResourceBinderModule resourceBinder;

	private Listener<ResourceBindEvent> resourceBindListener;

	private final Listener<BaseEvent> saslEventListener;

	private SaslModule saslModule;

	private final SessionObject sessionObject;

	private final Listener<StreamFeaturesReceivedEvent> streamFeaturesEventListener;

	private final PacketWriter writer;

	public XmppSessionLogic(BoshConnector connector, XmppModulesManager modulesManager, SessionObject sessionObject,
			PacketWriter writer) {
		this.connector = connector;
		this.modulesManager = modulesManager;
		this.sessionObject = sessionObject;
		this.writer = writer;

		this.streamFeaturesEventListener = new Listener<StreamFeaturesModule.StreamFeaturesReceivedEvent>() {

			@Override
			public void handleEvent(StreamFeaturesReceivedEvent be) {
				try {
					processStreamFeatures(be);
				} catch (JaxmppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		this.saslEventListener = new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				try {
					processSaslEvent(be);
				} catch (JaxmppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};
		this.resourceBindListener = new Listener<ResourceBindEvent>() {

			@Override
			public void handleEvent(ResourceBindEvent be) {
				try {
					processResourceBindEvent(be);
				} catch (JaxmppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
	}

	public void init() {
		StreamFeaturesModule featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		saslModule = this.modulesManager.getModule(SaslModule.class);
		resourceBinder = this.modulesManager.getModule(ResourceBinderModule.class);

		featuresModule.addListener(StreamFeaturesModule.STREAM_FEATURES_RECEIVED, streamFeaturesEventListener);
		saslModule.addListener(SaslModule.SASL_SUCCESS, this.saslEventListener);
		saslModule.addListener(SaslModule.SASL_FAILED, this.saslEventListener);

		resourceBinder.addListener(ResourceBinderModule.BIND_SUCCESSFULL, resourceBindListener);
	}

	protected void processResourceBindEvent(ResourceBindEvent be) throws JaxmppException {
		try {
			RosterModule roster = this.modulesManager.getModule(RosterModule.class);
			roster.rosterRequest();

			PresenceModule presence = this.modulesManager.getModule(PresenceModule.class);
			presence.sendInitialPresence();
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void processSaslEvent(BaseEvent be) throws JaxmppException {
		try {
			if (be.getType() == SaslModule.SASL_SUCCESS) {
				sessionObject.setProperty(AUTHORIZED, Boolean.TRUE);
				connector.restartStream();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void processStreamFeatures(StreamFeaturesReceivedEvent be) throws JaxmppException {
		try {
			if (sessionObject.getProperty(AUTHORIZED) != Boolean.TRUE) {
				saslModule.login();
			} else if (sessionObject.getProperty(AUTHORIZED) == Boolean.TRUE) {
				resourceBinder.bind();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}
}
