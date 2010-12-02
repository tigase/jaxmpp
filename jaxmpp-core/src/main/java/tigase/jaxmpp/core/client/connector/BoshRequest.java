package tigase.jaxmpp.core.client.connector;

public interface BoshRequest extends Runnable {

	String getRid();

	void terminate();
}
