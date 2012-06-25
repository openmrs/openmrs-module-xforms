package org.openmrs.module.xforms.web;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformObsEdit;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Handles multimedia (picture,sound & video) requests from the data entry form.
 * 
 * @author daniel
 *
 */
public class MultimediaServlet extends HttpServlet {

	private static final long serialVersionUID = 1239820102030344234L;

	private Log log = LogFactory.getLog(this.getClass());

	private final String KEY_MULTIMEDIA_POST_DATA = "MultidemiaPostData";
	private final String KEY_MULTIMEDIA_POST_CONTENT_TYPE = "MultidemiaPostContentType";


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Setting header from hear ensures that user is not given a blank page
		//if there is not data
		response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        
        
		String formId = request.getParameter("formId");
		String xpath = request.getParameter("xpath");
		String contentType = request.getParameter("contentType");
		String name = request.getParameter("name");
		
		if("recentbinary".equals(request.getParameter("action"))){
			byte[] postData = (byte[])getSessionData(request,formId,KEY_MULTIMEDIA_POST_DATA+getFieldKey(formId,xpath));
			if(postData != null){	
				response.setContentType((String)getSessionData(request,formId,KEY_MULTIMEDIA_POST_CONTENT_TYPE+getFieldKey(formId,xpath)));
				response.getOutputStream().write(postData);
				
				setSessionData(request,formId,KEY_MULTIMEDIA_POST_CONTENT_TYPE+getFieldKey(formId,xpath),null);
				setSessionData(request,formId,KEY_MULTIMEDIA_POST_DATA+getFieldKey(formId,xpath),null);
				
				XformObsEdit.setComplexDataDirty(request,formId, xpath);
			}
			return;
		}

		try{
			if(name == null || name.trim().length() == 0)
				name = "multimedia.3gp";
			
			if(formId == null || formId.trim().length() == 0)
				return;

			if(xpath == null || xpath.trim().length() == 0)
				return;
			
			//TODO Confirm that this is the required behaviour.
			//The user could have uploaded a new file to replace the one in the database
			//but when they have not yet saved. So this is why the data in session memory
			//is taking preference to that in the database
			byte[] bytes = (byte[])getSessionData(request,formId,KEY_MULTIMEDIA_POST_DATA+getFieldKey(formId,xpath)); 
			if(bytes == null)
				bytes = XformObsEdit.getComplexData(request,formId,xpath);
			
			if(bytes != null){		
				if(contentType != null && contentType.trim().length() > 0){
					response.setContentType(contentType);

					//Send it as an attachement such that atleast firefox can also detect it
					if(contentType.contains("video") || contentType.contains("audio"))
						response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + name + "\"");
				}

				response.getOutputStream().write(bytes);
			}//This else if is to prevent a blank page if there is no data.
			else if(contentType != null && (contentType.contains("video") || contentType.contains("audio")))
				response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + name + "\"");
		}
		catch(Exception ex){
			log.error(ex.getMessage(), ex);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String formId = request.getParameter("formId");
		String xpath = request.getParameter("xpath");

		CommonsMultipartResolver multipartResover = new CommonsMultipartResolver(/*this.getServletContext()*/);
		if(multipartResover.isMultipart(request)){
			MultipartHttpServletRequest multipartRequest = multipartResover.resolveMultipart(request);
			MultipartFile uploadedFile = multipartRequest.getFile("filecontents");
			if (uploadedFile != null && !uploadedFile.isEmpty()) {
				byte[] postData = uploadedFile.getBytes();
				response.getOutputStream().print(Base64.encode(postData));

				setSessionData(request,formId,KEY_MULTIMEDIA_POST_CONTENT_TYPE+getFieldKey(formId,xpath),uploadedFile.getContentType());
				setSessionData(request,formId,KEY_MULTIMEDIA_POST_DATA+getFieldKey(formId,xpath),postData);
			}
		}
	}

	private static String getFieldKey(String formId, String xpath){
		return formId + xpath;
	}
	
	private void setSessionData(HttpServletRequest request,String formId, String key, Object data){
		HttpSession session = request.getSession();
		HashMap<String,Object> dataMap = (HashMap<String,Object>)session.getAttribute(XformObsEdit.getFormKey(formId));
		
		if(dataMap == null){
			dataMap = new HashMap<String,Object>();
			session.setAttribute(XformObsEdit.getFormKey(formId), dataMap);
		}
		
		dataMap.put(key, data);
	}
	
	private Object getSessionData(HttpServletRequest request,String formId, String key){
		HttpSession session = request.getSession();
		HashMap<String,Object> dataMap = (HashMap<String,Object>)session.getAttribute(XformObsEdit.getFormKey(formId));
		
		if(dataMap != null)
			return dataMap.get(key);

		return null;
	}
}
