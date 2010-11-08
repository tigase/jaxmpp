package tigase.jaxmpp.core.client;

public class BareJID implements Comparable<BareJID> {

	public static BareJID bareJIDInstance(String jid) {
		String[] parsedJid = parseJID(jid);

		return bareJIDInstance(parsedJid[0], parsedJid[1]);
	}

	public static BareJID bareJIDInstance(String p_localpart, String p_domain) {
		return new BareJID(p_localpart, p_domain);
	}

	public static String[] parseJID(String jid) {
		String[] result = new String[3];

		// Cut off the resource part first
		int idx = jid.indexOf('/');

		// Resource part:
		result[2] = ((idx == -1) ? null : jid.substring(idx + 1));

		String id = ((idx == -1) ? jid : jid.substring(0, idx));

		// Parse the localpart and the domain name
		idx = id.indexOf('@');
		result[0] = ((idx == -1) ? null : id.substring(0, idx));
		result[1] = ((idx == -1) ? id : id.substring(idx + 1));

		return result;
	}

	static String toString(BareJID bareJid, String p_resource) {
		return bareJid.toString() + (((p_resource != null) && (p_resource.length() > 0)) ? "/" + p_resource : "");
	}

	static String toString(String p_localpart, String p_domain) {
		return (((p_localpart != null) && (p_localpart.length() > 0)) ? (p_localpart + "@" + p_domain) : p_domain);
	}

	private final String $toString;

	protected final String domain;

	protected final String localpart;

	protected BareJID(String localpart, String domain) {
		this.localpart = localpart.intern();
		this.domain = domain.toLowerCase().intern();
		this.$toString = toString(this.localpart, this.domain);
	}

	@Override
	public int compareTo(BareJID o) {
		return $toString.compareTo(o.$toString);
	}

	@Override
	public boolean equals(Object b) {
		boolean result = false;

		if (b instanceof BareJID) {
			result = (this.domain == ((BareJID) b).domain)
					&& ((this.localpart == null) ? this.localpart == ((BareJID) b).localpart
							: this.localpart.equalsIgnoreCase(((BareJID) b).localpart));
		}

		return result;
	}

	public String getDomain() {
		return domain;
	}

	public String getLocalpart() {
		return localpart;
	}

	@Override
	public int hashCode() {
		return $toString.hashCode();
	}

	@Override
	public String toString() {
		return $toString;
	}

}
