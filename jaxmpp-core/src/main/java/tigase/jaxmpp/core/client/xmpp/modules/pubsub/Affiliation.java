package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

public enum Affiliation {
	/** */
	member,
	/** */
	none,
	/** An entity that is disallowed from subscribing or publishing to a node. */
	outcast,
	/**
	 * The manager of a node, of which there may be more than one; often but not
	 * necessarily the node creator.
	 */
	owner,
	/** An entity that is allowed to publish items to a node. */
	publisher;
}
