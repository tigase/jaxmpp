package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import java.util.HashSet;
import java.util.Set;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;

public class AdHocResponse {

	private final Set<Action> availableActions = new HashSet<Action>();

	private Action defaultAction = Action.complete;

	private JabberDataElement form;

	private State state = State.completed;

	private final PacketWriter writer;

	public AdHocResponse(PacketWriter writer) {
		super();
		this.writer = writer;
	}

	public Set<Action> getAvailableActions() {
		return availableActions;
	}

	public Action getDefaultAction() {
		return defaultAction;
	}

	public JabberDataElement getForm() {
		return form;
	}

	public State getState() {
		return state;
	}

	public PacketWriter getWriter() {
		return writer;
	}

	public void setDefaultAction(Action defaultAction) {
		this.availableActions.add(defaultAction);
		this.defaultAction = defaultAction;
	}

	public void setForm(JabberDataElement form) {
		this.form = form;
	}

	public void setState(State state) {
		this.state = state;
	}

}
