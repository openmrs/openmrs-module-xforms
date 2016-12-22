/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.util.XformsUtil;

public class XformDataDeleteServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	public static final String REQUEST_ATTRIBUTE_ID_ERROR_MESSAGE = "ERROR_MESSAGE";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter writer = response.getWriter();
		
		// check if user is authenticated
		if (!XformsUtil.isAuthenticated(request,response,"/moduleServlet/xforms/xformDataDelete")) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		try {
			request.setAttribute(REQUEST_ATTRIBUTE_ID_ERROR_MESSAGE, null);
			request.setAttribute("encounterId", null);
			
			Integer encounterId = Integer.parseInt(request.getParameter("encounterId"));
			EncounterService service = Context.getEncounterService();
			service.voidEncounter(service.getEncounter(encounterId), "XForms Module");
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception ex) {
			XformsUtil.reportDataUploadError(ex, request, response, writer);
		}
	}
}
