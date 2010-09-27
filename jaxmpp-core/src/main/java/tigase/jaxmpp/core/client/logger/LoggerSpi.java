package tigase.jaxmpp.core.client.logger;

public interface LoggerSpi {

	void log(LogLevel level, String msg);

	void log(LogLevel level, String msg, Throwable thrown);
}
