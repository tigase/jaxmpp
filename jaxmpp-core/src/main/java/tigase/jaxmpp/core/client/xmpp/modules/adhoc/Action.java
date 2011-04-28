package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

public enum Action {
	/**
	 * The command should be canceled.
	 */
	cancel,
	/**
	 * The command should be completed (if possible).
	 */
	complete,
	/**
	 * The command should be executed or continue to be executed. This is the
	 * default value.
	 */
	execute,
	/**
	 * The command should progress to the next stage of execution.
	 */
	next,
	/**
	 * The command should be digress to the previous stage of execution.
	 */
	prev
}
