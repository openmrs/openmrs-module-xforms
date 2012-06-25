package org.openmrs.module.xforms.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.util.XformsUtil;


/**
 * Provides user account validation services.
 * 
 * @author Munaf Sheikh
 * @version 1.0
 */
public class UserValidatorServlet extends HttpServlet {

	public static final long serialVersionUID = 123427878377111L;

	private Log log = LogFactory.getLog(this.getClass());
	

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/plain");

		try {
			XformsUtil.authenticateInlineUser(request);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print("SUCCESS");
		} catch (ContextAuthenticationException e) {
			log.error(e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().print("FAILURE");
		}
	}
}

