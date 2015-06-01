/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.page.controller;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class PatientRegPageController {
	
	public void controller(UiUtils ui, @RequestParam(value = "patientId", required = false) Patient patient, PageModel model, HttpServletRequest request) {
		
		String patientId = request.getParameter("patientId");
		
		model.addAttribute("patientId", patientId);
		
		model.addAttribute("formName", "Register a patient");
		model.addAttribute("entityFormDefDownloadUrlSuffix",
		    "moduleServlet/xforms/xformDownload?target=xformentry&contentType=xml&");
		model.addAttribute("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form");

		model.addAttribute("afterSubmitUrlSuffix", "coreapps/clinicianfacing/patient.page?");
		model.addAttribute("afterCancelUrlSuffix", "index.htm");
		
		if (!"0".equals(patientId)) {
			model.addAttribute("formName", "Edit Patient Demographics");
			model.addAttribute("afterCancelUrlSuffix", "coreapps/clinicianfacing/patient.page?patientId=" + patientId);
			model.addAttribute("formDataUploadUrlSuffix", "module/xforms/xformDataUpload.form?mode=edit");
		}
		else {
			model.addAttribute("patient", new Patient(0));
		}
		
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
