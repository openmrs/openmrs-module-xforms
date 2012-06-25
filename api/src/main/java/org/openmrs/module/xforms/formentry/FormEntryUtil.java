/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms.formentry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class FormEntryUtil {

	private static Log log = LogFactory.getLog(FormEntryUtil.class);
	
	/**
	 * Cached directory where queue items are stored
	 * @see #getFormEntryQueueDir()
	 */
	private static File formEntryQueueDir = null;
	
	/**
	 * Cached directory where gp says archive items are stored
	 * @see #getFormEntryArchiveDir()
	 */
	private static String formEntryArchiveFileName = null;
		
	/**
     * Gets the directory where the user specified their queues were being stored
     * 
     * @return directory in which to store queued items
     */
    public static File getFormEntryQueueDir() {
    	
    	if (formEntryQueueDir == null) {
    		AdministrationService as = Context.getAdministrationService();
    		String folderName = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_QUEUE_DIR, FormEntryConstants.FORMENTRY_GP_QUEUE_DIR_DEFAULT);
    		formEntryQueueDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
    		if (log.isDebugEnabled())
    			log.debug("Loaded formentry queue directory from global properties: " + formEntryQueueDir.getAbsolutePath());
    	}
		
		return formEntryQueueDir;
    }
    
    /**
     * Gets the directory where the user specified their archives were being stored
     * 
     * @param optional Date to specify the folder this should possibly be sorted into 
     * @return directory in which to store archived items
     */
    public static File getFormEntryArchiveDir(Date d) {
    	// cache the global property location so we don't have to hit the db 
    	// everytime
    	if (formEntryArchiveFileName == null) {
	    	AdministrationService as = Context.getAdministrationService();
	    	formEntryArchiveFileName = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR, FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR_DEFAULT);
    	}
    	
    	// replace %Y %M %D in the folderName with the date
		String folderName = replaceVariables(formEntryArchiveFileName, d);
		
		// get the file object for this potentially new file
		File formEntryArchiveDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
		
		if (log.isDebugEnabled())
			log.debug("Loaded formentry archive directory from global properties: " + formEntryArchiveDir.getAbsolutePath());
    	
		return formEntryArchiveDir;
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

	/**
     * @deprecated this method has been moved into the OpenmrsUtil class
     * @see org.openmrs.util.OpenmrsUtil#getOutFile(File,Date,User)
     */
    public static File getOutFile(File dir, Date date, User user) {
    	return OpenmrsUtil.getOutFile(dir, date, user);
    }

	/**
     * Writes the give fileContentst to the given outFile
     * 
     * @param fileContents string to write to the file
     * @param outFile File to be overwritten with the given file contents
	 * @throws IOException on write exceptions
     */
    public static void stringToFile(String fileContents, File outFile) throws IOException {
    	FileWriter writer = new FileWriter(outFile);
    	
    	writer.write(fileContents);
    	
    	writer.close();
    }
	
	/**
	 * Helper method for the encodeUTF8String method above.
	 * 
	 * @param c
	 * @return
	 */
	private static String toEscape(char c) { //instead of this method charToHex() can be used
		int n = (int) c;
		String body = Integer.toHexString(n);
		//String body=charToHex(c);  //instead of this the above can be used
		String zeros = "000";
		return ("\\u" + zeros.substring(0, 4 - body.length()) + body);
	} //end of method

}


