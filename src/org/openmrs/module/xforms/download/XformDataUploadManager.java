package org.openmrs.module.xforms.download;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryQueue;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.openmrs.module.xforms.SerializableData;


/**
 * Manages upload of xform data.
 * 
 * @author Daniel
 *
 */
public class XformDataUploadManager {

	 /** Logger for this class and subclasses */
    protected static final Log log = LogFactory.getLog(XformDataUploadManager.class);
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	public static void submitXforms(InputStream is, String sessionId, String actionUrl){
		
		try{
			String enterer = XformsUtil.getEnterer();
			DocumentBuilder db = dbf.newDocumentBuilder();
			SerializableData sr = getXformSerializer();
			if(sr != null){
				List<String> xforms = (List<String>)sr.deSerialize(new DataInputStream(is),getXforms(actionUrl));
				for(String xml : xforms)
					processXform(xml,sessionId,enterer);
			}
			else
				log.warn("Cant create XForms serializer");
		}
		catch(Exception e){
			log.error(e);
			e.printStackTrace();
		}
	}
		
	/**
	 * Adds an xforms data to the xforms queue.
	 * 
	 * @param xml - the xforms model.
	 */
	public static void processXform(String xml, String sessionId, String enterer){
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(IOUtils.toInputStream(xml));
			setHeaderValues(doc,sessionId,enterer);
			queueForm(XformsUtil.doc2String(doc));
		}
		catch(Exception e){
			log.error(e);
		}
	}
	
	/**
	 * Save xforms data in the xforms queue.
	 * 
	 * @param xml - the xforms model.
	 */
	private static void queueForm(String xml){
		File file = FormEntryUtil.getOutFile(XformsUtil.getXformsQueueDir(), new Date(), Context.getAuthenticatedUser());
		try{
			FileWriter writter = new FileWriter(file); //new FileWriter(pathName, false);
			writter.write(xml);
			writter.close();		
		}
		catch(Exception e){
			log.error(e);
		}
	}
	
	/**
	 * Sets the values of openmrs form header
	 * 
	 * @param doc
	 * @param request
	 * @param enterer
	 */
	private static void setHeaderValues(Document doc, String sessionId, String enterer){
		NodeList elemList = doc.getElementsByTagName(XformConstants.NODE_SESSION);
		if (elemList != null && elemList.getLength() > 0) 
			((Element)elemList.item(0)).setTextContent(sessionId);
		
		elemList = doc.getElementsByTagName(XformConstants.NODE_UID);
		if (elemList != null && elemList.getLength() > 0) 
			((Element)elemList.item(0)).setTextContent(FormEntryUtil.generateFormUid());
		
		elemList = doc.getElementsByTagName(XformConstants.NODE_DATE_ENTERED);
		if (elemList != null && elemList.getLength() > 0) 
			((Element)elemList.item(0)).setTextContent(FormUtil.dateToString(new java.util.Date()));
		
		elemList = doc.getElementsByTagName(XformConstants.NODE_ENTERER);
		if (elemList != null && elemList.getLength() > 0) 
			((Element)elemList.item(0)).setTextContent(enterer);
	}
	
	/**
	 * Gets a reference to an object implementing the xforms serialization interface.
	 * 
	 * @return
	 */
	private static SerializableData getXformSerializer(){
		try{
			String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER,XformConstants.DEFAULT_XFORM_SERIALIZER);
			if(className == null || className.length() == 0)
				className = XformConstants.DEFAULT_XFORM_SERIALIZER;
			return (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		}
		catch(Exception e){
			log.error(e);
		}
		
		return null;
	}
	
	/**
	 * Gets a map of xforms keyed by the formid
	 * 
	 * @return - the xforms map.
	 */
	private static Map<Integer,String> getXforms(String actionUrl){
		
		String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		
		Map<Integer,String> xformMap = new HashMap();
		List<Xform> xforms = xformsService.getXforms();
		boolean patientXformFound = false; String xformData;
		for(Xform xform : xforms){
			if(xform.getFormId() == XformConstants.PATIENT_XFORM_FORM_ID)
				patientXformFound= true;
			
			xformData = xform.getXformData();
			if(createNew)
				xformData = XformDownloadManager.createNewXform(formEntryService, xform.getFormId(), actionUrl);
			
			xformMap.put(xform.getFormId(), xformData);
		}
		if(!patientXformFound) //TODO Should we use the stored global property.
			xformMap.put(XformConstants.PATIENT_XFORM_FORM_ID, XformBuilder.getNewPatientXform("testing"));
		return xformMap;
	}
}
