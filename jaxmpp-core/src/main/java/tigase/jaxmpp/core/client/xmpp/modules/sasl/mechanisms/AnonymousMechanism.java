/*
 * tigase-xmpp4gwt
 * Copyright (C) 2007 "Bartosz Małkowski" <bmalkow@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev: 68 $
 * Last modified by $Author: bmalkow $
 * $Date: 2009-01-12 12:59:15 +0100 (pon, 12 sty 2009) $
 */
package tigase.jaxmpp.core.client.xmpp.modules.sasl.mechanisms;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslMechanism;

public class AnonymousMechanism implements SaslMechanism {

	public AnonymousMechanism() {
	}

	public String evaluateChallenge(String input, SessionObject sessionObjec) {
		return null;
	}

	public Status getStatus() {
		return null;
	}

	public String getStatusMessage() {
		return null;
	}

	public boolean isComplete() {
		return false;
	}

	public String name() {
		return "ANONYMOUS";
	}

}
