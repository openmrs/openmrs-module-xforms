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

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * Adds Xform utility links to the administration page.
 * 
 * @author Daniel
 */
public class XformsAdminExt extends AdministrationSectionExt {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * Returns the required privilege in order to see this section. Can be a comma delimited list of
	 * privileges. If the default empty string is returned, only an authenticated user is required
	 * 
	 * @return Privilege string
	 */
	public String getRequiredPrivilege() {
		return "View XForms Menu";
	}
	
	public String getTitle() {
		return "xforms.title";
	}
	
	public Map<String, String> getLinks() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		if ("true".equals(Context.getAdministrationService().getGlobalProperty("xforms.showOfflineFormDesigner", "false")))
			map.put("module/xforms/xformDesigner.form", "xforms.designer");
		
		map.put("module/xforms/xformDesigner.form?formId=0", "xforms.designPatientXform");
		map.put("module/xforms/medicalHistoryFields.form?formId=-1", "xforms.medicalHistoryFields");
		//map.put("module/xforms/xformProperties.htm", "xforms.manageXFormProperties");
		
		return map;
	}
}
