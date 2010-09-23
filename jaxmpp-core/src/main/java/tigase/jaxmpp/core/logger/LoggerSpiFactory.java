package tigase.jaxmpp.core.logger;

/**
 * @author bmalkow
 * 
 */
public interface LoggerSpiFactory {

	LoggerSpi getLoggerSpi(String name);

}
