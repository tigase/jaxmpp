package tigase.jaxmpp.core.logger;

public interface LoggerSpi {

	void log(LogLevel level, String msg);

	void log(LogLevel level, String msg, Throwable thrown);
}
