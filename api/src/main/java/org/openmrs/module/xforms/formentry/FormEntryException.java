/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.formentry;

/**
 * Represents often fatal errors that occur within the form entry module
 * 
 */
public class FormEntryException extends RuntimeException {

	public static final long serialVersionUID = 121212344443122L;

	public FormEntryException() {
	}

	public FormEntryException(String message) {
		super(message);
	}

	public FormEntryException(String message, Throwable cause) {
		super(message, cause);
	}

	public FormEntryException(Throwable cause) {
		super(cause);
	}

}
