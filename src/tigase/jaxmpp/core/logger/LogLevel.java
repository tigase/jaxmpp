package tigase.jaxmpp.core.logger;

public enum LogLevel {
	ALL(-1),
	CONFIG(700),
	FINE(500),
	FINER(400),
	FINEST(300),
	INFO(800),
	OFF(10000),
	SEVERE(1000),
	WARNING(900);

	private final int value;

	private LogLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
