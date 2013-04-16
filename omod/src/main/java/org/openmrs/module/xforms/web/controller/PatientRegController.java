package org.openmrs.module.xforms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.util.XformsUtil;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


public class PatientRegController extends SimpleFormController{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		HashMap<String,Object> map = new HashMap<String,Object>();

		map.put("formId", 0);
     	
     	Integer patientId = null;
     	String id = request.getParameter("patientId");
     	if(id != null && id.trim().length() > 0)
     		patientId = Integer.parseInt(id);
     	
     	if(patientId == null)
     		patientId = 0;
     	
    	map.put("patientId", patientId);
    	
        String patientParams = "";
        String s = request.getParameter("addName");
        if(s != null && s.trim().length() > 0)
        	patientParams += "&addName=" + s;
        
        s = request.getParameter("addBirthdate");
        if(s != null && s.trim().length() > 0)
        	patientParams += "&addBirthdate=" + s;
        
        s = request.getParameter("addAge");
        if(s != null && s.trim().length() > 0)
        	patientParams += "&addAge=" + s;
        
        s = request.getParameter("addGender");
        if(s != null && s.trim().length() > 0)
        	patientParams += "&addGender=" + s;
       
        map.put("patientParams", patientParams);
        
        //"?addName=" + name + "&addBirthdate=" + birthdate + "&addAge=" + age + "&addGender=" + gender;

		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_SUBMIT_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_SUBMIT_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_DISPLAY_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_DISPLAY_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_DEFAULT_FONT_FAMILY, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DEFAULT_FONT_FAMILY,XformConstants.DEFAULT_FONT_FAMILY));
		map.put(XformConstants.FORM_DESIGNER_KEY_DEFAULT_FONT_SIZE, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DEFAULT_FONT_SIZE,XformConstants.DEFAULT_FONT_SIZE));

		String color = "#8FABC7";
		String theme = Context.getAdministrationService().getGlobalProperty("default_theme", "legacy");
		if("orange".equals(theme))
			color = "#f48a52";
		else if("purple".equals(theme))
			color = "#8c87c5";
		else if("green".equals(theme))
			color = "#1aac9b";
		
		map.put(XformConstants.FORM_DESIGNER_KEY_DEFAULT_GROUPBOX_HEADER_BG_COLOR, color);
		
		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_TIME_SUBMIT_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_TIME_SUBMIT_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_TIME_DISPLAY_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_TIME_DISPLAY_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_TIME_SUBMIT_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_TIME_SUBMIT_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_TIME_DISPLAY_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_DISPLAY_FORMAT,XformConstants.DEFAULT_TIME_DISPLAY_FORMAT));

		map.put(XformConstants.FORM_DESIGNER_KEY_SHOW_SUBMIT_SUCCESS_MSG, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_SHOW_SUBMIT_SUCCESS_MSG,XformConstants.DEFAULT_SHOW_SUBMIT_SUCCESS_MSG));

		map.put(XformConstants.FORM_DESIGNER_KEY_LOCALE_KEY, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_LOCALE, Context.getLocale().getLanguage()));
		map.put(XformConstants.FORM_DESIGNER_KEY_DECIMAL_SEPARATORS, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DECIMAL_SEPARATORS, XformConstants.DEFAULT_DECIMAL_SEPARATORS));

		map.put("appendEntityIdAfterSubmit", "1");
		map.put("appendEntityIdAfterCancel", "1");
		map.put("afterSubmitUrlSuffix", "patientDashboard.form?");
		map.put("afterCancelUrlSuffix", "patientDashboard.form?");
		
		if(request.getParameter("patientId") != null)
			map.put("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form?mode=edit");
		else{
			map.put("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form");
			
			map.put("appendEntityIdAfterCancel", "0");
			map.put("afterCancelUrlSuffix", "findPatient.htm?");
		}
		
		map.put("useOpenmrsMessageTag", XformsUtil.isOnePointNineOneAndAbove());
		map.put("usingJQuery", XformsUtil.usesJquery());
		map.put("locations", Context.getLocationService().getAllLocations(false));
		map.put("formatXml", "false");
		
		return map;
	}


	//Can't see current usage for this.
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						
		return new ModelAndView(new RedirectView(getSuccessView()));
	}


	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
		return "";
	}    
}