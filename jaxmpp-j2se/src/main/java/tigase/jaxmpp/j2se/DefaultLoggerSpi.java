package tigase.jaxmpp.j2se;

import java.util.logging.Level;

import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.LoggerSpi;
import tigase.jaxmpp.core.client.logger.LoggerSpiFactory;

public class DefaultLoggerSpi implements LoggerSpiFactory {

	@Override
	public LoggerSpi getLoggerSpi(final String name) {
		final java.util.logging.Logger log = java.util.logging.Logger.getLogger(name);

		LoggerSpi spi = new LoggerSpi() {

			private final Level convert(LogLevel l) {
				switch (l) {
				case ALL:
					return Level.ALL;
				case CONFIG:
					return Level.CONFIG;
				case FINE:
					return Level.FINE;
				case FINER:
					return Level.FINER;
				case FINEST:
					return Level.FINEST;
				case INFO:
					return Level.INFO;
				case OFF:
					return Level.OFF;
				case SEVERE:
					return Level.SEVERE;
				case WARNING:
					return Level.WARNING;
				default:
					return Level.INFO;
				}
			}

			@Override
			public void log(LogLevel level, String msg) {
				log.log(convert(level), msg);
			}

			@Override
			public void log(LogLevel level, String msg, Throwable thrown) {
				log.log(convert(level), msg, thrown);
			}
		};

		return spi;
	}
}