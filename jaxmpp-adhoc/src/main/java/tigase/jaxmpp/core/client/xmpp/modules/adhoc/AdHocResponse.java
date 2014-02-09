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

/**
 * Class for helping sending response to the client.
 * 
 * @author bmalkow
 * 
 */
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

	/**
	 * Returns collection of available action. All actions should be added to
	 * this collection.
	 * 
	 * @return collection of available actions.
	 */
	public Set<Action> getAvailableActions() {
		return availableActions;
	}

	/**
	 * Returns default action
	 * 
	 * @return {@linkplain Action} <code>null</code> is no action has no been
	 *         set.
	 */
	public Action getDefaultAction() {
		return defaultAction;
	}

	/**
	 * Return response Data Form.
	 * 
	 * @return {@linkplain JabberDataElement Data Form}. <code>null</code> is no
	 *         data has no been set.
	 */
	public JabberDataElement getForm() {
		return form;
	}

	/**
	 * Return state of execution.
	 * 
	 * @return {@linkplain State state}. <code>null</code> is no state has no
	 *         been set.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Return writer.
	 * 
	 * @return {@linkplain PacketWriter writer}
	 */
	public PacketWriter getWriter() {
		return writer;
	}

	/**
	 * Set default action. It also adds default action to available actions.
	 * 
	 * @param defaultAction
	 *            {@linkplain Action}
	 */
	public void setDefaultAction(Action defaultAction) {
		this.availableActions.add(defaultAction);
		this.defaultAction = defaultAction;
	}

	/**
	 * Set reponse data form.
	 * 
	 * @param form
	 *            {@linkplain JabberDataElement Data Form}
	 */
	public void setForm(JabberDataElement form) {
		this.form = form;
	}

	/**
	 * Set execution state.
	 * 
	 * @param state
	 *            {@linkplain State}
	 */
	public void setState(State state) {
		this.state = state;
	}

}