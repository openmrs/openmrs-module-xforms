/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.web.WebConstants;
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

	/**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#showForm(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
        throws Exception {
	    
    	if ("true".equals(request.getParameter("refappui"))) {
    		response.sendRedirect("/" + WebConstants.WEBAPP_NAME + "/xforms/formentry/xformEntry.page?" + request.getQueryString());
			return null;
		}
    	else {
    		return super.showForm(request, response, errors);
    	}
    }


	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		HashMap<String,Object> map = new HashMap<String,Object>();

		Encounter encounter = null;
		Integer patientId =  null;
		Integer formId = null;
		
		String id = request.getParameter("encounterId");
		if (StringUtils.isBlank(id)) {
			patientId = Integer.parseInt(request.getParameter("patientId"));
			formId = Integer.parseInt(request.getParameter("formId"));
			
			if (isSingleEntryForm(formId.toString())) {
				List<Encounter> encounters = Context.getEncounterService().getEncountersByPatientId(patientId);
				if (encounters.size() > 0) {
					encounter = encounters.get(0);
				}
			}
		}
		else {
			encounter = Context.getEncounterService().getEncounter(Integer.parseInt(id));
		}
		
		if(encounter == null){ //Must be new form
			map.put("formId", formId);
			map.put("patientId", patientId);
			map.put("formName", ((FormService)Context.getService(FormService.class)).getForm(formId).getName());
			map.put("entityFormDefDownloadUrlSuffix", "moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&");
			map.put("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form");
		}
		else{ //editing existing form
			Form form = encounter.getForm();
			map.put("formId", form.getFormId());
			map.put("patientId", encounter.getPatient().getPatientId());
			map.put("formName", ((FormService)Context.getService(FormService.class)).getForm(form.getFormId()).getName());
			map.put("entityFormDefDownloadUrlSuffix", "moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&encounterId="+encounter.getEncounterId()+"&");
			map.put("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form?mode=edit");
		}
		
		String url = "patientDashboard.form?";
		map.put("afterSubmitUrlSuffix", url);
		map.put("afterCancelUrlSuffix", url);

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
		map.put("usingJQuery", XformsUtil.usesJquery());
		map.put("locations", Context.getLocationService().getAllLocations(false));
		map.put("formatXml", "false");
		map.put("useOpenmrsMessageTag", XformsUtil.isOnePointNineOneAndAbove());
		
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
	
	private boolean isSingleEntryForm(String formId) {
    	if (StringUtils.isBlank(formId)) {
    		return false;
    	}
    	
		String formIds = Context.getAdministrationService().getGlobalProperty("xforms.singleEntryForms");
		if (!StringUtils.isBlank(formIds)) {
			String[] ids = formIds.split(",");
			for (String id : ids) {
				if (formId.equals(id)) {
					return true;
				}
			}
		}
		return false;
	}
}
