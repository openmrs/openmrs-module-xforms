/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms;

import org.kxml2.kdom.Element;
import org.openmrs.Patient;
import org.openmrs.Person;

/**
 * Utility for building relationship submission in from data entry xforms.
 * 
 * @since 4.0.3
 */
public class RelationshipSubmission {
	
	/**
	 * Saves relationships which have been edited, added, or deleted.
	 * 
	 * @param node the form root node.
	 * @param patient the patient that the form has been submitted for.
	 */
	public static void submit(Element node, Patient patient) {
	
		//Person person; person.
	}
}
