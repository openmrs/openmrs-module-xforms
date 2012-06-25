package org.openmrs.module.xforms.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class XformPropertiesController  extends SimpleFormController{
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    private Integer formId;
    private List<GlobalProperty> gps;
    private List<GlobalProperty> xformGps = new ArrayList<GlobalProperty>();
   
    public static final String PROP_NAME = "property";
	
	public static final String PROP_VAL_NAME = "value";
	
	public static final String PROP_DESC_NAME = "description";
    @Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
        HashMap<String,Object> map = new HashMap<String,Object>();
        
        gps = Context.getAdministrationService().getAllGlobalProperties();
        
        
        for (GlobalProperty gp :gps)
        	if (gp.getProperty().startsWith("xform")){
        		xformGps.add(gp);
        	}
        	
        
        
        
        map.put("xformsProps",xformGps);
        /*String id = request.getParameter("formId");
        if(id != null && id.trim().length() > 0){
            XformsService xformsService = (XformsService)Context.getService(XformsService.class);
            formId = Integer.parseInt(id);
            Xform xform = xformsService.getXform(formId);
            if(xform == null){
                xform = new Xform();
                xform.setFormId(formId);
                xform.setDateCreated(new Date());
                xform.setCreator(Context.getAuthenticatedUser());
                xform.setPublish(true);
                xformsService.saveXform(xform);
            }
            map.put("publish", xform.isPublish());
        }*/
        
        
        
        return map;
        
       
    }


    //Can't see current usage for this.
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {                       
        /*XformsService xformsService = (XformsService)Context.getService(XformsService.class);
        Xform xform = xformsService.getXform(formId);
        if(request.getParameter("publish") != null)
            xform.setPublish(true);
        else
            xform.setPublish(false);
        xformsService.saveXform(xform);*/
        
        //request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformPropertiesSaveSuccess");
       // return new ModelAndView(new RedirectView(getSuccessView()));
    	
    	String action = request.getParameter("action");
		if (action == null)
			action = "cancel";
		
		if (action.equals(getMessageSourceAccessor().getMessage("general.save"))) {
			HttpSession httpSession = request.getSession();
			
			if (Context.isAuthenticated()) {
				AdministrationService as = Context.getAdministrationService();
				
				// fetch the backing object
				// and save it to a hashmap for easy retrieval of already-used-GPs
				List<GlobalProperty> formBackingObject = (List<GlobalProperty>) object;
				Map<String, GlobalProperty> formBackingObjectMap = new HashMap<String, GlobalProperty>();
				for (GlobalProperty prop : formBackingObject) {
					formBackingObjectMap.put(prop.getProperty(), prop);
				}
				
				// the list we'll save to the database
				List<GlobalProperty> globalPropList = new ArrayList<GlobalProperty>();
				
				globalPropList = as.getAllGlobalProperties();
				
				
				String[] keys = request.getParameterValues(PROP_NAME);
				String[] values = request.getParameterValues(PROP_VAL_NAME);
				String[] descriptions = request.getParameterValues(PROP_DESC_NAME);
				
				for (int x = 0; x < keys.length; x++) {
					String key = keys[x];
					String val = values[x];
					String desc = descriptions[x];
					
					// try to get an already-used global property for this key
					GlobalProperty tmpGlobalProperty = formBackingObjectMap.get(key);
					
					// if it exists, use that object...just update it
					if (tmpGlobalProperty != null) {
						tmpGlobalProperty.setPropertyValue(val);
						tmpGlobalProperty.setDescription(desc);
						globalPropList.add(tmpGlobalProperty);
					} else {
						// if it doesn't exist, create a new global property
						globalPropList.add(new GlobalProperty(key, val, desc));
					}
				}
				
				try {
					as.saveGlobalProperties(globalPropList);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.saved");
					
					// refresh log level from global property(ies)
					OpenmrsUtil.applyLogLevels();
				}
				catch (Exception e) {
					log.error("Error saving properties", e);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.not.saved");
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, e.getMessage());
				}
				
				return new ModelAndView(new RedirectView(getSuccessView()));
				
			}
		}
		
		return showForm(request, response, exceptions);
		
    }


    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception { 
    	if (Context.isAuthenticated()) {
			// return a non-empty list if the user has authenticated properly
			AdministrationService as = Context.getAdministrationService();
			return as.getAllGlobalProperties();
		} else
			return new ArrayList<GlobalProperty>();
    } 
    
}
