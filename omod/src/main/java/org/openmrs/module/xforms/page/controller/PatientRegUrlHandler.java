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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.ui.framework.UiFrameworkException;
import org.openmrs.ui.framework.WebConstants;
import org.openmrs.ui.framework.page.FileDownload;
import org.openmrs.ui.framework.page.PageAction;
import org.openmrs.ui.framework.page.PageFactory;
import org.openmrs.ui.framework.page.PageRequest;
import org.openmrs.ui.framework.page.Redirect;
import org.openmrs.ui.framework.session.Session;
import org.openmrs.ui.framework.session.SessionFactory;
import org.openmrs.ui.util.ExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@OpenmrsProfile(modules = { "uiframework:*.*" })
public class PatientRegUrlHandler {
	
	public final static String SHOW_HTML_VIEW = "/module/uiframework/showHtml";
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	@Qualifier("corePageFactory")
	PageFactory pageFactory;
	
	@RequestMapping(value = "/registrationapp/registerPatient.page", method = {RequestMethod.GET, RequestMethod.POST})
	public String handleNewPatientPage(HttpServletRequest request, HttpServletResponse response, Model model, HttpSession httpSession) {
		
		if("true".equals(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USE_PATIENT_XFORM,"false"))) {
			return "redirect:/xforms/patientReg.page?target=xformentry&formId=0&patientId=0&refappui=true";
		}
		
		String path = request.getServletPath();
        path = path.substring(1, path.lastIndexOf(".page"));
		return handlePath(path, request, response, model, httpSession);
	}
	
	@RequestMapping(value = {"/registrationapp/editPatientDemographics.page", "/registrationapp/editSection.page"}, method = {RequestMethod.GET, RequestMethod.POST})
	public String handleEditPatietPage(HttpServletRequest request, HttpServletResponse response, @RequestParam("patientId") Patient patient, Model model, HttpSession httpSession) {

		if("true".equals(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USE_PATIENT_XFORM,"false"))) {
			return "redirect:/xforms/patientReg.page?target=xformentry&formId=0&mode=edit&patientId=" + patient.getPatientId() + "&refappui=true";
		}
		
		String path = request.getServletPath();
        path = path.substring(1, path.lastIndexOf(".page"));
		return handlePath(path, request, response, model, httpSession);
	}
	
	@RequestMapping(value = "/htmlformentryui/htmlform/editHtmlFormWithStandardUi.page", method = {RequestMethod.GET, RequestMethod.POST})
	public String handleEditEncounterPage(HttpServletRequest request, HttpServletResponse response, Model model, HttpSession httpSession) {
		
		String encounterId = request.getParameter("encounterId");
		String patientId = request.getParameter("patientId");
		if (StringUtils.isNotBlank(encounterId) && StringUtils.isNotBlank(patientId)) {
			Encounter encounter = Context.getEncounterService().getEncounter(Integer.parseInt(encounterId));
			if (encounter != null && encounter.getForm() != null) {
				Xform xform = Context.getService(XformsService.class).getXform(encounter.getForm());
				if (xform != null) {
					return "redirect:/xforms/formentry/xformEntry.page?refappui=true&encounterId=" + encounterId + 
							"&patientId=" + patientId + 
							"&returnUrl=coreapps/patientdashboard/patientDashboard.page?patientId=" + patientId + 
							"&visitId=" + encounter.getVisit().getVisitId();
				}
			}
		}
		
		String path = request.getServletPath();
        path = path.substring(1, path.lastIndexOf(".page"));
		return handlePath(path, request, response, model, httpSession);
	}
	
	/**
     * @param path should be of the form "provider/optional/subdirectories/pageName"
     * @param request
     * @param response
     * @param model
     * @param httpSession
     * @return
     */
    public String handlePath(String path, HttpServletRequest request, HttpServletResponse response, Model model, HttpSession httpSession) {
        // handle the case where the url has two slashes, e.g. host/openmrs//emr/patient.page
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        int index = path.indexOf("/");
        if (index < 0) {
            throw new IllegalArgumentException("page request must have at least provider/pageName, but this does not: " + request.getRequestURI());
        }
        String providerName = path.substring(0, index);
        String pageName = path.substring(index + 1);

        Session session;
        try {
            session = sessionFactory.getSession(httpSession);
        } catch (ClassCastException ex) {
            // this means that the UI Framework module was reloaded
            sessionFactory.destroySession(httpSession);
            session = sessionFactory.getSession(httpSession);
        }
        PageRequest pageRequest = new PageRequest(providerName, pageName, request, response, session);
        try {
            String html = pageFactory.handle(pageRequest);
            model.addAttribute("html", html);
            return SHOW_HTML_VIEW;
        } catch (Redirect redirect) {
            String ret = "";
            if (!redirect.getUrl().startsWith("/"))
                ret += "/";
            ret += redirect.getUrl();
            if (ret.startsWith("/" + WebConstants.CONTEXT_PATH + "/")) {
                ret = ret.substring(WebConstants.CONTEXT_PATH.length() + 1);
            }
            return "redirect:" + ret;
        } catch (FileDownload download) {
            response.setContentType(download.getContentType());
            response.setHeader("Content-Disposition", "attachment; filename=" + download.getFilename());
            try {
                IOUtils.copy(new ByteArrayInputStream(download.getFileContent()), response.getOutputStream());
                response.flushBuffer();
            } catch (IOException ex) {
                throw new UiFrameworkException("Error trying to write file content to response", ex);
            }
            return null;
        } catch (PageAction action) {
            throw new RuntimeException("Not Yet Implemented: " + action.getClass(), action);
        } catch (RuntimeException ex) {
            // special-case if this is due to the user not being logged in
            APIAuthenticationException authEx = ExceptionUtil.findExceptionInChain(ex, APIAuthenticationException.class);
            if (authEx != null) {
                throw authEx;
            }

            // The following should go in an @ExceptionHandler. I tried this, and it isn't getting invoked for some reason.
            // And it's not worth debugging that.

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            model.addAttribute("fullStacktrace", sw.toString());

            Throwable t = ex;
            while (t.getCause() != null && !t.equals(t.getCause()))
                t = t.getCause();
            sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            model.addAttribute("rootStacktrace", sw.toString());

            return "/module/uiframework/uiError";
        }
    }

    /**
     * @param pageFactory the pageFactory to set
     */
    public void setPageFactory(PageFactory pageFactory) {
	    this.pageFactory = pageFactory;
    }
    
    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
    }
}
