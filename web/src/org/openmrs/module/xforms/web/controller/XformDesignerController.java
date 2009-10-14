package org.openmrs.module.xforms.web.controller;

//baseRelativePath: "scripts/dojo/",

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs and saves XForm designs.
 * 
 * As per the current version, we deal with the following:
 * 
 * For the XForm:
 * 1 - Changing question properties like display text, readonly, required, disabled, visibility etc.
 * 2 - Order of questions.
 * 3 - Branching or Skipping logic.
 * 4 - 
 * 
 * For the XHTML as XSL transformed from the XForm:
 * 1 - Grouping of questions into various tabs. This avoids long scrollings between questions on a form.
 * 2 - 
 * 
 */
public class XformDesignerController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {    	
		Map<String, Object> map = new HashMap<String, Object>();
		String formId = request.getParameter("formId");

		String allowBindEdit = "1";
		if(formId != null && formId.trim().length() > 0){
			map.put("formId",Integer.parseInt(formId));
			allowBindEdit = "0";
		}
		else
			map.put("formId",-1);

		map.put("allowBindEdit", allowBindEdit);
		map.put("showLanguageTab", allowBindEdit);

		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_SUBMIT_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_SUBMIT_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_DISPLAY_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_DISPLAY_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_DEFAULT_FONT_FAMILY, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DEFAULT_FONT_FAMILY,XformConstants.DEFAULT_FONT_FAMILY));

		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_TIME_SUBMIT_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_TIME_SUBMIT_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_DATE_TIME_DISPLAY_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_TIME_DISPLAY_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_TIME_SUBMIT_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_TIME_SUBMIT_FORMAT));
		map.put(XformConstants.FORM_DESIGNER_KEY_TIME_DISPLAY_FORMAT, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_DISPLAY_FORMAT,XformConstants.DEFAULT_TIME_DISPLAY_FORMAT));

		map.put(XformConstants.FORM_DESIGNER_KEY_SHOW_SUBMIT_SUCCESS_MSG, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_SHOW_SUBMIT_SUCCESS_MSG,XformConstants.DEFAULT_SHOW_SUBMIT_SUCCESS_MSG));
		map.put(XformConstants.FORM_DESIGNER_KEY_LOCALE_LIST, Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_LOCALE_LIST,XformConstants.DEFAULT_LOCALE_LIST));


		String url = request.getRequestURI();
		url = url.substring(0, url.indexOf("module/xforms/xformDesigner.form"));

		if(formId != null && formId.trim().length() > 0 && !formId.equals("0"))
			url += "admin/forms/formEdit.form?formId=" + formId;
		else
			url += "admin/index.htm";

		map.put("closeUrl", url);

		//http://127.0.0.1:8080/openmrs/admin/forms/formEdit.form?formId=20
		//http://127.0.0.1:8080/openmrs/admin/index.htm

		//http://127.0.0.1:8080/openmrs/module/xforms/xformDesigner.form

		return map;

	}


	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						
		return new ModelAndView(new RedirectView(getSuccessView()));
	}


	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
		return "Not Yet";
	}    
}
