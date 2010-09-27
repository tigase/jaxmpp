package tigase.jaxmpp.core.client.logger;

/**
 * @author bmalkow
 * 
 */
public interface LoggerSpiFactory {

	LoggerSpi getLoggerSpi(String name);

}
