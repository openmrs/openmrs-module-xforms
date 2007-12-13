package org.openmrs.module.xforms.web;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Form;
import org.openmrs.User;
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

import org.openmrs.module.xforms.*;

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
		if (Context.isAuthenticated() == false) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		String target = request.getParameter("target");
		
		String useStoredXform = Context.getAdministrationService().getGlobalProperty("xforms.useStoredXform");
		boolean createNew = false;
		if("false".equalsIgnoreCase(useStoredXform))
			createNew = true;
		
		if("xforms".equalsIgnoreCase(target))
			doXformsGet(request, response,formEntryService,xformsService,createNew);
		else{
			Integer formId = Integer.parseInt(request.getParameter("formId"));
			Form form = formEntryService.getForm(formId);

			if ("xform".equalsIgnoreCase(target)) 
				doXformGet(request, response, form,formEntryService,xformsService,createNew);
			else if ("xformentry".equals(target))		
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
			List<Xform> xforms = xformsService.getXforms();
			List<String> xmlforms = new ArrayList<String>();
			for(Xform xform : xforms){
				if(xform.getFormId() != XformConstants.PATIENT_XFORM_FORM_ID)
					xmlforms.add(getXform(request, response,formEntryService,xformsService,xform.getFormId(),createNew));
			}
			
			String xml = XformBuilder.getNewPatientXform(getActionUrl(request));
			xmlforms.add(xml);
			
			String className = Context.getAdministrationService().getGlobalProperty("xforms.xformSerializer");
			if(className == null || className.length() == 0)
				className = "org.openmrs.module.xforms.DefaultXformSerializer";
			
			response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING); //setContentType("text/html;charset=utf-8");
			ServletOutputStream stream = response.getOutputStream();

			SerializableData sr = (SerializableData)Class.forName(className).newInstance();
			sr.serialize(new DataOutputStream(stream), xmlforms);
			stream.flush();
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
				XformConstants.CONTENT_DISPOSITION_VALUE + filename);
		
		String xformXml = getXform(request, response,formEntryService,xformsService,form.getFormId(),createNew);
		response.getOutputStream().print(xformXml);
	}
	
	/**
	 * Creates a new xform.
	 * 
	 * @param request - the http request.
	 * @param formEntryService - the formentry service.
	 * @param formId - the form id.
	 * @return - the created xml form.
	 */
	private String createNewXform(HttpServletRequest request,FormEntryService formEntryService, Integer formId){
		Form form = formEntryService.getForm(formId);
		return createNewXform(request,formEntryService, form);
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
	 * Creates a new xform for an given openmrs form.
	 * 
	 * @param request - the request object.
	 * @param formEntryService - the formentry service.
	 * @param form - the form object.
	 * @return - the xml content of the xform.
	 */
	private String createNewXform(HttpServletRequest request,FormEntryService formEntryService, Form form){
		String schemaXml = formEntryService.getSchema(form);
		String templateXml = new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		return XformBuilder.getXform4mStrings(schemaXml, templateXml,getActionUrl(request));
	}
	
	/**
	 * Gets an xform for a given form id.
	 * 
	 * @param request - the http request object.
	 * @param response - the http response object.
	 * @param formEntryService - the formentry service.
	 * @param xformsService  the xforms service.
	 * @param formId - the form id.
	 * @param createNew - true if you want
	 * @return - the xml content of the xform.
	 */
	private String getXform(HttpServletRequest request, HttpServletResponse response, FormEntryService formEntryService,XformsService xformsService,Integer formId,boolean createNew){
		
		String xformXml = null;
		
		if(!createNew){
			Xform xform = xformsService.getXform(formId);
			if(xform != null)
				xformXml = xform.getXformData();
		}
		
		if(xformXml == null)
			xformXml = createNewXform(request,formEntryService, formId);
		
		return xformXml;
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
		
		User user = Context.getAuthenticatedUser();
		String enterer = "";
		if (user != null)
			enterer = user.getUserId() + "^" + user.getGivenName() + " " + user.getFamilyName();
			
		String xformXml = getXform(request, response,formEntryService,xformsService,form.getFormId(),createNew);
		Document doc = XformBuilder.getDocument(xformXml);
		
		XformBuilder.setNodeValue(doc, XformConstants.NODE_SESSION, request.getSession().getId());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_UID, FormEntryUtil.generateFormUid());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_DATE_ENTERED, FormUtil.dateToString(new java.util.Date()));
		XformBuilder.setNodeValue(doc, XformConstants.NODE_ENTERER, enterer);
		
		Integer patientId = Integer.parseInt(request.getParameter("patientId"));
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patientId.toString());
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_FAMILY_NAME, patient.getFamilyName());
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_MIDDLE_NAME, patient.getMiddleName());
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_GIVEN_NAME, patient.getGivenName());
	
		String patientParam = "?patientId="+patientId;
		String phrase = request.getParameter("phrase");
		if(phrase != null)
			patientParam += "&phrase=" + phrase;
		
		XformBuilder.setNodeAttributeValue(doc, "submission", "action", getActionUrl(request)+patientParam);
		
		XformBuilder.setPatientTableFieldValues(doc.getRootElement(), patientId, xformsService);
		
		response.setHeader("Content-Type", "application/xhtml+xml; charset=utf-8");
		response.getOutputStream().print(XformBuilder.fromDoc2String(doc));
	}
}
