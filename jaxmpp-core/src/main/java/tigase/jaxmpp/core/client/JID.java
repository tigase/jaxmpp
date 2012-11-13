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
package tigase.jaxmpp.core.client;

/**
 * XMPP entity address form
 * <code>&lt;localpart@domainpart/resourcepart&gt;</code>
 */
public class JID implements Comparable<JID> {

	/**
	 * Creates intance of {@link JID JID} from {@link BareJID}.
	 * 
	 * @param bareJid
	 *            bare JID
	 * @return full JID. Resource is <code>null</code>.
	 */
	public static JID jidInstance(BareJID bareJid) {
		return new JID(bareJid, null);
	}

	/**
	 * Creates intance of {@link JID JID}.
	 * 
	 * @param bareJid
	 *            bare JID
	 * @param p_resource
	 *            resource
	 * @return full JID
	 */
	public static JID jidInstance(BareJID bareJid, String p_resource) {
		return new JID(bareJid, p_resource);
	}

	/**
	 * Creates intance of {@link JID JID}.
	 * 
	 * @param jid
	 *            string contains JID
	 * @return full JID.
	 */
	public static JID jidInstance(String jid) {
		String[] parsedJid = BareJID.parseJID(jid);

		return jidInstance(parsedJid[0], parsedJid[1], parsedJid[2]);
	}

	/**
	 * Creates intance of {@link JID JID}.
	 * 
	 * @param localpart
	 *            localpart
	 * @param domain
	 *            domainpart
	 * @return full JID.Resource is <code>null</code>.
	 */
	public static JID jidInstance(String localpart, String domain) {
		return jidInstance(localpart, domain, null);
	}

	/**
	 * Creates intance of {@link JID JID}.
	 * 
	 * @param localpart
	 *            localpart
	 * @param domain
	 *            domainpart
	 * @param resource
	 *            resource
	 * @return full JID.
	 */
	public static JID jidInstance(String localpart, String domain, String resource) {
		return jidInstance(BareJID.bareJIDInstance(localpart, domain), resource);
	}

	private static String toString(BareJID bareJid, String p_resource) {
		return bareJid.toString() + (((p_resource != null) && (p_resource.length() > 0)) ? "/" + p_resource : "");
	}

	private final String $toString;

	private final BareJID bareJid;

	private final String resource;

	private JID(BareJID bareJid, String resource) {
		this.bareJid = bareJid;
		this.resource = resource == null ? null : resource.intern();
		this.$toString = toString(bareJid, resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(JID o) {
		return $toString.compareTo(o.$toString);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Returns bare JID part (<code>&lt;localpart@domainpart&gt;</code>) from
	 * full JID.
	 * 
	 * @return bare JID
	 */
	public BareJID getBareJid() {
		return bareJid;
	}

	/**
	 * Return domainpart.
	 * 
	 * @return domainpart
	 */
	public String getDomain() {
		return bareJid.getDomain();
	}

	/**
	 * Return localpart.
	 * 
	 * @return localpart
	 */
	public String getLocalpart() {
		return bareJid.getLocalpart();
	}

	/**
	 * Return resource.
	 * 
	 * @return resource
	 */
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