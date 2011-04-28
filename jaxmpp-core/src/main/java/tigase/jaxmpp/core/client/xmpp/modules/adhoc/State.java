package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

public enum State {

	/**
	 * The command has been canceled. The command session has ended.
	 */
	canceled,

	/**
	 * The command has completed. The command session has ended.
	 */
	completed,

	/**
	 * The command is being executed.
	 */
	executing
}
