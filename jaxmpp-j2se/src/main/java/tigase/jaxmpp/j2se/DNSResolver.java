package tigase.jaxmpp.j2se;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.Entry;

public class DNSResolver {

	public static void main(String[] args) throws Exception {
		System.out.println(resolve("jajcus.net"));
		System.out.println(resolve2("jajcus.net"));
	}

	public static List<Entry> resolve(final String hostname) throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>(5);
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ctx = new InitialDirContext(env);
		List<Entry> xresult = new ArrayList<Entry>();
		try {
			Attributes attrs = ctx.getAttributes("_xmpp-client._tcp." + hostname, new String[] { "SRV" });
			Attribute att = attrs.get("SRV");

			if (att != null)
				for (int i = 0; i < att.size(); i++) {
					String[] dns_resp = att.get(i).toString().split(" ");
					Integer port = 5222;
					String name = dns_resp[3];
					try {
						port = Integer.valueOf(dns_resp[2]);
					} catch (Exception e) {
						continue;
					}

					if (name == null)
						continue;

					if (name.endsWith(".")) {
						name = name.substring(0, name.length() - 1);
					}

					Entry e = new Entry(name, port);
					xresult.add(e);
				}
			else {
				Entry e = new Entry(hostname, 5222);
				xresult.add(e);
			}
		} catch (Exception caught) {
			Entry e = new Entry(hostname, 5222);
			xresult.add(e);

		}

		return xresult;
	}

	public static List<Entry> resolve2(String hostname) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		try {
			Record[] rr = (new Lookup("_xmpp-client._tcp." + hostname, Type.SRV)).run();
			for (Record r : rr) {
				SRVRecord record = (SRVRecord) r;
				String name = record.getAdditionalName().toString();
				if (name.endsWith(".")) {
					name = name.substring(0, name.length() - 1);
				}

				result.add(new Entry(name, record.getPort()));
			}
		} catch (Exception e) {
			result.add(new Entry(hostname, 5222));
		}
		return result;
	}
}
