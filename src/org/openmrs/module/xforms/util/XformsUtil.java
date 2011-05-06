package org.openmrs.module.xforms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Provides utilities needed when processing xforms.
 * 
 * @author Daniel
 *
 */
public class XformsUtil {
	
	private static Log log = LogFactory.getLog(XformsUtil.class);
	
	/**
	 * Authenticates users who logon inline (with the request by appending user name and password to the url).
	 * 
	 * @param request
	 * @throws ContextAuthenticationException
	 */
	public static void authenticateInlineUser(HttpServletRequest request) throws ContextAuthenticationException{
		if (!Context.isAuthenticated()){
			String name = request.getParameter("uname");
			String pw = request.getParameter("pw");
			if(name != null & pw != null)
				Context.authenticate(name, pw);
		}
	}
	
	/**
	 * Checks if a user is authenticated. If not, takes them to the login page.
	 * 
	 * @param request  the http request.
	 * @param response  the http response.
	 * @param loginRedirect the part of the url appended to the Context Path, that the user is redireted to on successfully logging in.
	 * @return true if user is authenticated, else false.
	 * @throws Exception
	 */
	public static boolean isAuthenticated(HttpServletRequest request,HttpServletResponse response,String loginRedirect) {
		try{
			if (!Context.isAuthenticated()) {
				if(loginRedirect != null)
					request.getSession().setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR,request.getContextPath() + loginRedirect);
				
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
				response.sendRedirect(request.getContextPath() + "/logout");
				return false;
			}
		}catch(Exception e){
			log.equals(e);
			return false;
		}
		return true;
	}
	
	/**
	 * Gests the currently logged on user in a format expected by the enterer form node.
	 * 
	 * @return the string formated enterer.
	 */
	public static String getEnterer(){
		String enterer = "";
		User user = Context.getAuthenticatedUser();
		if (user != null)
			enterer = user.getUserId() + "^" + user.getGivenName() + " " + user.getFamilyName();
		
		return enterer;
	}
	
	//TODO Check to see if a method with this service already exists in the openmrs code.
	/**
	 * Converts a document to its text representation.
	 * 
	 * @param doc - the document.
	 * @return - the text representation of the document.
	 */
	public static String doc2String(Node doc){
		try{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StringWriter outStream  = new StringWriter();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outStream);
			transformer.transform(source, result);
			return outStream.getBuffer().toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Gets the default CSS for XForms.
	 * 
	 * @return the CSS text
	 */	
	private static String getDefaultStyle(){
		return "@namespace xf url(http://www.w3.org/2002/xforms); "+
		"/* Display a red background on all invalid form controls */ "+
		"*:invalid .xf-value { background-color: red; } "+
		" "+
		"/* Display a red asterisk after all required form controls */ "+
		"*:required::after { content: '*'; color: red; } "+
		" "+
		"/* Do not render non-relevant form controls */ "+
		"*:disabled { visibility: hidden; } "+
		" "+
		"/* Display an alert message when appropriate */ "+
		"*:valid   xf|alert { display: none; } "+
		"*:invalid xf|alert { display: show; } "+
		" "+
		"/* Display the selected repeat-item with a light blue color. */ "+
		".xf-repeat-index { background-color: lightblue; } "+
		" "+
		"/* Display repeat items in a table row. */ "+
		"xf|repeat .xf-repeat-item {display: table-row;} "+
		" "+
		"/* Display each select1 and input control within a repeat as a table cell, having a thin solid border and its lable aligned centrally. */ "+
		"xf|repeat xf|select1, xf|repeat xf|input{display: table-cell; border: thin; border-style: solid; text-align: center;}" +
		" " +
		"xf|input xf|label,xf|select xf|label,xf|select1 xf|label {width: 32ex; text-align: right; vertical-align: top; padding-right: 0.5em; padding-top: 1ex; padding-bottom: 1ex;} " +
		" "+
		"xf|item xf|label {width: 100%; text-align: left; padding-right: 0em; padding-bottom: 0ex; padding-top: 0ex;} " +
		" "+
		"xf|select {padding-top: 1ex; padding-bottom: 1ex;} " +
		" "+  
		"xf|input, xf|select1, xf|select, xf|submit, xf|item { display: table-row; } "+
		" "+
		"xf|input xf|label, xf|select1 xf|label, xf|select xf|label { display: table-cell; } ";
	}
	
	//<xsl:number value="position()" format="1" />   
	/**
	 * Gets the default XSLT for transforming an XForm into an XHTML document.
	 * 
	 * @return the XSLT text
	 */
	public static String getDefaultXSLT(){
		return "<?xml version='1.0' encoding='UTF-8'?> "+
		"<xsl:stylesheet version='2.0' "+
		"xmlns:xsl='http://www.w3.org/1999/XSL/Transform' "+
		"xmlns:fn='http://www.w3.org/2005/xpath-functions' "+
		"xmlns:xf='http://www.w3.org/2002/xforms'> "+
		"<xsl:output method='xml' version='1.0' encoding='UTF-8'/> "+
		"<xsl:template match='/'> "+
		" <html xmlns='http://www.w3.org/1999/xhtml' "+
		"       xmlns:xf='http://www.w3.org/2002/xforms' "+
		"       xmlns:xsd='http://www.w3.org/2001/XMLSchema' "+
		"       xmlns:xs='http://www.w3.org/2001/XMLSchema' "+
		"       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "+
		"       xmlns:ev='http://www.w3.org/2001/xml-events' "+
		"       xmlns:openmrstype='http://localhost:8080/openmrs/moduleServlet/formentry/forms/customtypes/schema/4-109' "+
		" > " +
		" <head> "+
		" 	<title> "+
		" 		<xsl:value-of select='/xf:xforms/xf:model/xf:instance/form/@name' /> "+
		"   </title> "+
		"	<style> "+ getDefaultStyle() + " </style> "+
		"   <script type='text/javascript'> <![CDATA[ "+ getJavaStriptNode() + " ]]> </script> "+
		" 	<xsl:copy-of select='/xf:xforms/xf:model' /> "+
		" </head> "+
		" <body> "+
		" 	<xsl:for-each select='/xf:xforms/*'> "+
		"   	<xsl:if test='local-name() != \"model\"'> "+
		" 			<xsl:copy-of select='.' /> "+
		"       </xsl:if> "+
		" 	</xsl:for-each> "+
		" </body> "+
		" </html> "+
		"</xsl:template> "+
		"</xsl:stylesheet> ";
	}
	
	
	/**
	 * Gets the default plain (without JavaScript and css) XSLT for transforming an XForm into an XHTML document.
	 * 
	 * @return the XSLT text
	 */
	public static String getPlainDefaultXSLT(){
		return "<?xml version='1.0' encoding='UTF-8'?> "+
		"<xsl:stylesheet version='2.0' "+
		"xmlns:xsl='http://www.w3.org/1999/XSL/Transform' "+
		"xmlns:fn='http://www.w3.org/2005/xpath-functions' "+
		"xmlns:xf='http://www.w3.org/2002/xforms'> "+
		"<xsl:output method='xml' version='1.0' encoding='UTF-8'/> "+
		"<xsl:template match='/'> "+
		" <html xmlns='http://www.w3.org/1999/xhtml' "+
		"       xmlns:xf='http://www.w3.org/2002/xforms' "+
		"       xmlns:xsd='http://www.w3.org/2001/XMLSchema' "+
		"       xmlns:xs='http://www.w3.org/2001/XMLSchema' "+
		"       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "+
		"       xmlns:ev='http://www.w3.org/2001/xml-events' "+
		"       xmlns:openmrstype='http://localhost:8080/openmrs/moduleServlet/formentry/forms/customtypes/schema/4-109' "+
		" > " +
		" <head> "+
		" 	<title> "+
		" 		<xsl:value-of select='/xf:xforms/xf:model/xf:instance/form/@name' /> "+
		"   </title> "+
		" 	<xsl:copy-of select='/xf:xforms/xf:model' /> "+
		" </head> "+
		" <body> "+
		" 	<xsl:for-each select='/xf:xforms/*'> "+
		"   	<xsl:if test='local-name() != \"model\"'> "+
		" 			<xsl:copy-of select='.' /> "+
		"       </xsl:if> "+
		" 	</xsl:for-each> "+
		" </body> "+
		" </html> "+
		"</xsl:template> "+
		"</xsl:stylesheet> ";
	}
	
	
	/**
	 * Gets the javascript needed during the xforms processsing in the browser.
	 * For now the javascript we have deals with deleting of xform repeat items.
	 * 
	 * @return the javascript script.
	 */
	private static String getJavaStriptNode(){
		
		String script = "function deleteRepeatItem(id){ " +
		"        var model = document.getElementById('"+XformBuilder.MODEL_ID+"'); " +
		"        var instance = model.getInstanceDocument('"+XformBuilder.INSTANCE_ID+"'); " +
		"        var dataElement = instance.getElementsByTagName('problem_list')[0]; " +
		"        var itemElements = dataElement.getElementsByTagName(id); " +
		"        var cnt = itemElements.length; " +
		
		"        if (cnt > 1){ " +
		"             dataElement.removeChild(itemElements[cnt-1]); " +
		"        } else { " +
		" 			var values = itemElements[0].getElementsByTagName('value'); " +
		" 			for(var i=0; i<values.length; i++) " +
		"			values[i].childNodes[0].nodeValue = null; " +
		"        } " +
		
		"        model.rebuild(); " +
		"        model.recalculate(); " +
		"        model.refresh(); " +
		"   } ";
		
		return script;
		
		/*Element scriptNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		scriptNode.setName(XformBuilder.NODE_SCRIPT);
		scriptNode.setAttribute(null, XformBuilder.ATTRIBUTE_TYPE, "text/javascript");
		scriptNode.addChild(Element.CDSECT, script);

		return scriptNode;*/
	}
	
	/**
	 * Converts an xform to an xhtml document.
	 * 
	 * @param xform the xform
	 * @param xsl the xslt
	 * @return the xhtml representation of the xform.
	 */
	public static String fromXform2Xhtml(String xform, String xsl) throws Exception{
		if(xsl == null) 
			xsl = getDefaultXSLT();
		
		return transformDocument(xform,xsl);
		
		/*StringWriter outWriter = new StringWriter();
		Source source = new StreamSource(IOUtils.toInputStream(xform,XformConstants.DEFAULT_CHARACTER_ENCODING));
		Source xslt = new StreamSource(IOUtils.toInputStream(xsl,XformConstants.DEFAULT_CHARACTER_ENCODING));
		Result result = new StreamResult(outWriter);

		System.setProperty("javax.xml.transform.TransformerFactory",
		"net.sf.saxon.TransformerFactoryImpl");

		TransformerFactory tf = TransformerFactory.newInstance();

		Transformer t = tf.newTransformer(xslt);
		t.transform(source, result);
		return outWriter.toString();*/
	}
	
	/**
	 * Converts an xform to an xhtml document.
	 * 
	 * @param xform the xform
	 * @param xsl the xslt
	 * @return the xhtml representation of the xform.
	 */
	public static String transformDocument(String xml, String xsl) throws Exception{
		if(xsl == null || xsl.trim().length() == 0) 
			return xml;
		
		StringWriter outWriter = new StringWriter();
		Source source = new StreamSource(IOUtils.toInputStream(xml,XformConstants.DEFAULT_CHARACTER_ENCODING));
		Source xslt = new StreamSource(IOUtils.toInputStream(xsl,XformConstants.DEFAULT_CHARACTER_ENCODING));
		Result result = new StreamResult(outWriter);
		
		System.setProperty("javax.xml.transform.TransformerFactory",
		"net.sf.saxon.TransformerFactoryImpl");
		
		TransformerFactory tf = TransformerFactory.newInstance();
		
		Transformer t = tf.newTransformer(xslt);
		t.transform(source, result);
		return outWriter.toString();
	}
	
	/**
	 * Gets the directory where the user specified their xform error files were being stored
	 * 
	 * @return directory in which to store xform error items
	 */
	public static File getXformsErrorDir() {
		AdministrationService as = Context.getAdministrationService();
		String folderName = as.getGlobalProperty(XformConstants.XFORMS_ERROR_DIR, XformConstants.XFORMS_ERROR_DIR_DEFAULT);
		File xformsQueueDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
		if (log.isDebugEnabled())
			log.debug("Loaded xforms error directory from global properties: " + xformsQueueDir.getAbsolutePath());
		
		return xformsQueueDir;
	}
	
	/**
	 * Gets the directory where the user specified their xform files were being stored before being processed.
	 * 
	 * @return directory in which to store xform queue items
	 */
	public static File getXformsQueueDir() {
		AdministrationService as = Context.getAdministrationService();
		String folderName = as.getGlobalProperty(XformConstants.XFORMS_QUEUE_DIR, XformConstants.XFORMS_QUEUE_DIR_DEFAULT);
		File xformsQueueDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
		if (log.isDebugEnabled())
			log.debug("Loaded xforms queue directory from global properties: " + xformsQueueDir.getAbsolutePath());
		
		return xformsQueueDir;
	}
	
	/**
	 * Gets the directory where the user specified for storage of complex obs.
	 * 
	 * @return directory in which to store xform queue items
	 */
	public static File getXformsComplexObsDir(String path) {
		AdministrationService as = Context.getAdministrationService();
		String folderName = as.getGlobalProperty(XformConstants.XFORMS_COMPLEX_OBS_DIR, XformConstants.XFORMS_COMPLEX_OBS_DIR_DEFAULT);
		File xformsComplexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName+File.separatorChar+path);
		if (log.isDebugEnabled())
			log.debug("Loaded xforms complex obs directory from global properties: " + xformsComplexObsDir.getAbsolutePath());
		
		return xformsComplexObsDir;
	}
	
	/**
	 * Gets the directory where the user specified their xform archives were being stored
	 * 
	 * @param optional Date to specify the folder this should possibly be sorted into 
	 * @return directory in which to store archived items
	 */
	public static File getXformsArchiveDir(Date d) {
		AdministrationService as = Context.getAdministrationService();
		String xformsArchiveFileName = as.getGlobalProperty(XformConstants.XFORMS_ARCHIVE_DIR, XformConstants.XFORMS_ARCHIVE_DIR_DEFAULT);
		
		// replace %Y %M %D in the folderName with the date
		String folderName = FormEntryWrapper.replaceVariables(xformsArchiveFileName, d);
		
		// get the file object for this potentially new file
		File xformsArchiveDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
		
		if (log.isDebugEnabled())
			log.debug("Loaded xforms archive directory from global properties: " + xformsArchiveDir.getAbsolutePath());
		
		return xformsArchiveDir;
	}
	
	/**
	 * Converts a string to a date.
	 * 
	 * @param date - the date string.
	 * @return - the Date object.
	 * @throws ParseException - when passed a badly formatted date.
	 */
	public static Date fromSubmitString2Date(String date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_SUBMIT_FORMAT));
		return dateFormat.parse(date);
	}
	
	public static Date fromSubmitString2DateTime(String dateTime) throws ParseException {
		String pattern = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_TIME_SUBMIT_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		
		if("yyyy-MM-dd'T'HH:mm:ssZ".equals(pattern))
			dateTime = dateTime.substring(0, 22) + dateTime.substring(23);
		
		return dateFormat.parse(dateTime);
	}
	
	public static Date fromSubmitString2Time(String time) throws ParseException {
		String pattern = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_TIME_SUBMIT_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		
		if("yyyy-MM-dd'T'HH:mm:ssZ".equals(pattern))
			time = time.substring(0, 22) + time.substring(23);
		
		return dateFormat.parse(time);
	}
	
	public static Date fromDisplayString2Date(String date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_DISPLAY_FORMAT));
		return dateFormat.parse(date);
	}
	
	public static Date fromDisplayString2DateTime(String date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_TIME_DISPLAY_FORMAT));
		return dateFormat.parse(date);
	}
	
	public static Date fromDisplayString2Time(String date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_DISPLAY_FORMAT,XformConstants.DEFAULT_TIME_DISPLAY_FORMAT));
		return dateFormat.parse(date);
	}
	
	public static String fromDate2DisplayString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_DISPLAY_FORMAT,XformConstants.DEFAULT_DATE_DISPLAY_FORMAT));
		return dateFormat.format(date);
	}
	
	public static String fromDate2SubmitString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_SUBMIT_FORMAT));
		return dateFormat.format(date);
	}
	
	public static String fromDateTime2SubmitString(Date date) {
		String pattern = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_DATE_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_DATE_TIME_SUBMIT_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		String value = dateFormat.format(date);
		
		if("yyyy-MM-dd'T'HH:mm:ssZ".equals(pattern))
			value = value.substring(0, 22) + ":" + value.substring(22);
		
		return value;
	}
	
	public static String fromTime2SubmitString(Date date) {
		String pattern = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_TIME_SUBMIT_FORMAT,XformConstants.DEFAULT_TIME_SUBMIT_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		String value = dateFormat.format(date);
		
		if("yyyy-MM-dd'T'HH:mm:ssZ".equals(pattern))
			value = value.substring(0, 22) + ":" + value.substring(22);
		
		return value;
	}
	
	/**
	 * Reads the contents of a file as a string.
	 * 
	 * @param pathName - the full path and name of the file.
	 * @return the string contents.
	 */
	public static String readFile(String pathName) throws FileNotFoundException, IOException{
		StringBuffer out = new StringBuffer();
		File file  = new File(pathName);
		FileReader reader = new FileReader(file);
		BufferedReader input = new BufferedReader(reader);
		
		int readChar = 0;
		while ((readChar = input.read()) != -1)
			out.append((char)readChar);
		
		input.close();
		reader.close();
		
		return out.toString();
	}
	
	/**
	 * Gets the url that the xform will post to after clicking the submit button.
	 * 
	 * @param request - the request object.
	 * @return - the url.
	 */
	public static String getActionUrl(HttpServletRequest request){
		return request.getContextPath() + XformConstants.XFORM_DATA_UPLOAD_RELATIVE_URL;
	}
	
	/**
	 * Gets the schema of a form.
	 * 
	 * @param form - the form reference.
	 * @return
	 */
	public static String getSchema(Form form){
		//TODO I need some implementattion here.
		return FormEntryWrapper.getSchema(form);//((FormEntryService)Context.getService(FormEntryService.class)).getSchema(form);
	}
	
	/**
	 * 
	 * Auto generated method comment
	 * 
	 * @param os
	 * @param globalPropKey
	 * @param defaultClassName
	 * @param data
	 * @throws Exception
	 */
	public static void invokeSerializationMethod(String methodName,OutputStream os, String globalPropKey, String defaultClassName, Object data) throws Exception{
		String className = Context.getAdministrationService().getGlobalProperty(globalPropKey);
		if(className == null || className.length() == 0)
			className = defaultClassName;
		
		Object obj = OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		
		if(methodName.equals("serializeForms")){
			Method method = obj.getClass().getMethod(methodName, new Class[]{OutputStream.class,Object.class,Integer.class,String.class,String.class});
			method.invoke(obj, new Object[]{os, data,new Integer(1),"",""});
		}
		else
		{
			Method method = obj.getClass().getMethod(methodName, new Class[]{OutputStream.class,Object.class});
			method.invoke(obj, new Object[]{os, data});
		}
	}
	
	
	/**
	 * 
	 * Auto generated method comment
	 * 
	 * @param is
	 * @param globalPropKey
	 * @param defaultClassName
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static Object  invokeDeserializationMethod(InputStream is, String globalPropKey, String defaultClassName, Object data) throws Exception{
		String className = Context.getAdministrationService().getGlobalProperty(globalPropKey);
		if(className == null || className.length() == 0)
			className = defaultClassName;
		
		Object obj = OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		Method method = obj.getClass().getMethod("deSerialize", new Class[]{InputStream.class,Object.class});
		
		return method.invoke(obj, new Object[]{is, data});
	}
	
	/*public static String conceptToString(Concept concept, Locale locale) {
		return concept.getConceptId() + "^" + concept.getName(locale).getName()
				+ "^" + FormConstants.HL7_LOCAL_CONCEPT;
	}*/
	
	public static Document fromString2Doc(String xml) throws Exception{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(IOUtils.toInputStream(xml,XformConstants.DEFAULT_CHARACTER_ENCODING));
	}
	
	
	public static void reportDataUploadError(Exception ex, HttpServletRequest request, HttpServletResponse response, PrintWriter writer) throws IOException {
		log.error(ex.getMessage(), ex);
		
		String message = ex.getMessage(); //"Could not process request. Click the more button for details.";
		Object msg = request.getAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_ERROR_MESSAGE);
		if(msg != null)
			message = msg.toString();
		
		response.setContentType("text/plain" /*XformConstants.HTTP_HEADER_CONTENT_TYPE_XML*/);
		response.setHeader(XformConstants.HEADER_PURCFORMS_ERROR_MESSAGE, message);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		ex.printStackTrace(writer);
	}
	
	
	/**
	 * Gets the id of the provider for a given encounter.
	 * OpenMRS from version 1.6 changed the return type of encounter.getProvider()
	 * from User to Person. As a result, this module could get problems with these versions
	 * of OpenMRS. One solution would be to develop separate versions of the module
	 * where one is for higher and another for lower versions of OpenMRS.
	 * Did not like this option very much because i did not want to maintain two different
	 * versions. So i decided to use reflection when on higher versions of OpenMRS.
	 * 
	 * @return Returns the provider id.
	 * @since 3.8.4
	 */
	public static Integer getProviderId(Encounter encounter) throws Exception {
		try{
			return encounter.getProvider().getUserId();
		}
		catch(NoSuchMethodError ex){
			Method method = encounter.getClass().getMethod("getProvider", null);
			return ((Person)method.invoke(encounter, null)).getPersonId();
		}
		/*List<User>  users = Context.getUserService().getUsersByPerson(encounter.getProvider(), false);
            // deal with multiples by tossing exceptions if you like
            if (!users.isEmpty()).
                return users.get(0).getUserId();*/
	}
	
	/**
	 * Gets the id of the person that a user represents.
	 * 
	 * @param user the user.
	 * @return the personId.
	 * @throws Exception
	 */
	public static Integer getPersonId(User user) throws Exception {
		try{
			return user.getPersonId();
		}
		catch(NoSuchMethodError ex){
			Method method = user.getClass().getMethod("getPerson", null);
			return ((Person)method.invoke(user, null)).getPersonId();
		}
	}
	
	public static boolean autoGeneratePatientIdentifier(){
		return "true".equalsIgnoreCase(Context.getAdministrationService().getGlobalProperty("xforms.autoGeneratePatientIdentifier", "false"));
	}
	
	public static PatientIdentifierType getNewPatientIdentifierType(){
		try{
			String type = Context.getAdministrationService().getGlobalProperty("xforms.new_patient_identifier_type_id", "1");
			if(StringUtils.isEmpty(type))
				return null;
			
			return Context.getPatientService().getPatientIdentifierType(Integer.parseInt(type));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public static String getNewPatientIdentifier(PatientIdentifierType patientIdentifierType){
		try{
			if(patientIdentifierType == null)
				return null;
			
			Object identifierSourceService = Context.getService(Class.forName("org.openmrs.module.idgen.service.IdentifierSourceService", true, OpenmrsClassLoader.getInstance()));
			Method method = identifierSourceService.getClass().getMethod("generateIdentifier", new Class[]{PatientIdentifierType.class, String.class});
			return (String)method.invoke(identifierSourceService, new Object[]{patientIdentifierType, "xforms module"});
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean encounterDateIncludesTime(){
		return "true".equalsIgnoreCase(Context.getAdministrationService().getGlobalProperty("xforms.encounterDateIncludesTime", "false"));
	}
	
	public static boolean isJavaRosaSaveFormat(){
		return "javarosa".equalsIgnoreCase(Context.getAdministrationService().getGlobalProperty("xforms.saveFormat","purcforms"));
	}
}
