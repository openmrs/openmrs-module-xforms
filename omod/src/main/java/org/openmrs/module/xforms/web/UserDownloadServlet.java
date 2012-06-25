package org.openmrs.module.xforms.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.xforms.XformsServer;


//TODO This class may need to be refactored out of the XForms module.

/**
 * Provides user download services.
 * 
 * @author Daniel Kayiwa
 * @version 1.0
 */
public class UserDownloadServlet  extends HttpServlet {

	public static final long serialVersionUID = 123427878377111L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * This just delegates to the doGet()
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try{
			new XformsServer().processConnection(new DataInputStream((InputStream)request.getInputStream()), new DataOutputStream((OutputStream)response.getOutputStream()));
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
}
