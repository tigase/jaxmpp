package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;

public class XmppSessionLogic {

	private final BoshConnector connector;

	private final XmppModulesManager modulesManager;

	private ResourceBinderModule resourceBinder;

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
				processStreamFeatures(be);
			}
		};
		this.saslEventListener = new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				processSaslEvent(be);
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
	}

	protected void processSaslEvent(BaseEvent be) {
		try {
			if (be.getType() == SaslModule.SASL_SUCCESS) {
				sessionObject.setProperty("jaxmpp#authorized", Boolean.TRUE);
				connector.restartStream();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void processStreamFeatures(StreamFeaturesReceivedEvent be) {
		try {
			if (sessionObject.getProperty("jaxmpp#authorized") != Boolean.TRUE) {
				saslModule.login();
			} else if (sessionObject.getProperty("jaxmpp#authorized") == Boolean.TRUE) {
				resourceBinder.bind();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}
}
