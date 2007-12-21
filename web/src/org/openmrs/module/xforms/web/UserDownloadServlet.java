package org.openmrs.module.xforms.web;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.SerializableData;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
import org.openmrs.module.xforms.XformUser;
import org.openmrs.module.xforms.XformConstants;


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
		
		//try to authenticate users who logon inline (with the request).
		try{
			try{
				XformsUtil.authenticateInlineUser(request);
			}catch(ContextAuthenticationException e){
				log.error(e);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
	
			//check for authenticated users
			if (!XformsUtil.isAuthenticated(request,response,null))
				return;
	
			List<XformUser> users = ((XformsService)Context.getService(XformsService.class)).getUsers();
			
			String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_SERIALIZER);
			if(className == null || className.length() == 0)
				className = XformConstants.DEFAULT_USER_SERIALIZER;
			
			response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING); 
			ServletOutputStream stream = response.getOutputStream();
	
			//SerializableData sr = (SerializableData)Class.forName(className).newInstance();
			SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
			sr.serialize(new DataOutputStream(stream), users);
			stream.flush();
		}
		catch(Exception e){
			response.getOutputStream().print("failed with: " + e.getMessage());
			log.error(e);
		}
	}
}
