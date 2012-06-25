package org.openmrs.module.xforms.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.download.XformDataUploadManager;
import org.openmrs.module.xforms.util.ServletFileUploadUtil;
import org.openmrs.module.xforms.util.XformsUtil;


/**
 * Provides XForm data upload services.
 * 
 * @author Daniel
 *
 */
public class XformDataUploadServlet extends HttpServlet{

	public static final long serialVersionUID = 1234278783771156L;

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * This just delegates to the doGet()
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter writer = response.getWriter();
		
		try{
			//check if external client sending multiple filled forms.
			if(XformConstants.TRUE_TEXT_VALUE.equalsIgnoreCase(request.getParameter(XformConstants.REQUEST_PARAM_BATCH_ENTRY)))                        
				new XformsServer().processConnection(new DataInputStream((InputStream)request.getInputStream()), new DataOutputStream((OutputStream)response.getOutputStream()));
			else{
				//try to authenticate users who logon inline (with the request).
				XformsUtil.authenticateInlineUser(request);

				// check if user is authenticated
				if (XformsUtil.isAuthenticated(request,response,"/moduleServlet/xforms/xformDataUpload")){
					response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);

					request.setAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_ERROR_MESSAGE, null);
					request.setAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_PATIENT_ID, null);
					
					String xml = null;
					
					// check if request has multipart content
					if(ServletFileUpload.isMultipartContent(request)) 
						xml = ServletFileUploadUtil.getXformsInstanceData(request, response, writer);
					else{
						xml = IOUtils.toString(request.getInputStream(),XformConstants.DEFAULT_CHARACTER_ENCODING);
						
						Object id = request.getAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_PATIENT_ID);
						if(id != null)
							writer.print(id.toString());
						
						response.setStatus(HttpServletResponse.SC_OK);
						response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
						writer.println("Data submitted successfully");
					}

					XformDataUploadManager.processXform(xml,request.getSession().getId(),XformsUtil.getEnterer(),true, request);
				}
				else
					System.out.println("...........Data upload user not authenticated.................");
			}
		}
		catch(Exception e){
			XformsUtil.reportDataUploadError(e, request, response, writer);
		}
	}
}
