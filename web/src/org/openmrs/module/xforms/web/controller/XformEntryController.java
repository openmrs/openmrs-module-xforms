package org.openmrs.module.xforms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Provides browser based XForm data entry services.
 * 
 * @author Daniel
 *
 */
public class XformEntryController extends SimpleFormController{

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    @Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
    	HashMap<String,Object> map = new HashMap<String,Object>();
    	
    	Integer formId = Integer.parseInt(request.getParameter("formId"));
    	map.put("formId", formId);
    	map.put("patientId", Integer.parseInt(request.getParameter("patientId")));
 		map.put("formName", ((FormService)Context.getService(FormService.class)).getForm(formId).getName());

    	return map;
	}


    //Can't see current usage for this.
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						
		return new ModelAndView(new RedirectView(getSuccessView()));
    }


    @Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
    	return "Not Yet";
    }    
}
