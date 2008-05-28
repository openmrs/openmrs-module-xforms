package org.openmrs.module.xforms.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.download.XformDataUploadManager;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


/**
 * Provides XForm data upload services.
 * 
 * @author Daniel
 *
 */
public class XformDataUploadController extends SimpleFormController{

	 /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    @Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		return new HashMap<String,Object>();
	}


	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						
		
		//try to authenticate users who logon inline (with the request).
		XformsUtil.authenticateInlineUser(request);
		
		// check if user is authenticated
		if (!XformsUtil.isAuthenticated(request,response,"/module/xforms/xformDataUpload.form"))
			return null;
		
		request.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		
		if(XformConstants.TRUE_TEXT_VALUE.equalsIgnoreCase(request.getParameter(XformConstants.REQUEST_PARAM_BATCH_ENTRY)))
			XformDataUploadManager.submitXforms(request.getInputStream(),request.getSession().getId(),XformsUtil.getActionUrl(request));
		else{
			XformDataUploadManager.processXform(IOUtils.toString(request.getInputStream()),request.getSession().getId(),XformsUtil.getEnterer());
			setSingleEntryResponse(request, response);
		}
		
		return null;
    }
	
	/**
	 * Write the response after processing an xform submitted from the browser.
	 * 
	 * @param request - the request.
	 * @param response - the response.
	 */
	private void setSingleEntryResponse(HttpServletRequest request, HttpServletResponse response){
		
		String searchNew = Context.getAdministrationService().getGlobalProperty("xforms.searchNewPatientAfterFormSubmission");
		String url = "/findPatient.htm";
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(searchNew)){
			String patientId = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_ID);
			url = "/patientDashboard.form?patientId="+patientId;
		}
		url = request.getContextPath() + url;
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html;charset=utf-8");
		
		try{
			//We are using an iframe to display the xform within the page.
			//So this response just tells the iframe parent document to go to either the
			//patient dashboard, or to the search patient screen, depending on the user's settings.
			response.getOutputStream().println("<html>" + "<head>"
					+"<script type='text/javascript'> window.onload=function() {self.parent.location.href='" + url + "';}; </script>"
					+"</head>" + "</html>");
		}
		catch(IOException e){
			log.error(e);
		}
	}

    @Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
    	return "";
    }
}
