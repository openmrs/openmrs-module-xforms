package org.openmrs.module.xforms.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.module.Extension;
import org.openmrs.util.InsertedOrderComparator;
import org.openmrs.api.context.Context;
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
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		if (formId != null) {
			map.put("moduleServlet/xforms/xformDownload?target=xform&formId=" + formId, "xforms.downloadXform");
			map.put("module/xforms/xformDesigner.form?formId=" + formId, "xforms.designXform");
			map.put("module/xforms/xformUpload.form?formId=" + formId, "xforms.uploadXform");
			if(((XformsService)Context.getService(XformsService.class)).hasXform(Integer.valueOf(formId))){
				map.put("module/xforms/xformDelete.form?formId=" + formId, "xforms.deleteXform");
				map.put("module/xforms/xsltUpload.form?target=xslt&formId=" + formId, "xforms.uploadXslt");
				map.put("moduleServlet/xforms/xformDownload?target=xslt&formId=" + formId, "xforms.downloadXslt");
			}
		}
		
		return map;
	}
	
	
}
