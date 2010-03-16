package org.openmrs.module.xforms.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.util.InsertedOrderComparator;

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
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
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
			
			if(xformsService == null || xformsService.hasXform(Integer.valueOf(formId)))
				map.put("module/xforms/xformDelete.form?target=xform&formId=" + formId, "xforms.deleteXform");
		}
		
		return map;
	}
	
	
}
