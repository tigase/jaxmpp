/*
 * Action.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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

/**
 * Command Actions.
 *
 * @author bmalkow
 */
public enum Action {
	/**
	 * The command should be canceled.
	 */
	cancel,
	/**
	 * The command should be completed (if possible).
	 */
	complete,
	/**
	 * The command should be executed or continue to be executed. This is the
	 * default value.
	 */
	execute,
	/**
	 * The command should progress to the next stage of execution.
	 */
	next,
	/**
	 * The command should be digress to the previous stage of execution.
	 */
	prev
}