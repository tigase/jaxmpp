/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
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