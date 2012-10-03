package org.openmrs.module.xforms.web.extension.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.xforms.BasicFormBuilder;
import org.openmrs.module.xforms.XformsService;

/**
 * Adds XForm links to the form schema design page.
 * 
 * @author Daniel
 *
 */
public class XformsFormHeaderExt extends Extension {

	private String formId;
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public void initialize(Map<String, String> parameters) {
		formId = parameters.get("formId");
	}

	public Map<String, String> getLinks() {
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		if (formId != null && formId.trim().length() > 0) {
			map.put("moduleServlet/xforms/xformDownload?target=xform&formId=" + formId, "xforms.downloadXform");
			map.put("module/xforms/xformDesigner.form?formId=" + formId, "xforms.designXform");
			
			//i for now do not see any need of having xforms not passing
			//through the form designer. Infact via this route, we even have a bug
			//where the form layout gets deleted, which is very dangerous.
			//map.put("module/xforms/xformUpload.form?formId=" + formId, "xforms.uploadXform");
			
			XformsService xformsService = null;
			try{
				xformsService = (XformsService)Context.getService(XformsService.class);
			}catch(APIException ex){
				//In openmrs from version 1.6 and above, we get
				//APIException: Service not found: interface org.openmrs.module.xforms.XformsService
				ex.printStackTrace();
			}
			
			if(xformsService == null || xformsService.hasXform(Integer.valueOf(formId))){
				map.put("module/xforms/xformDelete.form?target=xform&formId=" + formId, "xforms.deleteXform");
			}
			
			
			//If form without a single form field, add the default fields.
			Form form = Context.getFormService().getForm(Integer.parseInt(formId));
			if(form != null && (form.getFormFields() == null || form.getFormFields().size() == 0)){
				BasicFormBuilder.addDefaultFields(form);
			}
		}
		
		return map;
	}
}
