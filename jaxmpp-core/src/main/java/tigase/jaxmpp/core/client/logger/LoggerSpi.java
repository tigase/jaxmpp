package tigase.jaxmpp.core.client.logger;

public interface LoggerSpi {

	public boolean isLoggable(LogLevel level);

	void log(LogLevel level, String msg);

	void log(LogLevel level, String msg, Throwable thrown);
}
