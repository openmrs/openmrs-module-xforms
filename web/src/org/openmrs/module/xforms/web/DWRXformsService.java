package org.openmrs.module.xforms.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;


public class DWRXformsService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public String getXform(String formId){
    	Xform xform = null;
		
		//only fill the objects if the user has authenticated properly
		if (Context.isAuthenticated()) {
			XformsService svc = (XformsService)Context.getService(XformsService.class);
			if(formId != null && formId.trim().length() > 0){
				xform = svc.getXform(Integer.parseInt(formId),true);
				return xform.getXformData();
			}
		}
		
		return null;
	}
	
	public boolean saveXform(Xform xform){
		try{
			if (Context.isAuthenticated()) {
				XformsService svc = (XformsService)Context.getService(XformsService.class);
				Xform xf = svc.getXform(xform.getFormId(),true);
				xf.setXformData(xform.getXformData());
				svc.saveXform(xf);
				return true;
			}
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}
		
		return false;
	}
}
