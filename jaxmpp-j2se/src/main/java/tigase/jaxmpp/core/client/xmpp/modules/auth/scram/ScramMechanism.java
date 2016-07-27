package tigase.jaxmpp.core.client.xmpp.modules.auth.scram;

import tigase.jaxmpp.core.client.SessionObject;

public class ScramMechanism extends AbstractScram {

	public ScramMechanism() {
		super("SCRAM-SHA-1", "SHA1", "Client Key".getBytes(UTF_CHARSET), "Server Key".getBytes(UTF_CHARSET));
	}

	@Override
	protected byte[] getBindData(BindType bindType, SessionObject sessionObject) {
		return null;
	}

	@Override
	protected BindType getBindType(SessionObject sessionObject) {
		return BindType.n;
	}
}
