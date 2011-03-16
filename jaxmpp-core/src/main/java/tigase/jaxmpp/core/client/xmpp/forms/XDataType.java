package tigase.jaxmpp.core.client.xmpp.forms;

public enum XDataType {
	/**
	 * The form-submitting entity has cancelled submission of data to the
	 * form-processing entity.
	 */
	cancel,

	/**
	 * The form-processing entity is asking the form-submitting entity to
	 * complete a form.
	 */
	form,

	/**
	 * The form-processing entity is returning data (e.g., search results) to
	 * the form-submitting entity, or the data is a generic data set.
	 */
	result,

	/**
	 * The form-submitting entity is submitting data to the form-processing
	 * entity. The submission MAY include fields that were not provided in the
	 * empty form, but the form-processing entity MUST ignore any fields that it
	 * does not understand.
	 */
	submit
}
