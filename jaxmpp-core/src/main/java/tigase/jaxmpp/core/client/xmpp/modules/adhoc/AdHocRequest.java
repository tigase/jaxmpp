package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import java.util.Date;
import java.util.Map;

import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

public class AdHocRequest {

	private final Action action;

	private JabberDataElement form;

	private final IQ iq;

	private final String node;

	private Session session;

	private String sessionId;

	private final Map<String, Session> sessions;

	public AdHocRequest(Action action, String node, String sessionId, IQ iq, Map<String, Session> sessions) {
		super();
		this.action = action;
		this.node = node;
		this.sessionId = sessionId;
		this.iq = iq;
		this.sessions = sessions;
	}

	public Action getAction() {
		return action;
	}

	public JabberDataElement getForm() {
		return form;
	}

	public IQ getIq() {
		return iq;
	}

	public String getNode() {
		return node;
	}

	public Session getSession() {
		return getSession(true);
	}

	public Session getSession(boolean createNew) {
		if (session == null && sessionId != null) {
			session = this.sessions.get(sessionId);
		}
		if (session == null && createNew) {
			sessionId = AdHocCommansModule.generateSessionId();
			session = new Session(sessionId);
			session.setLastRequest(new Date());
			this.sessions.put(sessionId, session);
		}
		return session;
	}

	public String getSessionId() {
		return sessionId;
	}

	void setForm(JabberDataElement form) {
		this.form = form;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
