package org.openmrs.module.xforms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


/**
 * Provides XForm delete services.
 * 
 * @author Daniel
 *
 */
public class XformDeleteController  extends SimpleFormController{

	/** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    @Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
    	Integer formId = Integer.parseInt(request.getParameter("formId"));
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("formId", formId);
		
		String promptText = null;
		if("xslt".equals(request.getParameter("target")))
			promptText = "xforms.xsltDeleteConfirm";
		else
			promptText = "xforms.xformDeleteConfirm";
		map.put("promptText", promptText);
		
		map.put("formName", ((FormService)Context.getService(FormService.class)).getForm(formId).getName());
		return map;
	}
    
    @Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
    	return "Not Yet";
    }
    
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						
    	
		//try to authenticate users who logon inline (with the request).
		XformsUtil.authenticateInlineUser(request);

    	//check if user is authenticated
		if (!XformsUtil.isAuthenticated(request,response,"/module/xforms/xformDelete.form"))
			return null;
		
		Integer formId = Integer.parseInt(request.getParameter("formId"));
		if(request.getParameter("yes") != null){
			
			String target = request.getParameter("target");
			XformsService xformsService = (XformsService)Context.getService(XformsService.class);
			
			String successMsg = "xforms.xformDeleteSuccess";
			if("xslt".equals(target))
			{
				xformsService.deleteXslt(formId);
				successMsg = "xforms.xsltDeleteSuccess";
			}
			else
				xformsService.deleteXform(formId);
			
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, successMsg);
		}
		else
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformDeleteCancelled");
		
		return new ModelAndView(new RedirectView(request.getContextPath() + "/admin/forms/formSchemaDesign.form?formId=" + formId));
    }
}
