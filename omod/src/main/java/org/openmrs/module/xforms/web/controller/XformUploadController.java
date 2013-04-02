package org.openmrs.module.xforms.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Provides XForms upload services. For instance when the form designer makes changes to an xform
 * and wants to save it back to the database, it goes through this controller.
 * 
 * @author Daniel
 */
public class XformUploadController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("isOnePointNineAndAbove", XformsUtil.isOnePointNineAndAbove());
		return map;
	}
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "Not Yet";
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object,
	                                BindException exceptions) throws Exception {
		
		//try to authenticate users who logon inline (with the request).
		XformsUtil.authenticateInlineUser(request);
		
		//check if user is authenticated
		if (!XformsUtil.isAuthenticated(request, response, "/module/xforms/xformUpload.form"))
			return null;
		
		Integer formId = Integer.parseInt(request.getParameter("formId"));
		
		String target = request.getParameter(XformConstants.REQUEST_PARAM_TARGET);
		
		if (XformConstants.REQUEST_PARAM_XFORM.equals(target)) {
			String xml = getRequestAsString(request);
			XformsService xformsService = (XformsService) Context.getService(XformsService.class);
			Xform xform = xformsService.getXform(formId);
			
			if (xform == null) {
				xform = new Xform();
				xform.setFormId(formId);
				xform.setCreator(Context.getAuthenticatedUser());
				xform.setDateCreated(new Date());
			} else {
				xform.setChangedBy(Context.getAuthenticatedUser());
				xform.setDateChanged(new Date());
			}
			
			if ("true".equals(request.getParameter("localeXml")))
				xform.setLocaleXml(xml);
			else {
				String xformXml, layoutXml = null, localeXml = null, javaScriptSrc = null, css = null;
				
				/*int pos = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
				int pos2 = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR);
				if(pos > 0){
					xformXml = xml.substring(0,pos);
					layoutXml = xml.substring(pos+XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), pos2 > 0 ? pos2 : xml.length());

					if(pos2 > 0)
						localeXml = xml.substring(pos2+XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR.length(), xml.length());
				}
				else if(pos2 > 0){
					xformXml = xml.substring(0,pos2);
					localeXml = xml.substring(pos2+XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR.length(), xml.length());
				}
				else
					xformXml = xml;*/

				int pos = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
				int pos2 = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR);
				int pos3 = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR);
				int pos4 = xml.indexOf(XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR);
				if (pos > 0) {
					xformXml = xml.substring(0, pos);
					
					int endIndex = pos2;
					if(endIndex == -1) endIndex = pos3;
					if(endIndex == -1) endIndex = pos4;
					if(endIndex == -1) endIndex = xml.length();
					
					layoutXml = xml.substring(pos + XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), endIndex);
					
					if (pos2 > 0)
						
						endIndex = pos3;
						if(endIndex == -1) endIndex = pos4;
						if(endIndex == -1) endIndex = xml.length();
					
						localeXml = xml.substring(pos2 + XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR.length(), endIndex);
					
					if (pos3 > 0)
						javaScriptSrc = xml.substring(
						    pos3 + XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(),
						    pos4 > 0 ? pos4 : xml.length());
					
					if (pos4 > 0)
						css = xml.substring(
						    pos4 + XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR.length(), xml.length());
					
				} else if (pos2 > 0) {
					xformXml = xml.substring(0, pos2);
					
					int endIndex = pos3;
					if(endIndex == -1) endIndex = pos4;
					if(endIndex == -1) endIndex = xml.length();
					
					localeXml = xml.substring(pos2 + XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR.length(),
						endIndex);
					
					if (pos3 > 0) {
						javaScriptSrc = xml.substring(
						    pos3 + XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(),
						    pos4 > 0 ? pos4 : xml.length());
					}
					
					if (pos4 > 0) {
						css = xml.substring(
						    pos4 + XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR.length(), xml.length());
					}
				} else if (pos3 > 0) {
					xformXml = xml.substring(0, pos3);
					javaScriptSrc = xml.substring(pos3 + XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(),
						pos4 > 0 ? pos4 : xml.length());
					
					if (pos4 > 0) {
						css = xml.substring(pos4 + XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR.length(),
						    xml.length());
					}
				} else if (pos4 > 0) {
					xformXml = xml.substring(0, pos4);
					css = xml.substring(pos4 + XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR.length(),
						xml.length());
				} else
					xformXml = xml;
				
				xform.setXformXml(xformXml);
				xform.setLayoutXml(layoutXml);
				xform.setLocaleXml(localeXml);
				xform.setJavaScriptSrc(javaScriptSrc);
				xform.setCss(css);
			}
			
			xformsService.saveXform(xform);
			
			return null;
		} else {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			
			if (XformConstants.REQUEST_PARAM_XSLT.equals(target)) {
				MultipartFile xsltFile = multipartRequest.getFile("xsltFile");
				if (xsltFile != null && !xsltFile.isEmpty()) {
					XformsService xformsService = (XformsService) Context.getService(XformsService.class);
					String xml = IOUtils.toString(xsltFile.getInputStream(), XformConstants.DEFAULT_CHARACTER_ENCODING);
					xformsService.saveXslt(formId, xml);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xsltUploadSuccess");
				}
			} else {
				MultipartFile xformFile = multipartRequest.getFile("xformFile");
				if (xformFile != null && !xformFile.isEmpty()) {
					XformsService xformsService = (XformsService) Context.getService(XformsService.class);
					String xml = IOUtils.toString(xformFile.getInputStream(), XformConstants.DEFAULT_CHARACTER_ENCODING);
					
					Xform xform = new Xform();
					xform.setFormId(formId);
					xform.setXformXml(xml);
					xform.setCreator(Context.getAuthenticatedUser());
					xform.setDateCreated(new Date());
					xformsService.saveXform(xform);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.xformUploadSuccess");
				}
			}
			
			return new ModelAndView(new RedirectView(request.getContextPath() + "/admin/forms/formSchemaDesign.form?formId="
			        + formId));
		}
	}
	
	private String getRequestAsString(HttpServletRequest request) throws java.io.IOException {
		/*BufferedReader requestData = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuffer stringBuffer = new StringBuffer();
		String line;
		try{
			while ((line = requestData.readLine()) != null)
				stringBuffer.append(line);
		} 
		catch (Exception e){e.printStackTrace();}

		return stringBuffer.toString();*/

		//We have commented out the above because we want to preserver new lines for formatting
		//purposes. e.g the javascript attached to xforms becomes troublesome when the javascript is lost.
		
		return IOUtils.toString(request.getInputStream(), XformConstants.DEFAULT_CHARACTER_ENCODING);
	}
}
