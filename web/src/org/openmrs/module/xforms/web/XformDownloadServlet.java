package org.openmrs.module.xforms.web;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.Document;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.download.UserDownloadManager;
import org.openmrs.module.xforms.download.XformDownloadManager;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.util.FormUtil;

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
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
		FormService formService = (FormService)Context.getService(FormService.class);
		String target = request.getParameter(XformConstants.REQUEST_PARAM_TARGET);
		
		String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;
		
		//This property if for those who do not want to make two separate requests for 
		//users and xforms. This can be helpfull in say bluetooth implementations where
		//the first connection succeeds but second fails randomly hence making it better
		//to fetch everything in once single connection request.
		String shouldIncludeUsers = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_INCLUDE_USERS_IN_XFORMS_DOWNLOAD);
		boolean includeUsers = true;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(shouldIncludeUsers))
			includeUsers = false;
		
		if(XformConstants.REQUEST_PARAM_XFORMS.equalsIgnoreCase(target))
			doXformsGet(request, response,formService,xformsService,createNew,includeUsers);
		else{
			Integer formId = Integer.parseInt(request.getParameter(XformConstants.REQUEST_PARAM_FORM_ID));
			Form form = formService.getForm(formId);

			if (XformConstants.REQUEST_PARAM_XFORM.equalsIgnoreCase(target)) 
				doXformGet(request, response, form,formService,xformsService,createNew);
			else if (XformConstants.REQUEST_PARAM_XFORM_ENTRY.equals(target))		
				doXformEntryGet(request, response, form,formService,xformsService,createNew);
			else if (XformConstants.REQUEST_PARAM_XSLT.equals(target))		
				doXsltGet(response, form,xformsService,createNew);
		}
	}
	
	/**
	 * Handles a form which has been submitted 
	 * 
	 * @param request - the http request.
	 * @param response - the http response.
	 * @param formService - the form service.
	 * @param xformsService - the xforms service.
	 * @param createNew - true to create a new xform or false to load an existing. 
	 * @param includeUsers - true to include users else not.
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doXformsGet(HttpServletRequest request, HttpServletResponse response, FormService formService,XformsService xformsService,boolean createNew,boolean includeUsers) throws ServletException, IOException {
		try
		{		
			response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
			
			GZIPOutputStream gzip = new GZIPOutputStream(response.getOutputStream());
			DataOutputStream dos = new DataOutputStream(gzip);

			if(includeUsers)
				UserDownloadManager.downloadUsers(dos);
			
			XformDownloadManager.downloadXforms(XformsUtil.getActionUrl(request), dos);
			
			dos.flush();
			gzip.finish();	
			System.out.println("downloaded xforms");
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
	protected void doXformGet(HttpServletRequest request, HttpServletResponse response, Form form,FormService formService,XformsService xformsService,boolean createNew) throws ServletException, IOException {
		
		String filename = FormUtil.getFormUriWithoutExtension(form) + XformConstants.XFORM_FILE_EXTENSION;
		
		// generate the filename if they haven't defined a URI
		if (filename == null || filename.equals(XformConstants.EMPTY_STRING))
			filename = XformConstants.STARTER_XFORM;

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, 
				XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
		
		String xformXml = XformDownloadManager.getXform(formService,xformsService,form.getFormId(),createNew,XformsUtil.getActionUrl(request));
		//xformXml = XformsUtil.fromXform2Xhtml(xformXml, null);
		response.getOutputStream().print(xformXml);
	}
	
	/**
	 * Serve up the xslt for transforming an xform to an xhtml document.
	 * 
	 * @param request - the http request.
	 * @param response - the http response.
	 * @param form - the form
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doXsltGet(HttpServletResponse response, Form form,XformsService xformsService,boolean createNew) throws ServletException, IOException {
		
		String filename = FormUtil.getFormUriWithoutExtension(form) + XformConstants.XSLT_FILE_EXTENSION;
		
		// generate the filename if they haven't defined a URI
		if (filename == null || filename.equals(XformConstants.EMPTY_STRING))
			filename = XformConstants.STARTER_XSLT;

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, 
				XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
		
		String xslt= XformDownloadManager.getXslt(xformsService,form.getFormId(),false);
		response.getOutputStream().print(xslt);
	}
	
	/**
	 * Serve up the xhtml file for filling out a form 
	 * 
	 * @param request - the http request.
	 * @param response - the http response.
	 * @param form - the form
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doXformEntryGet(HttpServletRequest request, HttpServletResponse response, Form form,FormService formService,XformsService xformsService, boolean createNew) throws ServletException, IOException {			
		String xformXml = XformDownloadManager.getXform(formService,xformsService,form.getFormId(),createNew, XformsUtil.getActionUrl(request));
		
		try{
			xformXml = XformsUtil.fromXform2Xhtml(xformXml, xformsService.getXslt(form.getFormId()));
		}catch(Exception e){
			log.error(e.getMessage(), e);
			response.getOutputStream().print(e.getMessage()); //possibly user xslt has errors.
			return;
		}
		
		Document doc = XformBuilder.getDocument(xformXml);
		
		XformBuilder.setNodeValue(doc, XformConstants.NODE_SESSION, request.getSession().getId());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_UID, FormEntryWrapper.generateFormUid());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_DATE_ENTERED, FormUtil.dateToString(new java.util.Date()));
		XformBuilder.setNodeValue(doc, XformConstants.NODE_ENTERER, XformsUtil.getEnterer());
		
		String patientParam = "";
		String patientIdParam = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_ID);
		if(patientIdParam != null){
			Integer patientId = Integer.parseInt(patientIdParam);
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patientId.toString());
			
			Patient patient = Context.getPatientService().getPatient(patientId);
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_FAMILY_NAME, patient.getFamilyName());
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_MIDDLE_NAME, patient.getMiddleName());
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_GIVEN_NAME, patient.getGivenName());
		
			patientParam = "?"+XformConstants.REQUEST_PARAM_PATIENT_ID+"="+patientId;
			String phrase = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_SEARCH_PHRASE);
			if(phrase != null)
				patientParam += "&"+XformConstants.REQUEST_PARAM_PATIENT_SEARCH_PHRASE+"=" + phrase;
					
			XformBuilder.setPatientTableFieldValues(form.getFormId(),doc.getRootElement(), patientId, xformsService);
		}
		//URL url = new URL(""); url.openStream()
		XformBuilder.setNodeAttributeValue(doc, XformBuilder.NODE_SUBMISSION, XformBuilder.ATTRIBUTE_ACTION, XformsUtil.getActionUrl(request)+patientParam);

		//request.getRequestDispatcher("/xform.jsp").forward(request, response);
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XHTML_XML);
		response.getOutputStream().print(XformBuilder.fromDoc2String(doc));
	}
}
//		<form id="selectFormForm" method="get" action="<%= request.getContextPath() %>/module/xforms/xformEntry.form">