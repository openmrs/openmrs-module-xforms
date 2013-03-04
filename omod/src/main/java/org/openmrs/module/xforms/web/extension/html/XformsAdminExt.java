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
