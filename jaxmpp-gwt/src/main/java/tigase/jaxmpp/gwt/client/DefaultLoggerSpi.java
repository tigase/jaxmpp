package tigase.jaxmpp.gwt.client;

import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.LoggerSpi;
import tigase.jaxmpp.core.client.logger.LoggerSpiFactory;

import com.google.gwt.core.client.GWT;

public class DefaultLoggerSpi implements LoggerSpiFactory {

	@Override
	public LoggerSpi getLoggerSpi(final String name) {

		LoggerSpi spi = new LoggerSpi() {

			@Override
			public boolean isLoggable(LogLevel level) {
				return true;
			}

			@Override
			public void log(LogLevel level, String msg) {
				log(level, msg, null);
			}

			@Override
			public void log(LogLevel level, String msg, Throwable thrown) {
				GWT.log(msg, thrown);
			}
		};

		return spi;
	}
}