package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class PubSubAsyncCallback implements AsyncCallback {

	protected abstract void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition);

	@Override
	public final void onError(Stanza responseStanza, ErrorCondition errorCondition) throws XMLException {
		List<Element> errors = responseStanza.getChildren("error");
		Element error = errors == null || errors.isEmpty() ? null : errors.get(0);
		PubSubErrorCondition pubSubErrorCondition = null;

		List<Element> perrors = error.getChildrenNS("http://jabber.org/protocol/pubsub#errors");
		Element perror = perrors == null || perrors.isEmpty() ? null : perrors.get(0);

		if (perror != null) {
			String c = perror.getName();
			String feature = perror.getAttribute("feature");

			if (feature != null)
				c = c + "_" + feature;

			try {
				pubSubErrorCondition = PubSubErrorCondition.valueOf(c.replace("-", "_"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		onEror((IQ) responseStanza, errorCondition, pubSubErrorCondition);
	}

}
