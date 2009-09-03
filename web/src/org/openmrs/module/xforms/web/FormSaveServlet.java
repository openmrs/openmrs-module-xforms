package org.openmrs.module.xforms.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.xforms.XformConstants;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


/**
 * 
 * Handles saving of files (xforms) as needed by the form designer
 * 
 * @author daniel
 *
 */
public class FormSaveServlet extends HttpServlet{

	public static final long serialVersionUID = 111111111111112L;
	private String filecontents;
	private String filename;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			filecontents = null;
			CommonsMultipartResolver multipartResover = new CommonsMultipartResolver(/*this.getServletContext()*/);
			if(multipartResover.isMultipart(request)){
				MultipartHttpServletRequest multipartRequest = multipartResover.resolveMultipart(request);
				filecontents = multipartRequest.getParameter("filecontents");
				if (filecontents == null || filecontents.trim().length() == 0)
					return;
			}
			
			filename = "filename.xml";		
			if(request.getParameter("filename") != null)
				filename = request.getParameter("filename")+".xml";
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename + "\"");
		response.setContentType(XformConstants.HTTP_HEADER_CONTENT_TYPE_XML); 
		
		response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        
		response.getOutputStream().print(filecontents);
	}
}
