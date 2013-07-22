package org.openmrs.module.xforms.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.Document;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.RelativeBuilder;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformObsEdit;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.download.XformDownloadManager;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.util.ItextParser;
import org.openmrs.module.xforms.util.LanguageUtil;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsConstants;

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
	public static final String NODE_PATIENT_BIRTH_DATE_ESTIMATED = "birth_date_estimated";
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

		PrintWriter writer = response.getWriter();
		
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
					List<Object[]> xformList = xformsService.getXformsList();

					//Check if this client wants to include the download url for each form.
					if("withurl".equalsIgnoreCase(request.getParameter("format"))){

						String url = "http://" + request.getServerName();
						int port = request.getServerPort();
						if (port != 80)
							url += ":" + Integer.toString(port);

						url += request.getContextPath() + "/moduleServlet/xforms/xformDownload?target=xform&contentType=xml&excludeLayout=true&formId=";

						url = formatXml(url);
						
						xml += "\n<forms>";

						if(xformList != null){
							for(Object[] item : xformList)
								xml += "\n  <form url='" + url + item[0] + "'>" + StringEscapeUtils.escapeXml(item[1].toString()) + "</form>";
						}

						xml += "\n</forms>";
					}
					else{

						xml += "\n<xforms>";

						if(xformList != null){
							for(Object[] item : xformList){
								xml += "\n  <xform>";
								xml += "\n    <id>" + item[0] + "</id>";
								xml += "\n    <name>" + StringEscapeUtils.escapeXml(item[1].toString()) + "</name>";
								xml += "\n  </xform>";
							}
						}

						xml += "\n</xforms>";
					}

					response.setContentType(XformConstants.HTTP_HEADER_CONTENT_TYPE_XML); 
					response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
					writer.print(xml);
					return;
				}

				boolean attachment = true;
				if(XformConstants.REQUEST_PARAM_CONTENT_TYPE_VALUE_XML.equals(request.getParameter(XformConstants.REQUEST_PARAM_CONTENT_TYPE)))
					attachment = false;

				String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
				boolean createNew = false;
				if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
					createNew = true;

				Integer formId = Integer.parseInt(request.getParameter(XformConstants.REQUEST_PARAM_FORM_ID));
				Form form = formService.getForm(formId);

				if (XformConstants.REQUEST_PARAM_XFORM.equalsIgnoreCase(target)) {
					if(formId == 0)
						doPatientXformGet(request, response,xformsService,formId, writer);
					else
						doXformGet(request, response, form,formService,xformsService,createNew,attachment, writer);
				}
				else if (XformConstants.REQUEST_PARAM_XFORM_ENTRY.equals(target)){
					if(formId == 0)
						doPatientXformEntryGet(request, response, xformsService,formId, writer);
					else
						doXformEntryGet(request, response, form,formService,xformsService,createNew, writer);
				}
				else if (XformConstants.REQUEST_PARAM_XSLT.equals(target))		
					doXsltGet(response, form,xformsService,createNew, writer);
				else if (XformConstants.REQUEST_PARAM_LAYOUT.equals(target))      
					doLayoutGet(response, form,xformsService, writer);
				else if(XformConstants.REQUEST_PARAM_XFORMREFRESH.equals(target)){
					response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);               

					String xml = null;
					if(formId == 0)
						xml = XformBuilder.getNewPatientXform();
					else
						xml = xformsService.getNewXform(formId).getXformXml();

					response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
					writer.print(xml);
				}
			}
		}
		catch(Exception ex){
			XformsUtil.reportDataUploadError(ex, request, response, writer);
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
	protected void doXformGet(HttpServletRequest request, HttpServletResponse response, Form form,FormService formService,XformsService xformsService,boolean createNew, boolean attachment, PrintWriter writer) throws Exception {

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
				
				xml = xform.getCss();
				if(xml != null && xml.length() > 0)
					xformXml += XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR + xml;
			}
		}

		String xsltKey = request.getParameter("xsltKey");
		if(xsltKey != null)
			xformXml = XformsUtil.transformDocument(xformXml, Context.getAdministrationService().getGlobalProperty(xsltKey, null));

		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		writer.print(xformXml);
	}

	private void doPatientXformGet(HttpServletRequest request, HttpServletResponse response, XformsService xformsService, Integer formId, PrintWriter writer) throws ServletException, Exception{
		String xml = null;
		Xform xform = xformsService.getXform(formId);
		if(xform == null)
			xml = XformBuilder.getNewPatientXform();
		else{
			xml = xform.getXformXml();

			if(!"true".equals(request.getParameter("excludeLayout"))){
				String layout = xform.getLayoutXml();
				if(layout != null && layout.length() > 0)
					xml += XformConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR + layout;
	
				String localeXml = xform.getLocaleXml();
				if(localeXml != null && localeXml.length() > 0)
					xml += XformConstants.PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR + localeXml;
	
				String javaScriptSrc = xform.getJavaScriptSrc();
				if(javaScriptSrc != null && javaScriptSrc.length() > 0)
					xml += XformConstants.PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR + javaScriptSrc;
				
				String css = xform.getCss();
				if(css != null && css.length() > 0)
					xml += XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR + css;
			}
		}

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		writer.print(xml);
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
	protected void doXsltGet(HttpServletResponse response, Form form,XformsService xformsService,boolean createNew, PrintWriter writer) throws ServletException, IOException {

		String filename = FormUtil.getFormUriWithoutExtension(form) + XformConstants.XSLT_FILE_EXTENSION;

		// generate the filename if they haven't defined a URI
		if (filename == null || filename.equals(XformConstants.EMPTY_STRING))
			filename = XformConstants.STARTER_XSLT;

		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);

		String xslt= XformDownloadManager.getXslt(xformsService,form.getFormId(),false);
		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		writer.print(xslt);
	}

	protected void doLayoutGet(HttpServletResponse response, Form form,XformsService xformsService, PrintWriter writer) throws ServletException, IOException {
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);

		String xslt= XformDownloadManager.getXslt(xformsService,form.getFormId(),false);
		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		writer.print(xslt);
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
	protected void doXformEntryGet(HttpServletRequest request, HttpServletResponse response, Form form,FormService formService,XformsService xformsService, boolean createNew, PrintWriter writer) throws Exception {			
		String xformXml = XformDownloadManager.getXform(formService,xformsService,form.getFormId(),createNew);

		Document doc = XformBuilder.getDocument(xformXml);

		XformBuilder.setNodeValue(doc, XformConstants.NODE_SESSION, request.getSession().getId());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_UID, FormEntryWrapper.generateFormUid());
		XformBuilder.setNodeValue(doc, XformConstants.NODE_DATE_ENTERED, FormUtil.dateToString(new java.util.Date()));
		XformBuilder.setNodeValue(doc, XformConstants.NODE_ENTERER, XformsUtil.getEnterer());

		User user = Context.getAuthenticatedUser();

		if("true".equals(Context.getAdministrationService().getGlobalProperty("xforms.setDefaultProvider", "false"))){
			//Set default provider to the logged on user of no default value is already set in the xform.
			if(user.hasRole(OpenmrsConstants.PROVIDER_ROLE)){
				String providerId = XformBuilder.getNodeValue(doc.getRootElement(), XformBuilder.NODE_ENCOUNTER_PROVIDER_ID);
				if(providerId == null || providerId.trim().length() == 0) {
					Integer id = XformsUtil.getProviderId(user);
					if (id != null) {
						XformBuilder.setNodeValue(doc, XformBuilder.NODE_ENCOUNTER_PROVIDER_ID, id.toString());
					}
				}
			}
		}

		if("true".equals(Context.getAdministrationService().getGlobalProperty("xforms.setDefaultLocation", "false"))){
			//Set default location to that of the logged on user if no default value is already set in the xform.
			String locationId = XformBuilder.getNodeValue(doc.getRootElement(), XformBuilder.NODE_ENCOUNTER_LOCATION_ID);
			if(locationId == null || locationId.trim().length() == 0){
				locationId = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
				
				//If user has no location set under their profile page, use the default implementation location.
				if(locationId == null || locationId.trim().length() == 0){
					Location location = Context.getLocationService().getDefaultLocation();
					if(location != null){
						locationId = location.getLocationId().toString();
					}
				}
				
				if(locationId != null && locationId.trim().length() > 0)
					XformBuilder.setNodeValue(doc, XformBuilder.NODE_ENCOUNTER_LOCATION_ID, locationId);
			}
		}

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
			doc = XformBuilder.setPatientFieldValues(patient, form, doc, xformsService);

			XformBuilder.setNodeAttributeValue(doc, XformBuilder.NODE_PATIENT, XformBuilder.ATTRIBUTE_UUID, patient.getUuid());
			
			RelativeBuilder.fillRelationships(patient, doc.getRootElement());
		}

		//clear any previously stored form session data
		XformObsEdit.loadAndClearSessionData(request, form.getFormId());

		if(request.getParameter("encounterId") != null)
			XformObsEdit.fillObs(request,doc,Integer.parseInt(request.getParameter("encounterId")),xformXml);

		String xml = XformBuilder.fromDoc2String(doc);

		//If the xform is in the JR format, then parse itext for the current locale.
		if(XformsUtil.isJavaRosaSaveFormat())
			xml = ItextParser.parse(xml, Context.getLocale().getLanguage());
		
		//Get the layout and JavaScript of the form, if any.
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
			
			String css = xform.getCss();
			if(css != null && css.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR + css;
		}

		//request.getRequestDispatcher("/xform.jsp").forward(request, response);
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XHTML_XML);
		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		writer.print(xml);

		//TODO New model we need to get formdef or xform and layout xml to send client
		//formRunner.loadForm(formDef,layoutXml);
	}

	protected void doPatientXformEntryGet(HttpServletRequest request, HttpServletResponse response, XformsService xformsService, Integer formId, PrintWriter writer) throws Exception {			
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
				XformBuilder.setNodeAttributeValue(doc, XformBuilder.NODE_PATIENT, XformBuilder.ATTRIBUTE_UUID, patient.getUuid());
			}


			User user = Context.getAuthenticatedUser();

			if("true".equals(Context.getAdministrationService().getGlobalProperty("xforms.setDefaultProvider", "false"))){
				//Set default provider to the logged on user of no default value is already set in the xform.
				if(user.hasRole(OpenmrsConstants.PROVIDER_ROLE)){
					String providerId = XformBuilder.getNodeValue(doc.getRootElement(), XformBuilder.NODE_ENCOUNTER_PROVIDER_ID);
					if(providerId == null || providerId.trim().length() == 0) {
						Integer id = XformsUtil.getProviderId(user);
						if (id != null) {
							XformBuilder.setNodeValue(doc, XformBuilder.NODE_ENCOUNTER_PROVIDER_ID, id.toString());
						}
					}
				}
			}

			if("true".equals(Context.getAdministrationService().getGlobalProperty("xforms.setDefaultLocation", "false"))){
				//Set default location to that of the logged on user if no default value is already set in the xform.
				String locationId = XformBuilder.getNodeValue(doc.getRootElement(), XformBuilder.NODE_LOCATION_ID);
				if(locationId == null || locationId.trim().length() == 0){
					locationId = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
					
					//If user has no location set under their profile page, use the default implementation location.
					if(locationId == null || locationId.trim().length() == 0){
						Location location = Context.getLocationService().getDefaultLocation();
						if(location != null){
							locationId = location.getLocationId().toString();
						}
					}
					
					if(locationId != null && locationId.trim().length() > 0)
						XformBuilder.setNodeValue(doc, XformBuilder.NODE_LOCATION_ID, locationId);
				}
			}

			//XformBuilder.setPatientTableFieldValues(form.getFormId(),doc.getRootElement(), patientId, xformsService);
		}

		if(patient == null){
			patient = new Patient();
			getMiniPerson(patient, request.getParameter("addName"), request.getParameter("addGender"), request.getParameter("addBirthdate"), request.getParameter("addAge"));
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
		
		//=========================
		PersonName personName = patient.getPersonName();
		if(personName != null){
			s = personName.getDegree();
			if(s != null && s.trim().length() > 0)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_DEGREE, s);
			
			s = personName.getPrefix();
			if(s != null && s.trim().length() > 0)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_PREFIX, s);
			
			s = personName.getFamilyName2();
			if(s != null && s.trim().length() > 0)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_FAMILY_NAME2, s);
			
			s = personName.getFamilyNamePrefix();
			if(s != null && s.trim().length() > 0)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_FAMILY_NAME_PREFIX, s);
			
			s = personName.getFamilyNameSuffix();
			if(s != null && s.trim().length() > 0)
				XformBuilder.setNodeValue(doc, XformBuilder.NODE_FAMILY_NAME_SUFFIX, s);
		}
		//==========================

		s = patient.getGender();
		if(s != null && s.trim().length() > 0)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_GENDER, s);

		Date d = patient.getBirthdate();
		if(d != null)
			XformBuilder.setNodeValue(doc, NODE_PATIENT_BIRTH_DATE, XformsUtil.fromDate2SubmitString(d));

		XformBuilder.setNodeValue(doc, NODE_PATIENT_BIRTH_DATE_ESTIMATED, patient.getBirthdateEstimated() ? "true" : "false");

		//clear any previously stored form session data
		XformObsEdit.loadAndClearSessionData(request, XformConstants.PATIENT_XFORM_FORM_ID);
				
		PatientIdentifier identifier = patient.getPatientIdentifier();

		if(identifier !=  null){
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_IDENTIFIER, identifier.getIdentifier());
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_IDENTIFIER_TYPE_ID, identifier.getIdentifierType().getPatientIdentifierTypeId().toString());

			fillPersonAttributes(patient,doc);
			fillPersonAddresses(patient,doc);

			XformObsEdit.fillPatientComplexObs(request, doc, xformXml);
		}
		else{
			//else must be new patient. 
			
			if(XformsUtil.autoGeneratePatientIdentifier()){
				PatientIdentifierType patientIdentifierType = XformsUtil.getNewPatientIdentifierType();
				String id = XformsUtil.getNewPatientIdentifier(patientIdentifierType);
				if(id != null){
					XformBuilder.setNodeValue(doc, XformBuilder.NODE_IDENTIFIER, id);
					XformBuilder.setNodeValue(doc, XformBuilder.NODE_IDENTIFIER_TYPE_ID, patientIdentifierType.getPatientIdentifierTypeId().toString());
				}
			}
		} 

		String xml = XformBuilder.fromDoc2String(doc);

		//If the xform is in the JR format, then parse itext for the current locale.
		if(XformsUtil.isJavaRosaSaveFormat())
			xml = ItextParser.parse(xml, Context.getLocale().getLanguage());

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
			
			String css = xform.getCss();
			if(css != null && css.length() > 0)
				xml += XformConstants.PURCFORMS_FORMDEF_CSS_SEPARATOR + css;
		}

		//request.getRequestDispatcher("/xform.jsp").forward(request, response);
		response.setHeader(XformConstants.HTTP_HEADER_CONTENT_TYPE, XformConstants.HTTP_HEADER_CONTENT_TYPE_XML);
		response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		writer.print(xml);

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


	private void fillPersonAddresses(Patient patient, Document doc){
		PersonAddress address = patient.getPersonAddress();
		if(address == null)
			return;

		if(address.getAddress1() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_ADDRESS1, address.getAddress1());

		if(address.getAddress2() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_ADDRESS2, address.getAddress2());

		if(address.getCityVillage() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_CITY_VILLAGE, address.getCityVillage());

		if(address.getStateProvince() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_STATE_PROVINCE, address.getStateProvince());

		if(address.getPostalCode() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_POSTAL_CODE, address.getPostalCode());

		if(address.getCountry() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_COUNTRY, address.getCountry());

		if(address.getLatitude() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_LATITUDE, address.getLatitude());

		if(address.getLongitude() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_LONGITUDE, address.getLongitude());

		if(address.getCountyDistrict() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_COUNTY_DISTRICT, address.getCountyDistrict());

		if(address.getNeighborhoodCell() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_NEIGHBORHOOD_CELL, address.getNeighborhoodCell());

		if(address.getRegion() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_REGION, address.getRegion());

		if(address.getSubregion() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_SUBREGION, address.getSubregion());

		if(address.getTownshipDivision() != null)
			XformBuilder.setNodeValue(doc, XformBuilder.NODE_NAME_PREFIX_PERSON_ADDRESS + XformBuilder.NODE_NAME_TOWNSHIP_DIVISION, address.getTownshipDivision());
	}

	/**
	 * Add the given name, gender, and birthdate/age to the given Person
	 * 
	 * @param <P> Should be a Patient or User object
	 * @param person
	 * @param name
	 * @param gender
	 * @param date birthdate
	 * @param age
	 */
	public <P extends Person> void getMiniPerson(P person, String name, String gender, String date, String age) {
		try {
			//Check null for name, since parsePersonName throws NullPointerExceptin if name == null
			if (StringUtils.isEmpty(name)){
				person.addName(new PersonName());
			} else {
				person.addName(Context.getPersonService().parsePersonName(name));
			}
	
			person.setGender(gender);
			Date birthdate = null;
			boolean birthdateEstimated = false;
			if (date != null && !date.equals("")) {
				try {
					// only a year was passed as parameter
					if (date.length() < 5) {
						Calendar c = new GregorianCalendar();
						c.set(Calendar.YEAR, Integer.valueOf(date));
						c.set(Calendar.MONTH, 0);
						c.set(Calendar.DATE, 1);
						birthdate = c.getTime();
						birthdateEstimated = true;
					}
					// a full birthdate was passed as a parameter
					else {
						birthdate = Context.getDateFormat().parse(date);
						birthdateEstimated = false;
					}
				}
				catch (ParseException e) {
					log.debug("Error getting date from birthdate", e);
				}
			} else if (age != null && !age.equals("")) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				Integer d = c.get(Calendar.YEAR);
				d = d - Integer.parseInt(age);
				try {
					birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + d);
					birthdateEstimated = true;
				}
				catch (ParseException e) {
					log.debug("Error getting date from age", e);
				}
			}
			if (birthdate != null)
				person.setBirthdate(birthdate);
			person.setBirthdateEstimated(birthdateEstimated);
			
		} catch (Exception ex) {
			log.debug("Error occurred while receiving MiniPerson attributes.", ex);
		}
	}

	public String formatXml(String aText){
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ){
			if (character == '<') {
				result.append("&lt;");
			}
			else if (character == '>') {
				result.append("&gt;");
			}
			else if (character == '\"') {
				result.append("&quot;");
			}
			else if (character == '\'') {
				result.append("&#039;");
			}
			else if (character == '&') {
				result.append("&amp;");
			}
			else {
				//the char is not a special one
				//add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

}
