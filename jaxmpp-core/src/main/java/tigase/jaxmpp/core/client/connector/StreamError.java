package tigase.jaxmpp.core.client.connector;

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

	public static StreamError getByElementName(String name) {
		for (StreamError e : StreamError.values()) {
			if (e.elementName.equals(name))
				return e;
		}
		return null;
	}

	private final String elementName;

	private StreamError(String elementName) {
		this.elementName = elementName;
	}

	public String getElementName() {
		return elementName;
	}
}
