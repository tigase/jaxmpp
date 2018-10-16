package tigase.jaxmpp.j2se.connectors.socket;

import java.security.cert.Certificate;

/**
 * Created by bmalkow on 17.07.2017.
 */
public interface JaxmppHostnameVerifier {

	boolean verify(String hostname, Certificate certificate);
}
