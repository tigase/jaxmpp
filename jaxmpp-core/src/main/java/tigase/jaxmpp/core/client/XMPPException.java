package tigase.jaxmpp.core.client;

public class XMPPException extends Exception {

	public static enum ErrorCondition {
		/**
		 * the sender has sent XML that is malformed or that cannot be processed
		 * (e.g., an IQ stanza that includes an unrecognized value of the 'type'
		 * attribute); the associated error type SHOULD be "modify".
		 */
		bad_request("bad-request", "modify", 400),
		/**
		 * access cannot be granted because an existing resource or session
		 * exists with the same name or address; the associated error type
		 * SHOULD be "cancel".
		 */
		conflict("conflict", "cancel", 409),
		/**
		 * the feature requested is not implemented by the recipient or server
		 * and therefore cannot be processed; the associated error type SHOULD
		 * be "cancel".
		 */
		feature_not_implemented("feature-not-implemented", "cancel", 501),
		/**
		 * the requesting entity does not possess the required permissions to
		 * perform the action; the associated error type SHOULD be "auth".
		 */
		forbidden("forbidden", "auth", 403),
		/**
		 * the recipient or server can no longer be contacted at this address
		 * (the error stanza MAY contain a new address in the XML character data
		 * of the <gone/> element); the associated error type SHOULD be
		 * "modify".
		 */
		gone("gone", "modify", 302),
		/**
		 * the server could not process the stanza because of a misconfiguration
		 * or an otherwise-undefined internal server error; the associated error
		 * type SHOULD be "wait".
		 */
		internal_server_error("internal-server-error", "wait", 500),
		/**
		 * the addressed JID or item requested cannot be found; the associated
		 * error type SHOULD be "cancel".
		 */
		item_not_found("item-not-found", "cancel", 404),
		/**
		 * the sending entity has provided or communicated an XMPP address
		 * (e.g., a value of the 'to' attribute) or aspect thereof (e.g., a
		 * resource identifier) that does not adhere to the syntax defined in
		 * Addressing Scheme (Addressing Scheme); the associated error type
		 * SHOULD be "modify".
		 */
		jid_malformed("jid-malformed", "modify", 400),
		/**
		 * the recipient or server understands the request but is refusing to
		 * process it because it does not meet criteria defined by the recipient
		 * or server (e.g., a local policy regarding acceptable words in
		 * messages); the associated error type SHOULD be "modify".
		 */
		not_acceptable("not-acceptable", "modify", 406),
		/**
		 * the recipient or server does not allow any entity to perform the
		 * action; the associated error type SHOULD be "cancel".
		 */
		not_allowed("not-allowed", "cancel", 405),
		/**
		 * the sender must provide proper credentials before being allowed to
		 * perform the action, or has provided improper credentials; the
		 * associated error type SHOULD be "auth".
		 */
		not_authorized("not-authorized", "auth", 401),
		/**
		 * the requesting entity is not authorized to access the requested
		 * service because payment is required; the associated error type SHOULD
		 * be "auth".
		 */
		payment_required("payment-required", "auth", 402),
		/**
		 * the entity has violated some local service policy; the server MAY
		 * choose to specify the policy in the <text/> element or an
		 * application-specific condition element.
		 */
		policy_violation("policy-violation", "cancel", 0),
		/**
		 * the intended recipient is temporarily unavailable; the associated
		 * error type SHOULD be "wait" (note: an application MUST NOT return
		 * this error if doing so would provide information about the intended
		 * recipient's network availability to an entity that is not authorized
		 * to know such information).
		 */
		recipient_unavailable("recipient-unavailable", "wait", 404),
		/**
		 * the recipient or server is redirecting requests for this information
		 * to another entity, usually temporarily (the error stanza SHOULD
		 * contain the alternate address, which MUST be a valid JID, in the XML
		 * character data of the <redirect/> element); the associated error type
		 * SHOULD be "modify".
		 */
		redirect("redirect", "modify", 302),
		/**
		 * the requesting entity is not authorized to access the requested
		 * service because registration is required; the associated error type
		 * SHOULD be "auth".
		 */
		registration_required("registration-required", "auth", 407),
		/**
		 * a remote server or service specified as part or all of the JID of the
		 * intended recipient does not exist; the associated error type SHOULD
		 * be "cancel".
		 */
		remote_server_not_found("remote-server-not-found", "cancel", 404),
		/**
		 * a remote server or service specified as part or all of the JID of the
		 * intended recipient (or required to fulfill a request) could not be
		 * contacted within a reasonable amount of time; the associated error
		 * type SHOULD be "wait".
		 */
		remote_server_timeout("remote-server-timeout", "wait", 504),
		/**
		 * the server or recipient lacks the system resources necessary to
		 * service the request; the associated error type SHOULD be "wait".
		 */
		resource_constraint("resource-constraint", "wait", 500),
		/**
		 * the server or recipient does not currently provide the requested
		 * service; the associated error type SHOULD be "cancel".
		 */
		service_unavailable("service-unavailable", "cancel", 503),
		/**
		 * the requesting entity is not authorized to access the requested
		 * service because a subscription is required; the associated error type
		 * SHOULD be "auth".
		 */
		subscription_required("subscription-required", "auth", 407),
		/**
		 * the error condition is not one of those defined by the other
		 * conditions in this list; any error type may be associated with this
		 * condition, and it SHOULD be used only in conjunction with an
		 * application-specific condition.
		 */
		undefined_condition("undefined-condition", null, 500),
		/**
		 * the recipient or server understood the request but was not expecting
		 * it at this time (e.g., the request was out of order); the associated
		 * error type SHOULD be "wait".
		 */
		unexpected_request("unexpected-request", "wait", 400);

		private final String elementName;

		private final int errorCode;

		private final String type;

		private ErrorCondition(String elementName, String type, int errorCode) {
			this.elementName = elementName;
			this.type = type;
			this.errorCode = errorCode;
		}

		public String getElementName() {
			return elementName;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getType() {
			return type;
		}

	}

	private static final long serialVersionUID = 1L;

	private static final String xmlns = "urn:ietf:params:xml:ns:xmpp-stanzas";

	public static String getXmlns() {
		return xmlns;
	}

	private final ErrorCondition condition;

	public XMPPException(ErrorCondition condition) {
		this.condition = condition;
	}

	public XMPPException(ErrorCondition condition, String message) {
		super(message);
		this.condition = condition;
	}

	public XMPPException(ErrorCondition condition, String message, Throwable caught) {
		super(message, caught);
		this.condition = condition;
	}

	public XMPPException(ErrorCondition condition, Throwable caught) {
		super(caught);
		this.condition = condition;
	}

	public ErrorCondition getCondition() {
		return condition;
	}

}
