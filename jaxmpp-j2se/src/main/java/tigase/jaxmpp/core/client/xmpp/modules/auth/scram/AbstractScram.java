package tigase.jaxmpp.core.client.xmpp.modules.auth.scram;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.ClientSaslException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.CredentialsCallback;
import tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms.AbstractSaslMechanism;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractScram extends AbstractSaslMechanism {


	protected final static Charset UTF_CHARSET = Charset.forName("UTF-8");
	private final static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private final static String SCRAM_SASL_DATA_KEY = "SCRAM_SASL_DATA_KEY";
	private final static Pattern SERVER_FIRST_MESSAGE = Pattern.compile(
			"^(m=[^\\000=]+,)?r=([\\x21-\\x2B\\x2D-\\x7E]+),s=([a-zA-Z0-9/+=]+),i=(\\d+)(?:,.*)?$");
	private final static Pattern SERVER_LAST_MESSAGE = Pattern.compile("^(?:e=([^,]+)|v=([a-zA-Z0-9/+=]+)(?:,.*)?)$");
	private final String algorithm;
	private final byte[] clientKeyData;
	private final String mechanismName;
	private final Random random = new SecureRandom();
	private final byte[] serverKeyData;


	protected AbstractScram(String mechanismName, String algorithm, byte[] clientKey, byte[] serverKey) {
		this.clientKeyData = clientKey;
		this.serverKeyData = serverKey;
		this.algorithm = algorithm;
		this.mechanismName = mechanismName;
	}

	public static byte[] hi(String algorithm, byte[] password, final byte[] salt, final int iterations)
			throws InvalidKeyException, NoSuchAlgorithmException {
		final SecretKeySpec k = new SecretKeySpec(password, "Hmac" + algorithm);

		byte[] z = new byte[salt.length + 4];
		System.arraycopy(salt, 0, z, 0, salt.length);
		System.arraycopy(new byte[]{0, 0, 0, 1}, 0, z, salt.length, 4);

		byte[] u = hmac(k, z);
		byte[] result = new byte[u.length];
		System.arraycopy(u, 0, result, 0, result.length);

		int i = 1;
		while (i < iterations) {
			u = hmac(k, u);
			for (int j = 0; j < u.length; j++) {
				result[j] ^= u[j];
			}
			++i;
		}

		return result;
	}

	protected static byte[] hmac(final SecretKey key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance(key.getAlgorithm());
		mac.init(key);
		return mac.doFinal(data);
	}

	public static byte[] normalize(String str) {
		return str.getBytes(UTF_CHARSET);
	}

	@Override
	public String evaluateChallenge(String input, SessionObject sessionObject) throws ClientSaslException {
		final Data data = getData(sessionObject);
		try {
			if (data.stage == 0) {
				final BareJID userJID = sessionObject.getProperty(SessionObject.USER_BARE_JID);
				data.conce = randomString();

				data.bindType = getBindType(sessionObject);
				data.bindData = getBindData(data.bindType, sessionObject);

				StringBuilder sb = new StringBuilder();
				switch (data.bindType) {
					case n:
						sb.append("n");
						break;
					case y:
						sb.append("y");
						break;
					case tls_server_end_point:
						sb.append("p=tls-server-end-point");
						break;
					case tls_unique:
						sb.append("p=tls-unique");
						break;
				}
				sb.append(",");
				// sb.append("a=").append(userJID.toString());
				sb.append(',');
				data.cb = sb.toString();

				sb = new StringBuilder();
				sb.append("n=").append(userJID.getLocalpart()).append(',');
				sb.append("r=").append(data.conce);
				data.clientFirstMessageBare = sb.toString();

				++data.stage;
				return Base64.encode((data.cb + data.clientFirstMessageBare).getBytes(UTF_CHARSET));
			} else if (data.stage == 1) {
				final String serverFirstMessage = new String(Base64.decode(input));
				Matcher r = SERVER_FIRST_MESSAGE.matcher(serverFirstMessage);
				if (!r.matches())
					throw new ClientSaslException("Bad challenge syntax");

				final String mext = r.group(1);
				final String nonce = r.group(2);
				final byte[] salt = Base64.decode(r.group(3));
				final int iterations = Integer.parseInt(r.group(4));

				if (!nonce.startsWith(data.conce))
					throw new ClientSaslException("Wrong nonce");

				CredentialsCallback callback = sessionObject.getProperty(AuthModule.CREDENTIALS_CALLBACK);
				if (callback == null)
					callback = new AuthModule.DefaultCredentialsCallback(sessionObject);

				StringBuilder clientFinalMessage = new StringBuilder();


				final ByteArrayOutputStream cData = new ByteArrayOutputStream();
				cData.write(data.cb.getBytes());
				if (data.bindData != null) {
					cData.write(data.bindData);
				}
				clientFinalMessage.append("c=").append(Base64.encode(cData.toByteArray())).append(',');
				clientFinalMessage.append("r=").append(nonce);

				data.authMessage = data.clientFirstMessageBare + "," + serverFirstMessage + "," + clientFinalMessage.toString();

				data.saltedPassword = hi(algorithm, normalize(callback.getCredential()), salt, iterations);
				byte[] clientKey = hmac(key(data.saltedPassword), clientKeyData);
				byte[] storedKey = h(clientKey);

				byte[] clientSignature = hmac(key(storedKey), data.authMessage.getBytes(UTF_CHARSET));
				byte[] clientProof = xor(clientKey, clientSignature);

				clientFinalMessage.append(',');
				clientFinalMessage.append("p=").append(Base64.encode(clientProof));

				++data.stage;
				return Base64.encode(clientFinalMessage.toString().getBytes(UTF_CHARSET));
			} else if (data.stage == 2) {
				final String serverLastMessage = new String(Base64.decode(input));
				Matcher r = SERVER_LAST_MESSAGE.matcher(serverLastMessage);
				if (!r.matches())
					throw new ClientSaslException("Bad challenge syntax");

				final String e = r.group(1);
				final String v = r.group(2);

				if (e != null)
					throw new ClientSaslException("Error: " + e);

				byte[] serverKey = hmac(key(data.saltedPassword), serverKeyData);
				byte[] serverSignature = hmac(key(serverKey), data.authMessage.getBytes(UTF_CHARSET));

				if (!Arrays.equals(serverSignature, Base64.decode(v)))
					throw new ClientSaslException("Invalid Server Signature");

				++data.stage;
				setComplete(sessionObject, true);
				return null;
			} else if (isComplete(sessionObject) && input == null) {
				// server last message was sent in challange. Here should be
				// SUCCESS
				return null;
			} else
				throw new ClientSaslException(name() + ": Client at illegal state");
		} catch (ClientSaslException e) {
			throw e;
		} catch (Exception e1) {
			throw new ClientSaslException("Error in SASL", e1);
		}
	}

	protected abstract byte[] getBindData(BindType bindType, SessionObject sessionObject);

	protected abstract BindType getBindType(SessionObject sessionObject);

	protected Data getData(SessionObject sessionObject) {
		Data data = sessionObject.getProperty(SCRAM_SASL_DATA_KEY);
		if (data == null) {
			data = new Data();
			sessionObject.setProperty(SessionObject.Scope.stream, SCRAM_SASL_DATA_KEY, data);
		}
		return data;
	}

	protected byte[] h(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		return digest.digest(data);
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		return (sessionObject.getProperty(SessionObject.PASSWORD) != null
				|| sessionObject.getProperty(AuthModule.CREDENTIALS_CALLBACK) != null)
				&& sessionObject.getProperty(SessionObject.USER_BARE_JID) != null;
	}

	protected SecretKey key(final byte[] key) {
		return new SecretKeySpec(key, "Hmac" + algorithm);
	}

	@Override
	public String name() {
		return mechanismName;
	}

	protected String randomString() {
		final int length = 20;
		final int x = ALPHABET.length();
		char[] buffer = new char[length];
		for (int i = 0; i < length; i++) {
			int r = random.nextInt(x);
			buffer[i] = ALPHABET.charAt(r);
		}
		return new String(buffer);
	}

	protected byte[] xor(final byte[] a, final byte[] b) {
		final int l = a.length;
		byte[] r = new byte[l];
		for (int i = 0; i < l; i++) {
			r[i] = (byte) (a[i] ^ b[i]);
		}
		return r;
	}

	public enum BindType {
		/**
		 * Client doesn't support channel binding.
		 */
		n,
		/**
		 * Client does support channel binding but thinks the server does not.
		 */
		y,
		/**
		 * Client requires channel binding: <code>tls-unique</code>.
		 */
		tls_unique,
		/**
		 * Client requires channel binding: <code>tls-server-end-point</code>.
		 */
		tls_server_end_point
	}

	private class Data {

		private BindType bindType;

		private byte[] bindData;

		private String authMessage;

		private String cb;

		private String clientFirstMessageBare;

		private String conce;

		private byte[] saltedPassword;

		private int stage = 0;

	}

}
