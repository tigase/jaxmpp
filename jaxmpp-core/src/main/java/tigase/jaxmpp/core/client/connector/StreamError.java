/*
 * StreamError.java
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
package tigase.jaxmpp.core.client.connector;

/**
 * XML Stream errors.
 */
public enum StreamError {
	bad_format("bad-format"),
	bad_namespace_prefix("bad-namespace-prefix"),
	conflict("conflict"),
	connection_timeout("connection-timeout"),
	host_gone("host-gone"),
	host_unknown("host-unknown"),
	improper_addressing("improper-addressing"),
	internal_server_error("internal-server-error"),
	invalid_from("invalid-from"),
	invalid_id("invalid-id"),
	invalid_namespace("invalid-namespace"),
	invalid_xml("invalid-xml"),
	not_authorized("not-authorized"),
	not_well_formed("not-well-formed"),
	policy_violation("policy-violation"),
	remote_connection_failed("remote-connection-failed"),
	reset("reset"),
	resource_constraint("resource-constraint"),
	restricted_xml("restricted-xml"),
	see_other_host("see-other-host"),
	system_shutdown("system-shutdown"),
	undefined_condition("undefined-condition"),
	unsupported_encoding("unsupported-encoding"),
	unsupported_stanza_type("unsupported-stanza-type"),
	unsupported_version("unsupported-version");

	private final String elementName;

	public static StreamError getByElementName(String name) {
		for (StreamError e : StreamError.values()) {
			if (e.elementName.equals(name)) {
				return e;
			}
		}
		return null;
	}

	StreamError(String elementName) {
		this.elementName = elementName;
	}

	public String getElementName() {
		return elementName;
	}
}