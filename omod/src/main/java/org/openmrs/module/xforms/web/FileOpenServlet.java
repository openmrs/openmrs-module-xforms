package org.openmrs.module.xforms.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.xforms.XformConstants;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


/**
 * Handles opening of files (xforms,pictures,audio,video) as needed by the form designer.
 * 
 * @author daniel
 *
 */
public class FileOpenServlet extends HttpServlet{

	public static final long serialVersionUID = 111111111111113L;

	private final String KEY_FILE_CONTENTS = "FileContents";
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        
		response.setContentType(XformConstants.HTTP_HEADER_CONTENT_TYPE_XML); 
		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		response.getWriter().print((String)request.getSession().getAttribute(KEY_FILE_CONTENTS));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			CommonsMultipartResolver multipartResover = new CommonsMultipartResolver(/*this.getServletContext()*/);
			if(multipartResover.isMultipart(request)){
				MultipartHttpServletRequest multipartRequest = multipartResover.resolveMultipart(request);
				MultipartFile uploadedFile = multipartRequest.getFile("filecontents");
				if (uploadedFile != null && !uploadedFile.isEmpty()) 
					request.getSession().setAttribute(KEY_FILE_CONTENTS,IOUtils.toString(uploadedFile.getInputStream(),XformConstants.DEFAULT_CHARACTER_ENCODING));
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
