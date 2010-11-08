package tigase.jaxmpp.core.client;

public class JID implements Comparable<JID> {

	public static JID jidInstance(BareJID bareJid) {
		return new JID(bareJid, null);
	}

	public static JID jidInstance(BareJID bareJid, String p_resource) {
		return new JID(bareJid, p_resource);
	}

	public static JID jidInstance(String jid) {
		String[] parsedJid = BareJID.parseJID(jid);

		return jidInstance(parsedJid[0], parsedJid[1], parsedJid[2]);
	}

	public static JID jidInstance(String localpart, String domain) {
		return jidInstance(localpart, domain, null);
	}

	public static JID jidInstance(String localpart, String domain, String resource) {
		return jidInstance(BareJID.bareJIDInstance(localpart, domain), resource);
	}

	private final String $toString;

	private final BareJID bareJid;

	private final String resource;

	private JID(BareJID bareJid, String resource) {
		this.bareJid = bareJid;
		this.resource = resource == null ? null : resource.intern();
		this.$toString = BareJID.toString(bareJid, resource);
	}

	@Override
	public int compareTo(JID o) {
		return $toString.compareTo(o.$toString);
	}

	@Override
	public boolean equals(Object b) {
		boolean result = false;
		if (b instanceof JID) {
			JID jid = (JID) b;
			result = bareJid.equals(jid.bareJid)
					&& ((resource == jid.resource) || ((resource != null) && resource.equals(jid.resource)));
		}
		return result;
	}

	public BareJID getBareJid() {
		return bareJid;
	}

	public String getDomain() {
		return bareJid.getDomain();
	}

	public String getLocalpart() {
		return bareJid.getLocalpart();
	}

	public String getResource() {
		return resource;
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
