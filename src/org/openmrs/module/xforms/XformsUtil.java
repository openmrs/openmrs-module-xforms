package org.openmrs.module.xforms;

import java.io.File;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.w3c.dom.Document;

import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.module.formentry.*;

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
	
	//TODO Check to see if a method with this service already exists in the openmrs code.
	/**
	 * Converts a document to its text representation.
	 * 
	 * @param doc - the document.
	 * @return - the text representation of the document.
	 */
	public static String doc2String(Document doc){
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
     * Gets the directory where the user specified their xform archives were being stored
     * 
     * @param optional Date to specify the folder this should possibly be sorted into 
     * @return directory in which to store archived items
     */
    public static File getXformsArchiveDir(Date d) {
    	AdministrationService as = Context.getAdministrationService();
    	String xformsArchiveFileName = as.getGlobalProperty(XformConstants.XFORMS_ARCHIVE_DIR, XformConstants.XFORMS_ARCHIVE_DIR_DEFAULT);
    	
    	// replace %Y %M %D in the folderName with the date
		String folderName = FormEntryUtil.replaceVariables(xformsArchiveFileName, d);
		
		// get the file object for this potentially new file
		File xformsArchiveDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
		
		if (log.isDebugEnabled())
			log.debug("Loaded xforms archive directory from global properties: " + xformsArchiveDir.getAbsolutePath());
    	
		return xformsArchiveDir;
    }
}
