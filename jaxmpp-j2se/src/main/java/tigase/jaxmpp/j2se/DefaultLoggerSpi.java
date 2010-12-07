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
			public boolean isLoggable(LogLevel level) {
				return log.isLoggable(convert(level));
			}

			@Override
			public void log(LogLevel level, String msg) {
				log(level, msg, null);
			}

			@Override
			public void log(LogLevel level, String msg, Throwable ex) {
				final Level $level = convert(level);
				if (log.isLoggable($level)) {
					Throwable dummyException = new Throwable();
					StackTraceElement locations[] = dummyException.getStackTrace();
					String cname = "unknown";
					String method = "unknown";
					if (locations != null && locations.length > 5) {
						StackTraceElement caller = locations[5];
						cname = caller.getClassName();
						method = caller.getMethodName();
					}
					if (ex == null) {
						log.logp($level, cname, method, msg);
					} else {
						log.logp($level, cname, method, msg, ex);
					}
				}
			}
		};

		return spi;
	}
}