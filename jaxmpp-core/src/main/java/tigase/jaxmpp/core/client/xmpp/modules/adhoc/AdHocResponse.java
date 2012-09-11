/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
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