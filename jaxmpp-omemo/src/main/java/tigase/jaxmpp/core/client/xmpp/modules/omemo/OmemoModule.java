package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementBuilder;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.InitializingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubErrorCondition;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OmemoModule
		implements XmppModule, ContextAware, InitializingModule {

	final static String CURRENT = "current";
	final static String XMLNS = "eu.siacs.conversations.axolotl";
	public final static String DEVICELIST_NODE = XMLNS + ".devicelist";
	public static final String AUTOCREATE_OMEMO_SESSION = XMLNS + "#AUTOCREATE_OMEMO_SESSION";
	final static String BUNDLES_NODE = XMLNS + ".bundles:";
	private final static CipherFactory DEFAULT_CIPHER_FACTORY = new CipherFactory() {
		@Override
		public Cipher cipherInstance(int mode, SecretKeySpec secretKey, final byte[] iv)
				throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException,
					   InvalidAlgorithmParameterException, java.security.InvalidKeyException {
			Cipher cipher = Cipher.getInstance(OmemoExtension.CIPHER_NAME, "BC");

			AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(mode, secretKey, ivSpec);
			return cipher;
		}
	};
	private final boolean addOwnKeys = true;
	private final Random rnd = new SecureRandom();
	private Context context;
	private CipherFactory customCipherFactory;
	private OmemoExtension extension = new OmemoExtension(this);
	private Logger log = Logger.getLogger(this.getClass().getName());
	private PubSubModule pubSub;

	public static JaXMPPSignalProtocolStore getSignalProtocolStore(SessionObject sessionObject) {
		return sessionObject.getProperty(XMLNS + "#SignalProtocolStore");
	}

	public static void setSignalProtocolStore(SessionObject sessionObject, JaXMPPSignalProtocolStore store) {
		sessionObject.setProperty(SessionObject.Scope.user, XMLNS + "#SignalProtocolStore", store);
	}

	public CipherFactory getCustomCipherFactory() {
		return customCipherFactory;
	}

	public void setCustomCipherFactory(CipherFactory customCipherFactory) {
		this.customCipherFactory = customCipherFactory;
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return new String[]{XMLNS, DEVICELIST_NODE + "+notify"};
	}

	@Override
	public void process(Element element) throws JaxmppException {

	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void afterRegister() {
		this.pubSub = this.context.getModuleProvider().getModule(PubSubModule.class);
		if (this.pubSub == null) {
			throw new RuntimeException("There is no PubSubModule registered!");
		}
		this.context.getEventBus()
				.addHandler(PubSubModule.NotificationReceivedHandler.NotificationReceivedEvent.class,
							(sessionObject, message, pubSubJID, nodeName, itemId, payload, delayTime, itemType) -> {
								if (pubSubJID != null &&
										pubSubJID.getBareJid().equals(context.getSessionObject().getUserBareJid()) &&
										nodeName.equals(DEVICELIST_NODE) && itemId.equals(CURRENT)) {
									try {
										processOwnDevicesList(payload);
									} catch (Exception e) {
										log.log(Level.WARNING, "Cannot handle DeviceList event", e);
									}
								} else if (pubSubJID != null &&
										pubSubJID.getBareJid().equals(context.getSessionObject().getUserBareJid()) &&
										nodeName.startsWith(BUNDLES_NODE) && itemId.equals(CURRENT)) {
									try {
										processOwnDevicesBundle(pubSubJID.getBareJid(), payload, nodeName);
									} catch (Exception e) {
										log.log(Level.WARNING, "Cannot handle DeviceList event", e);
									}
								}
							});
	}

	public void subscribeForDeviceList(BareJID jid) throws JaxmppException {
		JID myJid = ResourceBinderModule.getBindedJID(this.context.getSessionObject());
		this.pubSub.subscribe(jid, DEVICELIST_NODE, myJid, new PubSubModule.SubscriptionAsyncCallback() {
			@Override
			public void onTimeout() throws JaxmppException {
				System.out.println("DEVICE LIST SUBSCRIPTION TIMEOUT");
			}

			@Override
			protected void onSubscribe(IQ response, PubSubModule.SubscriptionElement subscriptionElement) {
				System.out.println("DEVICE LIST SUBSCRIBE" + "");
			}

			@Override
			protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
								  PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {
				System.out.println("DEVICE LIST SUBSCRIPTION ERROR: " + pubSubErrorCondition);
			}
		});
	}

	@Override
	public void beforeRegister() {

	}

	@Override
	public void beforeUnregister() {

	}

	public void publishDeviceList() throws JaxmppException {
		this.pubSub.retrieveItem(null, OmemoModule.DEVICELIST_NODE, new PubSubModule.RetrieveItemsAsyncCallback() {
			@Override
			public void onTimeout() throws JaxmppException {
				log.warning("Cannot retrieve own device lists: Timeout");
			}

			@Override
			protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
								  PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {
				log.warning("Cannot retrieve own device lists: " + errorCondition);
			}

			@Override
			protected void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items) {
				try {
					Collection<Integer> ids = KeysRetriever.getDeviceIDsFromPayload(items);
					publishOwnKeys(getSignalProtocolStore(context.getSessionObject()), ids);
				} catch (Exception e) {
					log.log(Level.WARNING, "Cannot publish own keys and device list", e);
				}
			}
		});
	}

	public void getKeys(BareJID jid, final KeysRetrieverHandler handler) throws JaxmppException {
		KeysRetriever keysRetriever = new KeysRetriever(context, jid) {

			@Override
			protected void finish(List<Bundle> bundles) {
				if (handler != null) {
					handler.onSuccess(bundles);
				}
			}

			@Override
			protected void error() {
				handler.onError();
			}

		};
		keysRetriever.retrieve();

	}

	public XmppOMEMOSession getOMEMOSession(BareJID jid) {
		return getOMEMOSession(jid, context.getSessionObject().getProperty(AUTOCREATE_OMEMO_SESSION) == Boolean.TRUE);
	}

	public XmppOMEMOSession createOMEMOSession(BareJID jid) {
		XmppOMEMOSession s = new XmppOMEMOSession(context.getEventBus(), jid);
		getSignalProtocolStore(context.getSessionObject()).storeSession(s);
		return s;
	}

	public void createOMEMOSession(final BareJID jid, final CreateOMEMOSessionHandler handler) throws JaxmppException {
		final ArrayList<Bundle> bundles = new ArrayList<>();
		getKeys(jid, new KeysRetrieverHandler() {
			@Override
			public void onError() {
				handler.onError();
			}

			@Override
			public void onSuccess(List<Bundle> b1) {
				bundles.addAll(b1);

				if (addOwnKeys) {
					runAddOwnKeys();
				} else {
					create();
				}
			}

			private void create() {
				XmppOMEMOSession session = null;
				try {
					session = createOMEMOSession(jid, bundles);
					handler.onSessionCreated(session);
				} catch (XMLException e) {
					e.printStackTrace();
					handler.onError();
				} catch (UntrustedIdentityException e) {
					e.printStackTrace();
					handler.onError();
				}
			}

			private void runAddOwnKeys() {
				try {
					getKeys(context.getSessionObject().getUserBareJid(), new KeysRetrieverHandler() {
						@Override
						public void onError() {
							handler.onError();
						}

						@Override
						public void onSuccess(List<Bundle> b2) {
							bundles.addAll(b2);
							create();
						}
					});
				} catch (Exception e) {
					handler.onError();
				}
			}
		});
	}

	public Extension getExtension() {
		return extension;
//		return new Extension() {
//			@Override
//			public Element afterReceive(Element received) throws JaxmppException {
//				return received;
//			}
//
//			@Override
//			public Element beforeSend(Element received) throws JaxmppException {
//				Element bd = received.getFirstChild("body");
//				if (bd != null) {
//
//					getSignalProtocolStore(context.getSessionObject());
//
//					try {
//						getKeys(BareJID.bareJIDInstance(received.getAttribute("to")), null);
//						Thread.sleep(30_000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					received.removeChild(bd);
//					ElementBuilder bldr = ElementBuilder.create("encrypted", XMLNS);
//					bldr.child("payload").setValue(bd.getValue());
//					received.addChild(bldr.getElement());
//
//					received.addChild(ElementBuilder.create("store", "urn:xmpp:hints").getElement());
//				}
//				return received;
//			}
//
//			@Override
//			public String[] getFeatures() {
//				return null;
//			}
//		};
	}

	public boolean isOMEMORequired(BareJID jid) {
		return getSignalProtocolStore(context.getSessionObject()).isOMEMORequired(jid);
	}

	public XmppOMEMOSession getOMEMOSession(BareJID jid, boolean createIfNotExists) {
		XmppOMEMOSession sess = getSignalProtocolStore(context.getSessionObject()).getSession(jid);

		if (sess != null) {
			return sess;
		} else if (createIfNotExists) {
			return createFromOMEMOStore(jid);
		} else {
			return null;
		}
	}

	CipherFactory getCipherFactory() {
		return customCipherFactory == null ? DEFAULT_CIPHER_FACTORY : customCipherFactory;
	}

	SessionObject getSessionObject() {
		return context.getSessionObject();
	}

	byte[] generateIV() {
		byte[] iv = new byte[12];
		rnd.nextBytes(iv);
		return iv;
	}

	void addBundesToSession(XmppOMEMOSession session, Collection<Bundle> bundles)
			throws XMLException, UntrustedIdentityException {
		final SignalProtocolStore store = OmemoModule.getSignalProtocolStore(context.getSessionObject());
		for (Bundle b : bundles) {
			try {
				final SignalProtocolAddress address = b.getAddress();
				PreKeyBundle bundle = b.getPreKeyBundle();
				List<PreKeyBundle> preKeys = b.getPreKeys();
				PreKeyBundle preKey = preKeys.get(rnd.nextInt(preKeys.size()));

				System.out.println(b.getDeviceId() + " :: " + b.getPreKeyBundle().getIdentityKey().getFingerprint());

				final PreKeyBundle preKeyBundle = new PreKeyBundle(0, address.getDeviceId(), preKey.getPreKeyId(),
																   preKey.getPreKey(), bundle.getSignedPreKeyId(),
																   bundle.getSignedPreKey(),
																   bundle.getSignedPreKeySignature(),
																   bundle.getIdentityKey());

				SessionBuilder sb = new SessionBuilder(store, store, store, store, address);
				sb.process(preKeyBundle);

				SessionCipher sessionCipher = new SessionCipher(store, store, store, store, address);

				session.addDeviceCipher(address, sessionCipher);
			} catch (InvalidKeyException e) {
				log.warning("Invalid key. Skipping.");
			}
		}
	}

	XmppOMEMOSession createOMEMOSession(BareJID jid, List<Bundle> bundles)
			throws XMLException, UntrustedIdentityException {
		XmppOMEMOSession session = createOMEMOSession(jid);
		addBundesToSession(session, bundles);
		return session;
	}

	void addOwnKeysToSession(final XmppOMEMOSession session) {
		try {
			getKeys(context.getSessionObject().getUserBareJid(), new KeysRetrieverHandler() {
				@Override
				public void onError() {
				}

				@Override
				public void onSuccess(List<Bundle> b2) {
					try {
						addBundesToSession(session, b2);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JaxmppException e) {
			e.printStackTrace();
		}
	}

	private XmppOMEMOSession createFromOMEMOStore(final BareJID jid) {
		JaXMPPSignalProtocolStore store = getSignalProtocolStore(context.getSessionObject());

		if (store == null) {
			return null;
		}

		XmppOMEMOSession result = createOMEMOSession(jid);
		for (Integer id : store.getSubDeviceSessions(jid.toString())) {
			SignalProtocolAddress address = new SignalProtocolAddress(jid.toString(), id);
			SessionCipher sessionCipher = new SessionCipher(store, store, store, store, address);
			result.addDeviceCipher(address, sessionCipher);
		}
		if (addOwnKeys) {
			final BareJID ownJid = context.getSessionObject().getUserBareJid();
			for (Integer id : store.getSubDeviceSessions(ownJid.toString())) {
				SignalProtocolAddress address = new SignalProtocolAddress(ownJid.toString(), id);
				SessionCipher sessionCipher = new SessionCipher(store, store, store, store, address);
				result.addDeviceCipher(address, sessionCipher);
			}

		}

		return result;
	}

	private void publishOwnKeys(final JaXMPPSignalProtocolStore store, final Collection<Integer> publishedDevicesId)
			throws JaxmppException, InvalidKeyIdException {
		final int id = store.getLocalRegistrationId();
		ElementBuilder bldr = ElementBuilder.create("bundle", XMLNS);

		for (SignedPreKeyRecord signedPreKey : store.loadSignedPreKeys()) {
			bldr.child("signedPreKeyPublic")
					.setAttribute("signedPreKeyId", String.valueOf(signedPreKey.getId()))
					.setValue(Base64.encode(signedPreKey.getKeyPair().getPublicKey().serialize()))
					.up();
			bldr.child("signedPreKeySignature").setValue(Base64.encode(signedPreKey.getSignature())).up();
		}

		IdentityKeyPair identityKeyPair = store.getIdentityKeyPair();
		bldr.child("identityKey").setValue(Base64.encode(identityKeyPair.getPublicKey().serialize())).up();

		log.info("Publish key id: " + id + ": fingerprint: " +
						 Hex.format(Hex.encode(identityKeyPair.getPublicKey().serialize(), 1), 8));

		bldr.child("prekeys");

		for (PreKeyRecord preKey : store.loadPreKeys()) {
			bldr.child("preKeyPublic")
					.setAttribute("preKeyId", String.valueOf(preKey.getId()))
					.setValue(Base64.encode(preKey.getKeyPair().getPublicKey().serialize()))
					.up();
		}

		pubSub.publishItem(null, BUNDLES_NODE + id, CURRENT, bldr.getElement(),
						   new PubSubModule.PublishAsyncCallback() {
							   @Override
							   public void onTimeout() throws JaxmppException {
								   log.warning(
										   "Bundle " + store.getLocalRegistrationId() + " is not published: Timeout");
							   }

							   @Override
							   public void onPublish(String itemId) {
								   updateDeviceList(store, publishedDevicesId);
								   log.info("Bundle " + store.getLocalRegistrationId() + " published.");
							   }

							   @Override
							   protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
													 PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {
								   log.warning("Bundle " + store.getLocalRegistrationId() + " is not published: " +
													   errorCondition + ", " + pubSubErrorCondition);
							   }
						   });

	}

	private void updateDeviceList(JaXMPPSignalProtocolStore store, Collection<Integer> publishedIds) {
		try {
			final HashSet<Integer> deviceIdsToPublish = new HashSet<>();
			deviceIdsToPublish.addAll(publishedIds);
			deviceIdsToPublish.add(store.getLocalRegistrationId());
			log.fine("Adding local device id" + store.getLocalRegistrationId() + " to published list.");
			ElementBuilder bldr = ElementBuilder.create("list", XMLNS);
			for (Integer id : deviceIdsToPublish) {
				bldr.child("device").setAttribute("id", String.valueOf(id)).up();
			}

			pubSub.publishItem(null, DEVICELIST_NODE, CURRENT, bldr.getElement(),
							   new PubSubModule.PublishAsyncCallback() {
								   @Override
								   public void onPublish(String itemId) {
									   log.info("Device list is published.");
								   }

								   @Override
								   public void onTimeout() throws JaxmppException {
									   log.warning("Device list is not published: Timeout");
								   }

								   @Override
								   protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
														 PubSubErrorCondition pubSubErrorCondition)
										   throws JaxmppException {
									   log.warning("Device list is not published: " + errorCondition + ", " +
														   pubSubErrorCondition);
								   }
							   });
		} catch (JaxmppException e) {
			log.log(Level.WARNING, "Cannot update DeviceList", e);
		}
	}

	private Collection<Integer> getDevicesId(Element payload) throws XMLException {
		ArrayList<Integer> result = new ArrayList<>();
		for (Element d : payload.getChildren("device")) {
			try {
				result.add(Integer.valueOf(d.getAttribute("id")));
			} catch (Exception ignored) {
			}
		}
		return result;
	}

	private void processOwnDevicesBundle(BareJID jid, Element payload, String nodeName)
			throws JaxmppException, InvalidKeyException {
		JaXMPPSignalProtocolStore store = getSignalProtocolStore(context.getSessionObject());
		if (store == null) {
			log.warning("No OMEMO Store in SessionObject!");
			return;
		}
		final int deviceId = Integer.valueOf(nodeName.substring(BUNDLES_NODE.length()));

		Bundle b = new Bundle(jid, deviceId, payload);

		SignalProtocolAddress addr = new SignalProtocolAddress(context.getSessionObject().getUserBareJid().toString(),
															   deviceId);
		if (store.getIdentity(addr) == null) {
			store.saveIdentity(addr, b.getPreKeyBundle().getIdentityKey());
		}
	}

	private void processOwnDevicesList(Element payload) throws JaxmppException, InvalidKeyIdException {
		// check if THIS device is on list
		JaXMPPSignalProtocolStore store = getSignalProtocolStore(context.getSessionObject());
		if (store == null) {
			log.warning("No OMEMO Store in SessionObject!");
			return;
		}
		final int deviceId = store.getLocalRegistrationId();
		final Collection<Integer> publishedList = getDevicesId(payload);

		if (!publishedList.contains(deviceId)) {
			log.info("This device (" + deviceId + ") is not published. Preparing to publish.");
			publishOwnKeys(store, publishedList);
		}

		ArrayList<Integer> unknownIdentities = new ArrayList<>();

		for (Integer id : publishedList) {
			if (id == deviceId) {
				continue;
			}
			IdentityKey identity = store.getIdentity(
					new SignalProtocolAddress(context.getSessionObject().getUserBareJid().toString(), id));

			if (identity == null) {
				unknownIdentities.add(id);
			}
		}

		if (!unknownIdentities.isEmpty()) {
			KeysRetriever kr = new KeysRetriever(context, context.getSessionObject().getUserBareJid()) {

				@Override
				protected void finish(List<Bundle> bundles) {
					for (Bundle bundle : bundles) {
						try {
							store.saveIdentity(bundle.getAddress(), bundle.getPreKeyBundle().getIdentityKey());
						} catch (Exception e) {
							log.log(Level.WARNING, "Cannot save identity " + bundle.getAddress(), e);
						}
					}
				}

				@Override
				protected void error() {
				}
			};
			kr.retrieve(unknownIdentities);
		}

	}

	public interface CipherFactory {

		Cipher cipherInstance(int mode, SecretKeySpec secretKey, final byte[] iv)
				throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException,
					   InvalidAlgorithmParameterException, java.security.InvalidKeyException;
	}

	public interface CreateOMEMOSessionHandler {

		void onError();

		void onSessionCreated(XmppOMEMOSession session);
	}

	public interface KeysRetrieverHandler {

		void onError();

		void onSuccess(List<Bundle> bundles);
	}
}
