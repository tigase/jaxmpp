package tigase.jaxmpp.core.client.logger;

/**
 * Basic logging interface.<br/>
 * 
 * Should be instantiated by {@linkplain LoggerFactory LoggerFactory}.<br/>
 * For example:
 * 
 * <pre>
 * Logger l = LoggerFactory.getLogger(Test.class);
 * if (l.isLoggable(LogLevel.CONFIG))
 * 	l.config(&quot;Logger successfully initialized&quot;);
 * </pre>
 * 
 * @author bmalkow
 * 
 */
public interface Logger {

	/**
	 * Log message with {@linkplain LogLevel#CONFIG config} log level.
	 * 
	 * @param message
	 *            log message
	 */
	void config(String message);

	/**
	 * Log message with {@linkplain LogLevel#FINE fine} log level.
	 * 
	 * @param message
	 *            log message
	 */
	void fine(String message);

	/**
	 * Log message with {@linkplain LogLevel#FINER finer} log level.
	 * 
	 * @param message
	 *            log message
	 */
	void finer(String message);

	/**
	 * Log message with {@linkplain LogLevel#FINEST finest} log level.
	 * 
	 * @param message
	 *            log message
	 */
	void finest(String message);

	/**
	 * Log message with {@linkplain LogLevel#INFO info} log level.
	 * 
	 * @param message
	 *            log message
	 */
	void info(String message);

	/**
	 * Is given log level enabled?
	 * 
	 * @param level
	 *            log level to check
	 * @return <code>true</code> if given log level is enabled
	 */
	boolean isLoggable(LogLevel level);

	/**
	 * Log message with given log level.
	 * 
	 * @param level
	 *            log level
	 * @param message
	 *            log message
	 */
	void log(LogLevel level, String message);

	/**
	 * Log an error with given log level.
	 * 
	 * @param level
	 *            log level
	 * @param message
	 *            error message
	 * @param thrown
	 *            cause
	 */
	void log(LogLevel level, String message, Throwable thrown);
}
