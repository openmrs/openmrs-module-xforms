package org.openmrs.module.xforms.web;

import java.io.DataOutputStream;
import java.io.IOException;

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
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.download.UserDownloadManager;
import org.openmrs.module.xforms.download.XformDownloadManager;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.util.FormUtil;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

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
			log.error(e.getMessage(),e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		//check for authenticated users
		if (!XformsUtil.isAuthenticated(request,response,null))
			return;

        boolean attachment = true;
        if(XformConstants.REQUEST_PARAM_CONTENT_TYPE_VALUE_XML.equals(request.getParameter(XformConstants.REQUEST_PARAM_CONTENT_TYPE)))
            attachment = false;
        
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
				doXformGet(request, response, form,formService,xformsService,createNew,attachment);
			else if (XformConstants.REQUEST_PARAM_XFORM_ENTRY.equals(target))		
				doXformEntryGet(request, response, form,formService,xformsService,createNew);
			else if (XformConstants.REQUEST_PARAM_XSLT.equals(target))		
				doXsltGet(response, form,xformsService,createNew);
            else if (XformConstants.REQUEST_PARAM_LAYOUT.equals(target))      
                doLayoutGet(response, form,xformsService);
            else if(XformConstants.REQUEST_PARAM_XFORMREFRESH.equals(target)){
                response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);               
                response.getOutputStream().print(xformsService.getNewXform(formId).getXformXml());
            }
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
			
            ZOutputStream gzip = new ZOutputStream(response.getOutputStream(),JZlib.Z_BEST_COMPRESSION);
			DataOutputStream dos = new DataOutputStream(gzip);

			if(includeUsers)
				UserDownloadManager.downloadUsers(dos);
			
			XformDownloadManager.downloadXforms(dos);
             
			dos.flush();
			gzip.finish();	
 		}
		catch(Exception e){
			log.error(e.getMessage(),e);
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
	protected void doXformGet(HttpServletRequest request, HttpServletResponse response, Form form,FormService formService,XformsService xformsService,boolean createNew, boolean attachment) throws ServletException, IOException {
		
		String filename = FormUtil.getFormUriWithoutExtension(form) + XformConstants.XFORM_FILE_EXTENSION;
		
		// generate the filename if they haven't defined a URI
		if (filename == null || filename.equals(XformConstants.EMPTY_STRING))
			filename = XformConstants.STARTER_XFORM;

        if(attachment)
            response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
        else
            response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
            
		String xformXml = XformDownloadManager.getXform(formService,xformsService,form.getFormId(),createNew);
		//xformXml = XformsUtil.fromXform2Xhtml(xformXml, null);
        
        if(!attachment){
            Xform xform = xformsService.getXform(form.getFormId());
            if(xform != null){
                String xml = xform.getLayoutXml();
                if(xml != null && xml.length() > 0)
                    xformXml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + xml;
            }
        }
        
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

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
		
		String xslt= XformDownloadManager.getXslt(xformsService,form.getFormId(),false);
		response.getOutputStream().print(xslt);
	}
    
	protected void doLayoutGet(HttpServletResponse response, Form form,XformsService xformsService) throws ServletException, IOException {
        response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
        
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
		String xformXml = XformDownloadManager.getXform(formService,xformsService,form.getFormId(),createNew);
		
		/*try{
			xformXml = XformsUtil.fromXform2Xhtml(xformXml, xformsService.getXslt(form.getFormId()));
		}catch(Exception e){
			log.error(e.getMessage(), e);
			response.getOutputStream().print(e.getMessage()); //possibly user xslt has errors.
			return;
		}*/
		
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

		//request.getRequestDispatcher("/xform.jsp").forward(request, response);
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XHTML_XML);
		
        String xml = XformBuilder.fromDoc2String(doc);        
        Xform xform = xformsService.getXform(form.getFormId());
        String layoutXml = xform.getLayoutXml();
        if(layoutXml != null && layoutXml.length() > 0)
            xml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layoutXml;
        
        response.getOutputStream().print(xml);
        
        //TODO New model we need to get formdef or xform and layout xml to send client
        //formRunner.loadForm(formDef,layoutXml);
	}
}
//		<form id="selectFormForm" method="get" action="<%= request.getContextPath() %>/module/xforms/xformEntry.form">