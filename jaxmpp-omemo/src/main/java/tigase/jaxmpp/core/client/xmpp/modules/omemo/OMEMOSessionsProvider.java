package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import tigase.jaxmpp.core.client.BareJID;

public interface OMEMOSessionsProvider {

	XmppOMEMOSession getSession(BareJID jid);

	void storeSession(XmppOMEMOSession session);

	boolean isOMEMORequired(BareJID jid);

	void setOMEMORequired(BareJID jid, boolean required);

}
