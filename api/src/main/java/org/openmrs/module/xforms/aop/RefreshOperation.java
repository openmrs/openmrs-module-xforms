/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.aop;

/**
 * Represents the three operations on a provider, location or concept in an xforms document.
 */
public enum RefreshOperation {
	
	/** New provider, location or concept added. */
	ADD,

	/** Provider, location, or concept name has been changed. */
	EDIT,

	/** Provider, location or concept has been deleted. */
	DELETE
}
