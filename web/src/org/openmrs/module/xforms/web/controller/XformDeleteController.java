package org.openmrs.module.xforms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import org.openmrs.module.formentry.FormEntryService;


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
		map.put("formName", ((FormEntryService)Context.getService(FormEntryService.class)).getForm(formId).getName());
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
			((XformsService)Context.getService(XformsService.class)).deleteXform(formId);
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformDeleteSuccess");
		}
		else
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformDeleteCancelled");
		
		return new ModelAndView(new RedirectView(request.getContextPath() + "/admin/forms/formSchemaDesign.form?formId=" + formId));
    }
}
