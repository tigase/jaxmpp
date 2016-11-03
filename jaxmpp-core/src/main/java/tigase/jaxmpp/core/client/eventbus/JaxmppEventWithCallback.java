/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2016 Tigase, Inc.
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
package tigase.jaxmpp.core.client.eventbus;

import tigase.jaxmpp.core.client.SessionObject;

/**
 * Created by andrzej on 02.11.2016.
 */
public abstract class JaxmppEventWithCallback<H extends EventHandler> extends JaxmppEvent<H> {

	private final RunAfter<JaxmppEventWithCallback> runAfter;

	protected JaxmppEventWithCallback(SessionObject sessionObject, RunAfter<? extends JaxmppEventWithCallback> runAfter) {
		super(sessionObject);
		this.runAfter = (RunAfter<JaxmppEventWithCallback>) runAfter;
	}

	public RunAfter<? extends JaxmppEventWithCallback> getRunAfter() {
		return runAfter;
	}

	public interface RunAfter<E> {

		void after(E event);

	}

}
