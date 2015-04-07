/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.web.extension.html;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.PatientDashboardTabExt;


/**
 * Adds the XForms tab to the patient dashboard, which allows XForms based data entry.
 * 
 * @author Daniel
 *
 */
public class XformsPatientDashboardTabExt  extends PatientDashboardTabExt{

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public String getTabName() {
		return "xforms.patientDashboard.forms";
	}
	
	@Override
	public String getRequiredPrivilege() {
		return "Patient Dashboard - View Forms Section";
	}
	
	@Override
	public String getTabId() {
		return "patientXformsSelect";
	}
	
	@Override
	public String getPortletUrl() {
		return "patientXformsSelect";
	}
}
