package org.openmrs.module.xforms.formentry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformsException;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsUtil;


/**
 * Wraps the formentry methods hence putting all formentry dependencies in one place.
 * 
 * @author Daniel
 *
 */
public class FormEntryWrapper {

	private static Log log = LogFactory.getLog(FormEntryWrapper.class);
	
	/**
	 * Cached directory where queue items are stored
	 * @see #getFormEntryQueueDir()
	 */
	private static File formEntryQueueDir = null;
	
	//max length of HL7 message control ID is 20
	private static final int FORM_UID_LENGTH = 20;
	public static final String FORMENTRY_GP_QUEUE_DIR = "formentry.queue_dir";
	public static final String FORMENTRY_GP_QUEUE_DIR_DEFAULT = "formentry/queue";

	// These variables used to be non-final and editable by runtime properties.
	// Users should not need to modify these settings.
	public static final String FORMENTRY_INFOPATH_PUBLISH_PATH = "/moduleServlet/formentry/forms/";

	// Global properties used in the formentry module
	public static final String FORMENTRY_GP_SERVER_URL = "formentry.infopath_server_url";

	/**
	 * Gets the xml template for a form.
	 * 
	 * @param form - the form reference.
	 * @return the xml template of the form.
	 */
	public static String getFormTemplate(Form form){
		return new FormXmlTemplateBuilder(form,getFormAbsoluteUrl(form)).getXmlTemplate(false);
	}
	
	/**
     * Replaces %Y in the string with the four digit year.
     * Replaces %M with the two digit month
     * Replaces %D with the two digit day
     * Replaces %w with week of the year
     * Replaces %W with week of the month
     * 
     * @param str String filename containing variables to replace with date strings 
     * @return String with variables replaced
     */
    public static String replaceVariables(String str, Date d) {
    	
    	Calendar calendar = Calendar.getInstance();
    	if (d != null)
    		calendar.setTime(d);
    	
    	int year = calendar.get(Calendar.YEAR);
    	str = str.replace("%Y", Integer.toString(year));
    	
    	int month = calendar.get(Calendar.MONTH) + 1;
    	String monthString = Integer.toString(month);
    	if (month < 10)
    		monthString = "0" + monthString;
    	str = str.replace("%M", monthString);
    	
    	int day = calendar.get(Calendar.DATE);
    	String dayString = Integer.toString(day);
    	if (day < 10)
    		dayString = "0" + dayString;
		str = str.replace("%D", dayString);
    	
    	int week = calendar.get(Calendar.WEEK_OF_YEAR);
    	String weekString = Integer.toString(week);
    	if (week < 10)
    		weekString = "0" + week;
		str = str.replace("%w", weekString);
    	
    	int weekmonth = calendar.get(Calendar.WEEK_OF_MONTH);
    	String weekmonthString = Integer.toString(weekmonth);
    	if (weekmonth < 10)
    		weekmonthString = "0" + weekmonthString;
		str = str.replace("%W", weekmonthString);
    	
    	return str;
    }
	 
	public static void createFormEntryQueue(String xml){
		FormEntryQueue formEntryQueue = new FormEntryQueue();
		formEntryQueue.setFormData(xml);
		
		User creator = Context.getAuthenticatedUser();
		if (formEntryQueue.getDateCreated() == null)
			formEntryQueue.setDateCreated(new Date());
		
		File queueDir = getFormEntryQueueDir();
		
		File outFile = OpenmrsUtil.getOutFile(queueDir, formEntryQueue.getDateCreated(), creator);
		
		// write the queue's data to the file
		FileWriter writer = null;
		try {
			writer = new FileWriter(outFile);
			writer.write(formEntryQueue.getFormData());
		}
		catch (IOException io) {
			throw new XformsException("Unable to save formentry queue", io);
		}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {
				log.debug("Error creating queu item", e);
			}
		}
	 }
	 
	 /**
	 * Gets the directory where the user specified their queues were being stored
	 * 
	 * @return directory in which to store queued items
	 */
	 public static File getFormEntryQueueDir() {
		
		if (formEntryQueueDir == null) {
			AdministrationService as = Context.getAdministrationService();
			String folderName = as.getGlobalProperty(FORMENTRY_GP_QUEUE_DIR, FORMENTRY_GP_QUEUE_DIR_DEFAULT);
			formEntryQueueDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
			if (log.isDebugEnabled())
				log.debug("Loaded formentry queue directory from global properties: " + formEntryQueueDir.getAbsolutePath());
		}
		
		return formEntryQueueDir;
	 }
	 
	 public static String generateFormUid(){
		 return OpenmrsUtil.generateUid(FORM_UID_LENGTH);
	 }
	 
	 public static String getSchema(Form form){
		 return new FormSchemaBuilder(form).getSchema(); //((FormEntryService)Context.getService(FormEntryService.class)).getSchema(form);
	 }
	 
	 public static String getFormSchemaNamespace(Form form) {
		String serverURL = Context.getAdministrationService().getGlobalProperty(FORMENTRY_GP_SERVER_URL, FORMENTRY_GP_SERVER_URL + " cannot be empty");
		String baseUrl = serverURL + FORMENTRY_INFOPATH_PUBLISH_PATH;
		return baseUrl + "schema/" + form.getFormId() + "-" + form.getBuild();
	 }
	 
	 public static String getSolutionVersion(Form form) {
		String version = form.getVersion();
		if (version == null || version.length() < 1 || version.length() > 4)
			version = "1.0.0";
		int numDots, i;
		for (numDots = 0, i = 0; (i = version.indexOf('.', i + 1)) > 0; numDots++)
			;
		if (numDots < 2)
			for (i = numDots; i < 2; i++)
				version += ".0";
		if (form.getBuild() == null || form.getBuild() < 1
				|| form.getBuild() > 9999)
			form.setBuild(1);
		version += "." + form.getBuild();
		return version;
	}
	 
	public static String getFormAbsoluteUrl(Form form) {
		// int endOfDomain = requestURL.indexOf('/', 8);
		// String baseUrl = requestURL.substring(0, (endOfDomain > 8 ?
		// endOfDomain : requestURL.length()));
		String serverURL = Context.getAdministrationService().getGlobalProperty(FORMENTRY_GP_SERVER_URL, FORMENTRY_GP_SERVER_URL + " cannot be empty");
		String baseUrl = serverURL + FORMENTRY_INFOPATH_PUBLISH_PATH;
		return baseUrl + getFormUri(form);
	}

	public static String getFormUri(Form form) {
		return FormUtil.getFormUriWithoutExtension(form) + getFormUriExtension(form);
	}
	
	public static String getFormUriExtension(Form form) {
		return ".xsn";
	}
}
