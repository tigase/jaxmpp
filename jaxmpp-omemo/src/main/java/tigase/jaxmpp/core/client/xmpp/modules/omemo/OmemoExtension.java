package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementBuilder;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OmemoExtension
		implements Extension {

	public static final String OMEMO_ERROR_FLAG = OmemoModule.XMLNS + "#ERROR";
	public final static String CIPHER_NAME = "AES/GCM/NoPadding";
	public final static String ALGORITHM_NAME = "AES";
	private final static int KEY_SIZE = 128;
	private final static boolean AUTHTAG = true;
	private final Logger log = Logger.getLogger(this.getClass().getName());
	private final OmemoModule module;

	private static byte[] generateKey() throws NoSuchAlgorithmException {
		KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM_NAME);
		generator.init(KEY_SIZE);
		return generator.generateKey().getEncoded();
	}

	private static byte[] getCiphertext(Element encElement) throws XMLException {
		Element e = encElement.getFirstChild("payload");
		return e == null ? null : Base64.decode(e.getValue());
	}

	private static BareJID getFromJid(Element stanza) throws XMLException {
		String t = stanza.getAttribute("from");
		if (t == null) {
			return null;
		}
		return BareJID.bareJIDInstance(t);
	}

	private static byte[] getIV(Element encElement) throws XMLException {
		Element header = encElement.getFirstChild("header");
		Element e = header.getFirstChild("iv");
		return e == null ? null : Base64.decode(e.getValue());
	}

	private static Element getKeyElement(Element encElement, int localRegistrationId) throws XMLException {
		Element header = encElement.getFirstChild("header");
		for (Element c : header.getChildren("key")) {
			if (Integer.valueOf(c.getAttribute("rid")) == localRegistrationId) {
				return c;
			}
		}
		return null;
	}

	private static Integer getSenderDeviceId(Element encElement) throws XMLException {
		Element header = encElement.getFirstChild("header");
		return header == null ? null : Integer.valueOf(header.getAttribute("sid"));
	}

	private static BareJID getToJid(Element stanza) throws XMLException {
		String t = stanza.getAttribute("to");
		if (t == null) {
			return null;
		}
		return BareJID.bareJIDInstance(t);
	}

	OmemoExtension(OmemoModule omemoModule) {
		this.module = omemoModule;
	}

	@Override
	public Element afterReceive(Element stanza) throws JaxmppException {
		final Element encElement = stanza.getChildrenNS("encrypted", OmemoModule.XMLNS);
		if (encElement != null && getFromJid(stanza) != null) {
			return decryptMessage(ElementFactory.create(stanza));
		}

		if (stanza.findChild(new String[]{"message", "sent", "forwarded", "message", "encrypted"}) != null) {
			return processMessageCarbon(stanza);
		}

		if (stanza.findChild(new String[]{"message", "result", "forwarded", "message", "encrypted"}) != null) {
			return processMAMMessage(stanza);
		}

		return stanza;
	}

	@Override
	public Element beforeSend(final Element stanza) throws JaxmppException {
		final BareJID jid = getToJid(stanza);
		final Element bd = stanza.getFirstChild("body");

		final boolean encryptableStanza = bd != null && jid != null;

		if (!encryptableStanza) {
			return stanza;
		}

		final OMEMOEncryptableMessage.Encryption encryption = calculateEncryptionStatus(jid, stanza);
		final XmppOMEMOSession session = module.getOMEMOSession(jid, encryption ==
				OMEMOEncryptableMessage.Encryption.Required);

		if (encryption == OMEMOEncryptableMessage.Encryption.Disabled) {
			return stanza;
		} else if (encryptableStanza && encryption == OMEMOEncryptableMessage.Encryption.Required && session == null) {
			throw new CannotEncryptException();
		} else if (!encryptableStanza || session == null) {
			return stanza;
		}

		stanza.removeChild(bd);
		stanza.addChild(ElementFactory.create("body", "Message is encrypted.", null));
		try {
			stanza.addChild(createEncryptedElement(bd, session));
		} catch (Exception e) {
			throw new JaxmppException(e);
		}
		stanza.addChild(ElementBuilder.create("store", "urn:xmpp:hints").getElement());

		return new OMEMOMessage(true, stanza);
	}

	public Message decryptMessage(Element stanza) throws JaxmppException {
		final BareJID jid = getFromJid(stanza);
		final Element encElement = stanza.getChildrenNS("encrypted", OmemoModule.XMLNS);

		final SignalProtocolStore store = OmemoModule.getSignalProtocolStore(module.getSessionObject());
		final XmppOMEMOSession session = getOrCreateSession(jid);

		final int senderDeviceId = getSenderDeviceId(encElement);
		final byte[] iv = getIV(encElement);
		final Element encKeyElement = getKeyElement(encElement, store.getLocalRegistrationId());
		final byte[] encryptedKey = extractKey(encKeyElement);
		if (encryptedKey == null) {
			Message m = (Message) Message.create(stanza);
			m.addFlag(OMEMO_ERROR_FLAG);
			m.setBody("Message is not encrypted for this device.");
			return m;
		}
		final boolean prekey = isPreKey(encKeyElement);
		byte[] ciphertext = getCiphertext(encElement);

		final OMEMOMessage result = new OMEMOMessage(false, stanza);
		result.removeChild(result.getChildrenNS("encrypted", OmemoModule.XMLNS));

		try {

			byte[] key = session.processEncryptedKey(store, senderDeviceId, encryptedKey, prekey);

			if (key.length >= 32) {
				int authtaglength = key.length - 16;
				byte[] newCipherText = new byte[key.length - 16 + ciphertext.length];
				byte[] newKey = new byte[16];

				System.arraycopy(ciphertext, 0, newCipherText, 0, ciphertext.length);
				System.arraycopy(key, 16, newCipherText, ciphertext.length, authtaglength);
				System.arraycopy(key, 0, newKey, 0, newKey.length);

				ciphertext = newCipherText;
				key = newKey;
			}

			SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);

			final Cipher cipher = module.getCipherFactory().cipherInstance(Cipher.DECRYPT_MODE, keySpec, iv);

//			Cipher cipher = Cipher.getInstance(CIPHER_NAME);
//			GCMParameterSpec ivSpec = new GCMParameterSpec(KEY_SIZE, iv);
//			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
//
			byte[] plain = ciphertext == null ? null : cipher.doFinal(ciphertext);

			if (plain == null) {
				result.setBody(null);
			} else {
				result.setBody(new String(plain));
			}
			result.setSecured(true);
		} catch (Exception e) {
			log.log(Level.WARNING, "Problem on processing OMEMO data", e);
			result.addFlag(OMEMO_ERROR_FLAG);
			result.setBody(e.getMessage());
			result.setType(StanzaType.error);
		}

		if (prekey) {
			module.publishDeviceList();
		}

		return result;
	}

	@Override
	public String[] getFeatures() {
		return new String[]{OmemoModule.XMLNS};
	}

	private OMEMOEncryptableMessage.Encryption calculateEncryptionStatus(final BareJID jid, final Element stanza) {
		if (module.isOMEMORequired(jid)) {
			return OMEMOEncryptableMessage.Encryption.Required;
		} else if (stanza instanceof OMEMOEncryptableMessage) {
			return ((OMEMOEncryptableMessage) stanza).getEncryption();
		} else {
			return OMEMOEncryptableMessage.Encryption.Default;
		}
	}

	private Element createEncryptedElement(final Element body, final XmppOMEMOSession session)
			throws JaxmppException, NoSuchAlgorithmException, NoSuchPaddingException,
				   InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
				   IllegalBlockSizeException, UntrustedIdentityException, NoSuchProviderException {
		final String plaintext = body.getValue();

		final SignalProtocolStore store = OmemoModule.getSignalProtocolStore(module.getSessionObject());
		final ElementBuilder encryptedElement = ElementBuilder.create("encrypted", OmemoModule.XMLNS);
		final byte[] keyData = generateKey();
		final byte[] iv = module.generateIV();

		final SecretKeySpec secretKey = new SecretKeySpec(keyData, ALGORITHM_NAME);

		final Cipher cipher = module.getCipherFactory().cipherInstance(Cipher.ENCRYPT_MODE, secretKey, iv);

//		Cipher cipher = Cipher.getInstance(CIPHER_NAME);
//		AlgorithmParameterSpec ivSpec = new GCMParameterSpec(KEY_SIZE, module.generateIV());
//		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

		byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

		byte[] authtagPlusInnerKey;
		if (AUTHTAG) {
			authtagPlusInnerKey = new byte[16 + 16];
			byte[] encData = new byte[ciphertext.length - 16];
			System.arraycopy(ciphertext, 0, encData, 0, encData.length);
			System.arraycopy(ciphertext, encData.length, authtagPlusInnerKey, 16, 16);
			System.arraycopy(keyData, 0, authtagPlusInnerKey, 0, keyData.length);
			ciphertext = encData;
		} else {
			authtagPlusInnerKey = null;
		}

		encryptedElement.child("header").setAttribute("sid", String.valueOf(store.getLocalRegistrationId()));

		for (Map.Entry<SignalProtocolAddress, SessionCipher> s : session.getDeviceCiphers().entrySet()) {
			SignalProtocolAddress addr = s.getKey();
			encryptedElement.child("key").setAttribute("rid", String.valueOf(addr.getDeviceId()));

			CiphertextMessage m;
			try {
				if (authtagPlusInnerKey != null) {
					m = s.getValue().encrypt(authtagPlusInnerKey);
				} else {
					m = s.getValue().encrypt(secretKey.getEncoded());
				}
				if (m.getType() == CiphertextMessage.PREKEY_TYPE) {
					encryptedElement.setAttribute("prekey", "true");
				}
				encryptedElement.setValue(Base64.encode(m.serialize()));

			} catch (java.lang.IllegalArgumentException e) {
				log.warning("Cannot encrypt to " + s.getKey());
			} catch (Exception e) {
				throw new JaxmppException("Cannot encrypt to " + s.getKey() + ", remoteRegistrationId=" +
												  s.getValue().getRemoteRegistrationId(), e);
			} finally {
				encryptedElement.up();
			}
		}

		encryptedElement.child("iv").setValue(Base64.encode(iv)).up();
		encryptedElement.up().child("payload").setValue(Base64.encode(ciphertext));
		return encryptedElement.getElement();
	}

	private byte[] extractKey(Element keyElement) throws XMLException {
		if (keyElement == null || keyElement.getValue() == null) {
			return null;
		}
		return Base64.decode(keyElement.getValue());
	}

	private XmppOMEMOSession getOrCreateSession(BareJID jid) {
		XmppOMEMOSession s = module.getOMEMOSession(jid);
		if (s == null) {
			s = module.createOMEMOSession(jid);
			module.addOwnKeysToSession(s);
		}
		return s;
	}

	private boolean isPreKey(Element keyElement) throws XMLException {
		if (keyElement == null) {
			return false;
		}
		String a = keyElement.getAttribute("prekey");
		return a != null && (a.equals("1") || a.equals("true"));
	}

	private Element processMAMMessage(Element stanza) throws JaxmppException {
		Element inStanza = ElementFactory.create(stanza);
		Element forwardedMessage = inStanza.findChild(new String[]{"message", "result", "forwarded", "message"});

		String body = forwardedMessage.getFirstChild("body") != null
					  ? forwardedMessage.getFirstChild("body").getValue()
					  : null;
		System.out.println(body);

		Message dcr = decryptMessage(forwardedMessage);
		Element forwarded = forwardedMessage.getParent();
		forwarded.removeChild(forwardedMessage);
		forwarded.addChild(dcr);

		return inStanza;
	}

	private Element processMessageCarbon(Element stanza) throws JaxmppException {
		Element inStanza = ElementFactory.create(stanza);
		Element forwardedMessage = inStanza.findChild(new String[]{"message", "sent", "forwarded", "message"});

		String body = forwardedMessage.getFirstChild("body") != null
					  ? forwardedMessage.getFirstChild("body").getValue()
					  : null;
		System.out.println(body);

		Message dcr = decryptMessage(forwardedMessage);
		Element forwarded = forwardedMessage.getParent();
		forwarded.removeChild(forwardedMessage);
		forwarded.addChild(dcr);

		return inStanza;
	}
}
