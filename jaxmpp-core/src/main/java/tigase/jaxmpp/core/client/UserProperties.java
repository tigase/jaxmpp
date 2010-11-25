package tigase.jaxmpp.core.client;

public interface UserProperties {

	public <T> T getUserProperty(String key);

	public void setUserProperty(String key, Object value);

}
