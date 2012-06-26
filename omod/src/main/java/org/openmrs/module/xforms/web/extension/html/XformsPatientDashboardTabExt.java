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
