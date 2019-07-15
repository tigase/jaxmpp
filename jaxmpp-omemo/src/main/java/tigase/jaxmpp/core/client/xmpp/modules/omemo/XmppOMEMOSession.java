package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class XmppOMEMOSession {

	private final Map<SignalProtocolAddress, SessionCipher> deviceCiphers = new HashMap<>();
	private final transient EventBus eventBus;
	private final BareJID jid;

	XmppOMEMOSession(EventBus eventBus, BareJID jid) {
		this.jid = jid;
		this.eventBus = eventBus;
	}

	public Map<SignalProtocolAddress, SessionCipher> getDeviceCiphers() {
		return deviceCiphers;
	}

	public BareJID getJid() {
		return jid;
	}

	public void addDeviceCipher(SignalProtocolAddress addr, SessionCipher sessionCipher) {
		this.deviceCiphers.put(addr, sessionCipher);
	}

	public void addDeviceCipher(SignalProtocolStore store, final SignalProtocolAddress address) {
		SessionCipher sessionCipher = new SessionCipher(store, store, store, store, address);
		addDeviceCipher(address, sessionCipher);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof XmppOMEMOSession)) {
			return false;
		}

		XmppOMEMOSession session = (XmppOMEMOSession) o;

		return jid.equals(session.jid);
	}

	@Override
	public int hashCode() {
		return jid.hashCode();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("XmppOMEMOSession{");
		sb.append("jid=").append(jid);
		sb.append(", devices=").append(deviceCiphers.size());
		sb.append('}');
		return sb.toString();
	}

	byte[] processEncryptedKey(final SignalProtocolStore store, final int senderDeviceId, final byte[] encryptedKey,
							   final boolean prekey)
			throws InvalidVersionException, InvalidMessageException, LegacyMessageException, DuplicateMessageException,
				   InvalidKeyIdException, UntrustedIdentityException, InvalidKeyException, NoSessionException {
		SessionCipher sc = this.deviceCiphers.get(senderDeviceId);

		if (sc == null) {
			SignalProtocolAddress addr = new SignalProtocolAddress(jid.toString(), senderDeviceId);
			sc = new SessionCipher(store, addr);
			this.deviceCiphers.put(addr, sc);
		}

		if (prekey) {
			PreKeySignalMessage m = new PreKeySignalMessage(encryptedKey);
			return sc.decrypt(m);
		} else {
			SignalMessage m = new SignalMessage(encryptedKey);
			return sc.decrypt(m);
		}
	}
}
