package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyBundle;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.ArrayList;
import java.util.List;

public class Bundle
		extends ElementWrapper {

	private final Integer deviceId;

	public Bundle(Integer deviceId, Element element) throws JaxmppException {
		super(element);
		this.deviceId = deviceId;
		if (!element.getName().equals("bundle") || !element.getXMLNS().equals(OmemoModule.XMLNS)) {
			throw new JaxmppException("Invalid bundle!");
		}
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Bundle{");
		sb.append("deviceId='").append(deviceId).append('\'');
		sb.append('}');
		return sb.toString();
	}

	public PreKeyBundle getPreKeyBundle() throws XMLException, InvalidKeyException {
		ECPublicKey signedPreKeyPublic = getSignedPreKeyPublic();
		Integer signedPreKeyId = getSignedPreKeyId();
		byte[] signedPreKeySignature = getSignedPreKeySignature();
		IdentityKey identityKey = getIdentityKey();
		return new PreKeyBundle(0, deviceId, 0, null, signedPreKeyId, signedPreKeyPublic, signedPreKeySignature,
								identityKey);
	}

	public List<PreKeyBundle> getPreKeys() throws XMLException, InvalidKeyException {
		final ArrayList<PreKeyBundle> result = new ArrayList<>();

		Element prekeys = getFirstChild("prekeys");
		for (Element preKeyElement : prekeys.getChildren("preKeyPublic")) {
			Integer preKeyId = new Integer(preKeyElement.getAttribute("preKeyId"));
			final ECPublicKey preKeyPublic = Curve.decodePoint(Base64.decode(preKeyElement.getValue()), 0);

			result.add(new PreKeyBundle(0, deviceId, preKeyId, preKeyPublic, 0, null, null, null));
		}

		return result;
	}

	private byte[] getSignedPreKeySignature() throws XMLException {
		Element tmp = getFirstChild("signedPreKeySignature");
		if (tmp != null) {
			return Base64.decode(tmp.getValue());
		} else {
			return null;
		}
	}

	private IdentityKey getIdentityKey() throws XMLException, InvalidKeyException {
		Element identityKey = getFirstChild("identityKey");

		return new IdentityKey(Base64.decode(identityKey.getValue()), 0);
	}

	private Integer getSignedPreKeyId() throws XMLException {
		Element signedPreKeyPublic = getFirstChild("signedPreKeyPublic");
		return new Integer(signedPreKeyPublic.getAttribute("signedPreKeyId"));
	}

	private ECPublicKey getSignedPreKeyPublic() throws XMLException, InvalidKeyException {
		final Element signedPreKeyPublic = getFirstChild("signedPreKeyPublic");
		return Curve.decodePoint(Base64.decode(signedPreKeyPublic.getValue()), 0);
	}
}
