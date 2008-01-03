package org.openmrs.module.xforms.web;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.formentry.*;
import org.openmrs.util.FormUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.Patient;

import org.openmrs.module.xforms.SerializableData;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsActivator;

import org.openmrs.module.xforms.*;
import org.openmrs.module.xforms.download.XformDownloadManager;

import org.kxml2.kdom.*;
import java.util.*;

/**
 * Provides Xform download services.
 * 
 * @author Daniel Kayiwa
 * @version 1.0
 */
public class XformDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123427878377111L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * This just delegates to the doGet()
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
	
	/**
	 * Sort out the multiple options for xformDownload.  This servlet does things like
	 * the xform template with data download, the xform download.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//try to authenticate users who logon inline (with the request).
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

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		String target = request.getParameter(XformConstants.REQUEST_PARAM_TARGET);
		
		String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;
		
		if(XformConstants.REQUEST_PARAM_XFORMS.equalsIgnoreCase(target))
			doXformsGet(request, response,formEntryService,xformsService,createNew);
		else{
			Integer formId = Integer.parseInt(request.getParameter(XformConstants.REQUEST_PARAM_FORM_ID));
			Form form = formEntryService.getForm(formId);

			if (XformConstants.REQUEST_PARAM_XFORM.equalsIgnoreCase(target)) 
				doXformGet(request, response, form,formEntryService,xformsService,createNew);
			else if (XformConstants.REQUEST_PARAM_XFORMENTRY.equals(target))		
				doXformEntryGet(request, response, form,formEntryService,xformsService,createNew);
		}
	}
	
	/**
	 * Handles a form which has been submitted 
	 * 
	 * @param request - the http request.
	 * @param response - the http response.
	 * @param formEntryService - the formentry service.
	 * @param xformsService - the xforms service.
	 * @param createNew - true to create a new xform or false to load an existing. 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doXformsGet(HttpServletRequest request, HttpServletResponse response, FormEntryService formEntryService,XformsService xformsService,boolean createNew) throws ServletException, IOException {
		try
		{		
			response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING); //setContentType("text/html;charset=utf-8");
			XformDownloadManager.downloadXforms(getActionUrl(request), response.getOutputStream());
		}
		catch(Exception e){
			response.getOutputStream().print("failed with: " + e.getMessage());
			log.error(e);
		}

	}
	
	/**
	 * Serve up the xml file for filling out a form 
	 * 
	 * @param request - the http request.
	 * @param response - the http response.
	 * @param form - the form
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doXformGet(HttpServletRequest request, HttpServletResponse response, Form form,FormEntryService formEntryService,XformsService xformsService,boolean createNew) throws ServletException, IOException {
		
		String filename = FormEntryUtil.getFormUriWithoutExtension(form) + XformConstants.XFORM_FILE_EXTENSION;
		
		// generate the filename if they haven't defined a URI
		if (filename == null || filename.equals(XformConstants.EMPTY_STRING))
			filename = XformConstants.STARTER_XFORM;

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, 
				XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
		
		String xformXml = XformDownloadManager.getXform(formEntryService,xformsService,form.getFormId(),createNew,getActionUrl(request));
		response.getOutputStream().print(xformXml);
	}
	
	/**
	 * Gets the url that the xform will post to after clicking the submit button.
	 * 
	 * @param request - the request object.
	 * @return - the url.
	 */
	private String getActionUrl(HttpServletRequest request){
		return request.getContextPath() + "/module/xforms/xformDataUpload.form";
	}
	
	/**
	 * Serve up the xml file for filling out a form 
	 * 
	 * @param request - the http request.
	 * @param response - the http response.
	 * @param form - the form
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doXformEntryGet(HttpServletRequest request, HttpServletResponse response, Form form,FormEntryService formEntryService,XformsService xformsService, boolean createNew) throws ServletException, IOException {			
		String xformXml = XformDownloadManager.getXform(formEntryService,xformsService,form.getFormId(),createNew, getActionUrl(request));
		Document doc = XformBuilder.getDocument(xformXml);
		
		XformBuilder.setNodeValue(doc, XformConstants.NODE_SESSION, request.getSession().getId());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_UID, FormEntryUtil.generateFormUid());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_DATE_ENTERED, FormUtil.dateToString(new java.util.Date()));
		XformBuilder.setNodeValue(doc, XformConstants.NODE_ENTERER, XformsUtil.getEnterer());
		
		Integer patientId = Integer.parseInt(request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_ID));
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patientId.toString());
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_FAMILY_NAME, patient.getFamilyName());
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_MIDDLE_NAME, patient.getMiddleName());
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_GIVEN_NAME, patient.getGivenName());
	
		String patientParam = "?"+XformConstants.REQUEST_PARAM_PATIENT_ID+"="+patientId;
		String phrase = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_SEARCH_PHRASE);
		if(phrase != null)
			patientParam += "&"+XformConstants.REQUEST_PARAM_PATIENT_SEARCH_PHRASE+"=" + phrase;
		
		XformBuilder.setNodeAttributeValue(doc, XformBuilder.NODE_SUBMISSION, XformBuilder.ATTRIBUTE_ACTION, getActionUrl(request)+patientParam);
		
		XformBuilder.setPatientTableFieldValues(doc.getRootElement(), patientId, xformsService);
		
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XHTML_XML);
		response.getOutputStream().print(XformBuilder.fromDoc2String(doc));
	}
}
