package tigase.jaxmpp.core.client.logger;

public interface Logger {

	void config(String string);

	void fine(String string);

	void finer(String string);

	void finest(String string);

	void info(String string);

	boolean isLoggable(LogLevel level);

	void log(LogLevel level, String msg);

	void log(LogLevel level, String msg, Throwable thrown);
}
