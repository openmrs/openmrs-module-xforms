/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.page.controller.formentry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class XformEntryPageController {
	
	public void controller(@RequestParam("patientId") Patient patient, UiUtils ui,
	                       @RequestParam(value = "returnUrl", required = false) String returnUrl,
	                       PageModel model, HttpServletRequest request) {
		
		model.addAttribute("returnUrl", returnUrl);
		addFormEntryValues(model, request);
	}
	
	private void addFormEntryValues(PageModel model, HttpServletRequest request) {
		
		if (request.getParameter("encounterId") == null) { //Must be new form
			Integer formId = Integer.parseInt(request.getParameter("formId"));
			model.addAttribute("formId", formId);
			model.addAttribute("patientId", Integer.parseInt(request.getParameter("patientId")));
			model.addAttribute("formName", ((FormService) Context.getService(FormService.class)).getForm(formId).getName());
			model.addAttribute("entityFormDefDownloadUrlSuffix",
			    "moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&");
			model.addAttribute("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form");
		} else { //editing existing form
			Integer encounterId = Integer.parseInt(request.getParameter("encounterId"));
			Encounter encounter = Context.getEncounterService().getEncounter(encounterId);
			Form form = encounter.getForm();
			model.addAttribute("formId", form.getFormId());
			model.addAttribute("patientId", encounter.getPatientId());
			model.addAttribute("formName", ((FormService) Context.getService(FormService.class)).getForm(form.getFormId())
			        .getName());
			model.addAttribute("entityFormDefDownloadUrlSuffix",
			    "moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&encounterId=" + encounterId + "&");
			model.addAttribute("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form?mode=edit");
		}
		
		model.addAttribute("returnModule", "xforms");
		model.addAttribute("returnPage", "formentry/patient");
		
		String returnUrl = request.getParameter("returnUrl");
		if (StringUtils.isNotBlank(returnUrl)) {
			if (returnUrl.contains("clinicianfacing")) {
				returnUrl = "coreapps/clinicianfacing/patient.page?";
				model.addAttribute("returnPage", "clinicianfacing/patient");
				model.addAttribute("returnModule", "coreapps");
			}
			else if (returnUrl.contains("patientdashboard")) {
				returnUrl = "coreapps/patientdashboard/patientDashboard.page?";
				model.addAttribute("returnPage", "patientdashboard/patientDashboard");
				model.addAttribute("returnModule", "coreapps");
			}
		}
		
		String url = "coreapps/findpatient/findPatient.page?app=xforms.formentry";
		String str = request.getParameter("afterSubmitUrl");
		if (StringUtils.isNotBlank(str)) {
			url = str;
		}
		url = Context.getAdministrationService().getGlobalProperty("xforms.afterSubmitUrl", url);
		if ("coreapps/findpatient/findPatient.page?app=xforms.formentry".equals(url)) {
			url += "&afterSelectedUrl=/xforms/formentry/patient.page?patientId={{patientId}}";
		}
		
		if(StringUtils.isNotBlank(returnUrl)) {
			url = returnUrl;
		}
		model.addAttribute("afterSubmitUrlSuffix", url);
		
		
		url = "xforms/formentry/patient.page?";
		str = request.getParameter("afterCancelUrl");
		if (StringUtils.isNotBlank(str)) {
			url = str;
		}
		url = Context.getAdministrationService().getGlobalProperty("xforms.afterCancelUrl", url);
		
		if(StringUtils.isNotBlank(returnUrl)) {
			url = returnUrl;
		}
		model.addAttribute("afterCancelUrlSuffix", url);
		
		
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_DATE_SUBMIT_FORMAT,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_SUBMIT_FORMAT,
		        XformConstants.DEFAULT_DATE_SUBMIT_FORMAT));
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_DATE_DISPLAY_FORMAT,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_DISPLAY_FORMAT,
		        XformConstants.DEFAULT_DATE_DISPLAY_FORMAT));
		model.addAttribute(XformConstants.FORM_DESIGNER_KEY_DEFAULT_FONT_FAMILY, Context.getAdministrationService()
		        .getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DEFAULT_FONT_FAMILY, XformConstants.DEFAULT_FONT_FAMILY));
		model.addAttribute(XformConstants.FORM_DESIGNER_KEY_DEFAULT_FONT_SIZE, Context.getAdministrationService()
		        .getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DEFAULT_FONT_SIZE, XformConstants.DEFAULT_FONT_SIZE));
		
		String color = "#8FABC7";
		String theme = Context.getAdministrationService().getGlobalProperty("default_theme", "legacy");
		if ("orange".equals(theme))
			color = "#f48a52";
		else if ("purple".equals(theme))
			color = "#8c87c5";
		else if ("green".equals(theme))
			color = "#1aac9b";
		
		model.addAttribute(XformConstants.FORM_DESIGNER_KEY_DEFAULT_GROUPBOX_HEADER_BG_COLOR, color);
		
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_DATE_TIME_SUBMIT_FORMAT,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_SUBMIT_FORMAT,
		        XformConstants.DEFAULT_DATE_TIME_SUBMIT_FORMAT));
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_DATE_TIME_DISPLAY_FORMAT,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_DISPLAY_FORMAT,
		        XformConstants.DEFAULT_DATE_TIME_DISPLAY_FORMAT));
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_TIME_SUBMIT_FORMAT,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_SUBMIT_FORMAT,
		        XformConstants.DEFAULT_TIME_SUBMIT_FORMAT));
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_TIME_DISPLAY_FORMAT,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_DISPLAY_FORMAT,
		        XformConstants.DEFAULT_TIME_DISPLAY_FORMAT));
		
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_SHOW_SUBMIT_SUCCESS_MSG,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_SHOW_SUBMIT_SUCCESS_MSG,
		        XformConstants.DEFAULT_SHOW_SUBMIT_SUCCESS_MSG));
		
		model.addAttribute(XformConstants.FORM_DESIGNER_KEY_LOCALE_KEY, Context.getAdministrationService()
		        .getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_LOCALE, Context.getLocale().getLanguage()));
		model.addAttribute(
		    XformConstants.FORM_DESIGNER_KEY_DECIMAL_SEPARATORS,
		    Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DECIMAL_SEPARATORS,
		        XformConstants.DEFAULT_DECIMAL_SEPARATORS));
		model.addAttribute("usingJQuery", XformsUtil.usesJquery());
		model.addAttribute("locations", Context.getLocationService().getAllLocations(false));
		model.addAttribute("formatXml", "false");
		model.addAttribute("useOpenmrsMessageTag", XformsUtil.isOnePointNineOneAndAbove());
	}
}
