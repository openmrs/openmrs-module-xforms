package org.openmrs.module.xforms.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
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
                
    @Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
        HashMap<String,Object> map = new HashMap<String,Object>();
        
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
        
        request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformPropertiesSaveSuccess");
        return new ModelAndView(new RedirectView(getSuccessView()));
    }


    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception { 
        return "Not Yet";
    }   
}
