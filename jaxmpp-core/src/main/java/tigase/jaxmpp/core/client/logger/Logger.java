package tigase.jaxmpp.core.client.logger;

public class Logger {

	private static LoggerSpiFactory spiFactory = new LoggerSpiFactory() {

		public LoggerSpi getLoggerSpi(String name) {
			return new LoggerSpi() {

				public void log(LogLevel level, String msg) {
				}

				public void log(LogLevel level, String msg, Throwable thrown) {
				}
			};
		}
	};

	public static Logger getLogger(String name) {
		final LoggerSpi spi = spiFactory.getLoggerSpi(name);
		return new Logger(spi);
	}

	public static void setLoggerSpiFactory(final LoggerSpiFactory spiFactory) {
		Logger.spiFactory = spiFactory;
	}

	private final LoggerSpi spi;

	private Logger(LoggerSpi spi) {
		this.spi = spi;
	}

	public void config(String string) {
		log(LogLevel.CONFIG, string);
	}

	public void fine(String string) {
		log(LogLevel.FINE, string);
	}

	public void finer(String string) {
		log(LogLevel.FINER, string);
	}

	public void finest(String string) {
		log(LogLevel.FINEST, string);
	}

	public void info(String string) {
		log(LogLevel.INFO, string);
	}

	public void log(LogLevel level, String msg) {
		spi.log(level, msg);
	}

	public void log(LogLevel level, String msg, Throwable thrown) {
		spi.log(level, msg, thrown);
	}

}
