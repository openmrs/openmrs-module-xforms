package org.openmrs.module.xforms.extension.html;

import java.util.Map;
import java.util.TreeMap;

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
		
		if (formId != null) {
			map.put("moduleServlet/xforms/xformDownload?target=xform&formId=" + formId, "xforms.downloadXform");
			map.put("module/xforms/xformDesigner.form?formId=" + formId, "xforms.designXform");
			map.put("module/xforms/xformUpload.form?formId=" + formId, "xforms.uploadXform");
			
			XformsService xformsService = (XformsService)Context.getService(XformsService.class);
			if(xformsService.hasXform(Integer.valueOf(formId)))
				map.put("module/xforms/xformDelete.form?target=xform&formId=" + formId, "xforms.deleteXform");
			
			map.put("moduleServlet/xforms/xformDownload?target=xslt&formId=" + formId, "xforms.downloadXslt");
			map.put("module/xforms/xsltUpload.form?target=xslt&formId=" + formId, "xforms.uploadXslt");
						
			if(xformsService.hasXslt(Integer.valueOf(formId)))
				map.put("module/xforms/xformDelete.form?target=xslt&formId=" + formId, "xforms.deleteXslt");
		}
		
		return map;
	}
	
	
}
