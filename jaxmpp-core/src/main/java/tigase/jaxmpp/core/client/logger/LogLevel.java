package tigase.jaxmpp.core.client.logger;

/**
 * Definition of levels of log.<br/>
 * From lowest to highest:
 * <ul>
 * <li>FINEST</li>
 * <li>FINER</li>
 * <li>FINE</li>
 * <li>CONFIG</li>
 * <li>INFO</li>
 * <li>WARNING</li>
 * <li>SEVERE</li>
 * </ul>
 * 
 * @author bmalkow
 */
public enum LogLevel {
	ALL(-1),
	/**
	 * Log level for configuration messages.
	 */
	CONFIG(700),
	/**
	 * Log level for tracing information.
	 */
	FINE(500),
	/**
	 * Log level for detailed tracing information.
	 */
	FINER(400),
	/**
	 * Log level for more detailed tracing information.
	 */
	FINEST(300),
	/**
	 * Log level for informational messages.
	 */
	INFO(800),
	OFF(10000),
	/**
	 * Log level for errors.
	 */
	SEVERE(1000),
	/**
	 * Log level for potential problems.
	 */
	WARNING(900);

	private final int value;

	private LogLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
