package org.openmrs.module.xforms.download;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.util.DOMUtil;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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

	public static void submitXforms(InputStream is, String sessionId) throws Exception{
		String enterer = XformsUtil.getEnterer();
		DocumentBuilder db = dbf.newDocumentBuilder();

		List<String> xforms = (List<String>)XformsUtil.invokeDeserializationMethod(is, XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER,XformConstants.DEFAULT_XFORM_SERIALIZER,getXforms());
		/*for(String xml : xforms)
            processXform(xml,sessionId,enterer);*/

		List<Document> docs  = mergeNewPatientsWithEncounters(xforms,sessionId,enterer);

		for(Document doc : docs)
			queueForm(XformsUtil.doc2String(doc));
	}

	private static List<Document> mergeNewPatientsWithEncounters(List<String> xforms, String sessionId, String enterer) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		List<Document> encounterDocs = new ArrayList<Document>();
		HashMap<String,Document> patientIdPatientDocMap = new HashMap<String,Document>();
		HashMap<String,List<Document>> patientIdEncounterDocsMap = new HashMap<String,List<Document>>();

		//Loops through the xml texts creating Document objects for each and put
		//New patient docs in a map (patientIdEncounterDocsMap) which will point to a list of encounter
		//docs collected for the new patient keyed by the patient id.
		for(String xml : xforms){
			//Create Document from xml text
			Document doc = db.parse(IOUtils.toInputStream(xml));

			setHeaderValues(doc,sessionId,enterer);
			
			//If new patient, put in the new patient map
			if(DOMUtil.isNewPatientDoc(doc)){
				String patientId = DOMUtil.getPatientFormPatientId(doc);
				patientIdEncounterDocsMap.put(patientId, new ArrayList<Document>());
				patientIdPatientDocMap.put(patientId, doc);
			}
			else
				encounterDocs.add(doc);
		}

		List<Document> processedDocs = new ArrayList<Document>();

		//Now loop through all encounter docs (encounterDocs) while creating putting them
		//in the appropriate list as for the patientIdEncounterDocsMap
		for(Document doc : encounterDocs){
			String patientId = DOMUtil.getEncounterFormPatientId(doc);
			if(patientId != null && patientIdEncounterDocsMap.containsKey(patientId))
				patientIdEncounterDocsMap.get(patientId).add(doc); //encounter form collected for a new patient
			else
				processedDocs.add(doc); //encounter form collected for an existing patient
		}

		Set<Entry<String,List<Document>>> set = patientIdEncounterDocsMap.entrySet();
		for(Entry<String,List<Document>> entry : set){
			String patientId = entry.getKey();
			List<Document> docs = entry.getValue();
			processedDocs.add(mergeDocs(patientIdPatientDocMap.get(patientId),docs));
		}			

		return processedDocs;
	}

	private static Document mergeDocs(Document patientDoc, List<Document> encounterDocs) throws Exception {
		if(encounterDocs.size() == 0)
			return patientDoc;
		
		Document mergedDoc = getNewMergeDoc();
		Element mergeRootNode = mergedDoc.getDocumentElement();

		mergeRootNode.appendChild(mergedDoc.adoptNode(patientDoc.getDocumentElement()));

		for(Document encounterDoc : encounterDocs)
			mergeRootNode.appendChild(mergedDoc.adoptNode(encounterDoc.getDocumentElement()));

		return mergedDoc;
	}

	private static Document getNewMergeDoc() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc = db.newDocument();
		Element root = (Element) doc.createElement("openmrs_data");
		doc.appendChild(root);
		return doc;
	}

	/**
	 * Adds an xforms data to the xforms queue.
	 * 
	 * @param xml - the xforms model.
	 */
	public static void processXform(String xml, String sessionId, String enterer) throws Exception{
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(IOUtils.toInputStream(xml));
		setHeaderValues(doc,sessionId,enterer);
		queueForm(XformsUtil.doc2String(doc));
	}

	/**
	 * Save xforms data in the xforms queue.
	 * 
	 * @param xml - the xforms model.
	 */
	private static void queueForm(String xml) throws Exception{
		File file = OpenmrsUtil.getOutFile(XformsUtil.getXformsQueueDir(), new Date(), Context.getAuthenticatedUser());
		FileWriter writter = new FileWriter(file); //new FileWriter(pathName, false);
		writter.write(xml);
		writter.close();		
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
			((Element)elemList.item(0)).setTextContent(FormEntryWrapper.generateFormUid());

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
	/*private static SerializableData getXformSerializer(){
		try{
			String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER,XformConstants.DEFAULT_XFORM_SERIALIZER);
			if(className == null || className.length() == 0)
				className = XformConstants.DEFAULT_XFORM_SERIALIZER;
			return (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}

		return null;
	}*/

	/**
	 * Gets a map of xforms keyed by the formid
	 * 
	 * @return - the xforms map.
	 */
	private static Map<Integer,String> getXforms(){

		String useStoredXform = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		FormService formService = (FormService)Context.getService(FormService.class);

		Map<Integer,String> xformMap = new HashMap();
		List<Xform> xforms = xformsService.getXforms();
		boolean patientXformFound = false; String xformData;
		for(Xform xform : xforms){
			if(xform.getFormId() == XformConstants.PATIENT_XFORM_FORM_ID)
				patientXformFound= true;

			xformData = xform.getXformXml();
			if(createNew)
				xformData = XformDownloadManager.createNewXform(formService, xform.getFormId());

			xformMap.put(xform.getFormId(), xformData);
		}
		if(!patientXformFound) //TODO Should we use the stored global property.
			xformMap.put(XformConstants.PATIENT_XFORM_FORM_ID, XformBuilder.getNewPatientXform());
		return xformMap;
	}
}
