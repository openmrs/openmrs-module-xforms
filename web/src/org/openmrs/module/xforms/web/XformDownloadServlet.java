package org.openmrs.module.xforms.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.Document;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformObsEdit;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.download.XformDownloadManager;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.util.LanguageUtil;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormUtil;
import org.openmrs.web.controller.user.UserFormController;

/**
 * Provides Xform download services.
 * 
 * @author Daniel Kayiwa
 * @version 1.0
 */
public class XformDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123427878377111L;

	private Log log = LogFactory.getLog(this.getClass());


	public static final String NODE_PATIENT_PATIENT_ID = "patient_id";
	public static final String NODE_PATIENT_FAMILY_NAME= "family_name";
	public static final String NODE_PATIENT_MIDDLE_NAME = "middle_name";
	public static final String NODE_PATIENT_GIVEN_NAME = "given_name";
	public static final String NODE_PATIENT_BIRTH_DATE = "birth_date";
	public static final String NODE_PATIENT_GENDER = "gender";
	public static final String NODE_ENTERER = "enterer";
	public static final String NODE_LOCATION_ID = "location_id";
	public static final String NODE_PATIENT_IDENTIFIER = "identifier";
	public static final String NODE_PATIENT_IDENTIFIER_TYPE_ID = "patient_identifier_type_id";


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

		String target = request.getParameter(XformConstants.REQUEST_PARAM_TARGET);

		try{
			if(XformConstants.REQUEST_PARAM_XFORMS.equalsIgnoreCase(target))
				new XformsServer().processConnection(new DataInputStream((InputStream)request.getInputStream()), new DataOutputStream((OutputStream)response.getOutputStream()));
			else{
				//try to authenticate users who log on inline (with the request).
				try{
					XformsUtil.authenticateInlineUser(request);
				}catch(ContextAuthenticationException e){

					log.error(e.getMessage(),e);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}

				//check for authenticated users
				if(!XformConstants.REQUEST_PARAM_XFORMS.equalsIgnoreCase(target)){
					if (!XformsUtil.isAuthenticated(request,response,null))
						return;
				}
				
				XformsService xformsService = (XformsService)Context.getService(XformsService.class);
				FormService formService = (FormService)Context.getService(FormService.class);
				
				if(XformConstants.REQUEST_PARAM_XFORMS_LIST.equals(target)){

					String xml = "<?xml version='1.0' encoding='UTF-8' ?>";
					xml += "\n<xforms>";

					List<Object[]> xformList = xformsService.getXformsList();
					if(xformList != null){
						for(Object[] item : xformList){
							xml += "\n  <xform>";
							xml += "\n    <id>" + item[0] + "</id>";
							xml += "\n    <name>" + item[1] + "</name>";
							xml += "\n  </xform>";
						}
					}

					xml += "\n</xforms>";

					response.setContentType(XformConstants.HTTP_HEADER_CONTENT_TYPE_XML); 
					response.getOutputStream().print(xml);
					return;
				}

				boolean attachment = true;
				if(XformConstants.REQUEST_PARAM_CONTENT_TYPE_VALUE_XML.equals(request.getParameter(XformConstants.REQUEST_PARAM_CONTENT_TYPE)))
					attachment = false;

				String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
				boolean createNew = false;
				if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
					createNew = true;

				//This property if for those who do not want to make two separate requests for 
				//users and xforms. This can be helpfull in say bluetooth implementations where
				//the first connection succeeds but second fails randomly hence making it better
				//to fetch everything in once single connection request.
				/*String shouldIncludeUsers = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_INCLUDE_USERS_IN_XFORMS_DOWNLOAD);
				boolean includeUsers = true;
				if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(shouldIncludeUsers))
					includeUsers = false;*/

				Integer formId = Integer.parseInt(request.getParameter(XformConstants.REQUEST_PARAM_FORM_ID));
				Form form = formService.getForm(formId);

				if (XformConstants.REQUEST_PARAM_XFORM.equalsIgnoreCase(target)) {
					if(formId == 0)
						doPatientXformGet(request, response,xformsService,formId);
					else
						doXformGet(request, response, form,formService,xformsService,createNew,attachment);
				}
				else if (XformConstants.REQUEST_PARAM_XFORM_ENTRY.equals(target)){
					if(formId == 0)
						doPatientXformEntryGet(request, response, xformsService,formId);
					else
						doXformEntryGet(request, response, form,formService,xformsService,createNew);
				}
				else if (XformConstants.REQUEST_PARAM_XSLT.equals(target))		
					doXsltGet(response, form,xformsService,createNew);
				else if (XformConstants.REQUEST_PARAM_LAYOUT.equals(target))      
					doLayoutGet(response, form,xformsService);
				else if(XformConstants.REQUEST_PARAM_XFORMREFRESH.equals(target)){
					response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);               

					String xml = null;
					if(formId == 0)
						xml = XformBuilder.getNewPatientXform();
					else
						xml = xformsService.getNewXform(formId).getXformXml();

					response.getOutputStream().print(xml);
				}
			}
		}
		catch(Exception ex){
			log.error(ex.getMessage(), ex);
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

		if(!attachment && !"true".equals(request.getParameter("excludeLayout"))){
			Xform xform = xformsService.getXform(form.getFormId());
			if(xform != null){
				String xml = xform.getLayoutXml();
				if(xml != null && xml.length() > 0)
					xformXml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + xml;

				xml = xform.getLocaleXml();
				if(xml != null && xml.length() > 0)
					xformXml += XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR + xml;
				
				xml = xform.getJavaScriptSrc();
				if(xml != null && xml.length() > 0)
					xformXml += XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR + xml;
			}
		}
		
		response.getOutputStream().print(xformXml);
	}

	private void doPatientXformGet(HttpServletRequest request, HttpServletResponse response, XformsService xformsService, Integer formId) throws ServletException, IOException{
		String xml = null;
		Xform xform = xformsService.getXform(formId);
		if(xform == null)
			xml = XformBuilder.getNewPatientXform();
		else{
			xml = xform.getXformXml();

			String layout = xform.getLayoutXml();
			if(layout != null && layout.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layout;

			String localeXml = xform.getLocaleXml();
			if(localeXml != null && localeXml.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR + localeXml;
			
			String javaScriptSrc = xform.getJavaScriptSrc();
			if(javaScriptSrc != null && javaScriptSrc.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR + javaScriptSrc;
		}

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
		response.getOutputStream().print(xml);
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
	protected void doXformEntryGet(HttpServletRequest request, HttpServletResponse response, Form form,FormService formService,XformsService xformsService, boolean createNew) throws ServletException, Exception {			
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
			Patient patient = Context.getPatientService().getPatient(patientId);

			/*XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patientId.toString());

			if(patient.getFamilyName() != null)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_FAMILY_NAME, patient.getFamilyName());

			if(patient.getMiddleName() != null)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_MIDDLE_NAME,patient.getMiddleName());

			if(patient.getGivenName() != null)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_GIVEN_NAME, patient.getGivenName());*/

			patientParam = "?"+XformConstants.REQUEST_PARAM_PATIENT_ID+"="+patientId;
			String phrase = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_SEARCH_PHRASE);
			if(phrase != null)
				patientParam += "&"+XformConstants.REQUEST_PARAM_PATIENT_SEARCH_PHRASE+"=" + phrase;

			//XformBuilder.setPatientTableFieldValues(form.getFormId(),doc.getRootElement(), patient, xformsService);
			XformBuilder.setPatientFieldValues(patient,form,doc.getRootElement(), xformsService);
		}

		//clear any previously stored form session data
		XformObsEdit.loadAndClearSessionData(request, form.getFormId());

		if(request.getParameter("encounterId") != null)
			XformObsEdit.fillObs(request,doc,Integer.parseInt(request.getParameter("encounterId")),xformXml);

		String xml = XformBuilder.fromDoc2String(doc);  

		//System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		//System.out.println(xml);

		Xform xform = xformsService.getXform(form.getFormId());

		if(xform != null){

			org.w3c.dom.Element languageTextNode = null;
			String localeXml = xform.getLocaleXml();
			if(localeXml != null && localeXml.trim().length() > 0){
				languageTextNode = LanguageUtil.getLocaleTextNode(localeXml, Context.getLocale().getLanguage());
				if(languageTextNode != null)
					xml = LanguageUtil.translateXformXml(xml,languageTextNode);
			}

			String layoutXml = xform.getLayoutXml();
			if(layoutXml != null && layoutXml.length() > 0){
				if(languageTextNode != null)
					layoutXml = LanguageUtil.translateLayoutXml(layoutXml,languageTextNode);

				xml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layoutXml;
			}
			
			String javaScriptSrc = xform.getJavaScriptSrc();
			if(javaScriptSrc != null && javaScriptSrc.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR + javaScriptSrc;
		}

		//request.getRequestDispatcher("/xform.jsp").forward(request, response);
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XHTML_XML);

		response.getOutputStream().print(xml);

		//TODO New model we need to get formdef or xform and layout xml to send client
		//formRunner.loadForm(formDef,layoutXml);
	}

	protected void doPatientXformEntryGet(HttpServletRequest request, HttpServletResponse response, XformsService xformsService, Integer formId) throws Exception {			
		String xformXml = null;
		Xform xform = xformsService.getXform(formId);
		if(xform == null)
			xformXml = XformBuilder.getNewPatientXform();
		else
			xformXml = xform.getXformXml();

		Document doc = XformBuilder.getDocument(xformXml);

		Patient patient = null;
		String patientIdParam = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_ID);
		if(patientIdParam != null){
			Integer patientId = Integer.parseInt(patientIdParam);
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_PATIENT_ID, patientId.toString());

			patient = Context.getPatientService().getPatient(patientId);
			if(patient != null){
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_FAMILY_NAME, patient.getFamilyName());
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_MIDDLE_NAME, patient.getMiddleName());
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_GIVEN_NAME, patient.getGivenName());
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_LOCATION_ID, patient.getPatientIdentifier().getLocation().getLocationId().toString());
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PATIENT_ID, patient.getPatientId().toString());
			}

			//XformBuilder.setPatientTableFieldValues(form.getFormId(),doc.getRootElement(), patientId, xformsService);
		}

		if(patient == null){
			patient = new Patient();
			UserFormController.getMiniPerson(patient, request.getParameter("addName"), request.getParameter("addGender"), request.getParameter("addBirthdate"), request.getParameter("addAge"));
		}

		String s = patient.getFamilyName();
		if(s != null && s.trim().length() > 0)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_FAMILY_NAME, s);

		s = patient.getMiddleName();
		if(s != null && s.trim().length() > 0)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_MIDDLE_NAME, s);

		s = patient.getGivenName();
		if(s != null && s.trim().length() > 0)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_GIVEN_NAME, s);

		s = patient.getGender();
		if(s != null && s.trim().length() > 0)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_GENDER, s);

		Date d = patient.getBirthdate();
		if(d != null)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_BIRTH_DATE, XformsUtil.fromDate2SubmitString(d));

		PatientIdentifier identifier = patient.getPatientIdentifier();

		if(identifier !=  null){
			XformBuilder.setNodeValue(doc,"identifier",identifier.getIdentifier());
			XformBuilder.setNodeValue(doc,"patient_identifier_type_id",identifier.getIdentifierType().getPatientIdentifierTypeId().toString());

			fillPersonAttributes(patient,doc);

			XformObsEdit.fillPatientComplexObs(request, doc, xformXml);
		}
		//else must be new patient.  

		String xml = XformBuilder.fromDoc2String(doc);

		org.w3c.dom.Element languageTextNode = null;
		String localeXml = xform.getLocaleXml();
		if(localeXml != null && localeXml.trim().length() > 0){
			languageTextNode = LanguageUtil.getLocaleTextNode(localeXml, Context.getLocale().getLanguage());
			if(languageTextNode != null)
				xml = LanguageUtil.translateXformXml(xml,languageTextNode);
		}

		if(xform != null){
			String layoutXml = xform.getLayoutXml();
			if(layoutXml != null && layoutXml.length() > 0){
				if(languageTextNode != null)
					layoutXml = LanguageUtil.translateLayoutXml(layoutXml,languageTextNode);

				xml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layoutXml;
			}
			
			String javaScriptSrc = xform.getJavaScriptSrc();
			if(javaScriptSrc != null && javaScriptSrc.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR + javaScriptSrc;
		}

		//request.getRequestDispatcher("/xform.jsp").forward(request, response);
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
		response.getOutputStream().print(xml);

		//request.getLocale().;

		//TODO New model we need to get formdef or xform and layout xml to send client
		//formRunner.loadForm(formDef,layoutXml);
	}

	/**
	 * Fills a document with person attributes for a given patient.
	 * 
	 * @param patient the patient whos person attributes to fill the document with.
	 * @param doc the document to fill with the person attributes.
	 */
	private void fillPersonAttributes(Patient patient, Document doc){
		Set<PersonAttribute> attributes = patient.getAttributes();
		if(attributes == null)
			return;

		for(PersonAttribute attribute : attributes){
			PersonAttributeType attributeType = attribute.getAttributeType();
			String value = attribute.getValue();
			if(value != null)
				XformBuilder.setNodeValue(doc, "person_attribute"+attributeType.getPersonAttributeTypeId(), value);
		}
	}

}//ROOM NO 303
//		<form id="selectFormForm" method="get" action="<%= request.getContextPath() %>/module/xforms/xformEntry.form">


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
/*protected void doXformsGet(HttpServletRequest request, HttpServletResponse response, FormService formService,XformsService xformsService,boolean createNew,boolean includeUsers) throws ServletException, IOException {

	ZOutputStream gzip = new ZOutputStream(response.getOutputStream(),JZlib.Z_BEST_COMPRESSION);
	DataOutputStream dos = new DataOutputStream(gzip);

	byte responseStatus = ResponseStatus.STATUS_ERROR;

	try
	{		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataInputStream dis = new DataInputStream(request.getInputStream());

		String name = dis.readUTF();
		String password = dis.readUTF();
		String serializer = dis.readUTF();

		byte action = dis.readByte();

		try{
			XformsUtil.authenticateInlineUser(request);
		}catch(ContextAuthenticationException e){
			log.error(e.getMessage(),e);
			responseStatus = ResponseStatus.STATUS_ACCESS_DENIED;
		}

		if(responseStatus != ResponseStatus.STATUS_ACCESS_DENIED){
			DataOutputStream dosTemp = new DataOutputStream(baos);

			response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);

			if(includeUsers)
				UserDownloadManager.downloadUsers(dosTemp);

			XformDownloadManager.downloadXforms(dosTemp);

			responseStatus = ResponseStatus.STATUS_SUCCESS;
		}

		dos.writeByte(responseStatus);

		if(responseStatus == ResponseStatus.STATUS_SUCCESS)
			dos.write(baos.toByteArray());

		dos.flush();
		gzip.finish();
	}
	catch(Exception ex){
		log.error(ex.getMessage(),ex);
		try{
			dos.writeByte(responseStatus);
			dos.flush();
			gzip.finish();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}*/