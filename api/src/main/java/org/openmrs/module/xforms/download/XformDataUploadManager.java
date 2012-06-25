package org.openmrs.module.xforms.download;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
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
import org.openmrs.module.xforms.XformsQueueProcessor;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.util.DOMUtil;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manages upload of xform data regardless of what transport method (HTTP,Bluetooth, SMS, etc) was
 * used to send it.
 * 
 * @author Daniel
 */
public class XformDataUploadManager {
	
	/** Logger for this class and subclasses */
	protected static final Log log = LogFactory.getLog(XformDataUploadManager.class);
	
	/** XML Document builder factory. */
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	/** Instance of xforms processor processing data immediately without queueing. */
	private static XformsQueueProcessor processor = null;
	
	/**
	 * Reads xforms data from a stream and saves it in the database.
	 * 
	 * @param is
	 * @param sessionId
	 * @throws Exception
	 */
	public static void submitXforms(InputStream is, String sessionId, String serializerKey) throws Exception {
		//TODO Need to handles the situation where some forms get processed successfully
		//while others fail. For now, forms that fail are just put into the erros folder
		//and the client gets a process successful message. In other wards, the client
		//success is not meaning successful processing, it simply means successful delivered
		//to xforms queue where forms sit and wait for processing.
		
		if (serializerKey == null)
			serializerKey = XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER;
		
		String enterer = XformsUtil.getEnterer();
		List<String> xforms = (List<String>) XformsUtil.invokeDeserializationMethod(is, serializerKey,
		    XformConstants.DEFAULT_XFORM_SERIALIZER, getXforms());
		List<Document> docs = mergeNewPatientsWithEncounters(xforms, sessionId, enterer);
		
		for (Document doc : docs)
			queueForm(XformsUtil.doc2String(doc), false, null);
	}
	
	/**
	 * Adds an xforms data to the xforms queue.
	 * 
	 * @param xml - the xforms model.
	 */
	public static void processXform(String xml, String sessionId, String enterer, boolean propagateErrors,
	                                HttpServletRequest request) throws Exception {
		DocumentBuilder db = dbf.newDocumentBuilder();
		xml = XformsUtil.replaceConceptMaps(xml);
		Document doc = db.parse(IOUtils.toInputStream(xml, XformConstants.DEFAULT_CHARACTER_ENCODING));
		setHeaderValues(doc, sessionId, enterer);
		queueForm(XformsUtil.doc2String(doc), propagateErrors, request);
	}
	
	/**
	 * Goes though a list of forms and if those for new patients, who also have encounter forms, are
	 * found, merges them into one new document in the merged document format.
	 * 
	 * @param xforms a list of xform models.
	 * @param sessionId the user session id.
	 * @param enterer the user submitting the forms.
	 * @return a list of forms where those that deal with new patients with encounters are already
	 *         merged into one document.
	 * @throws Exception
	 */
	private static List<Document> mergeNewPatientsWithEncounters(List<String> xforms, String sessionId, String enterer)
	                                                                                                                   throws Exception {
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		//Holds a list of encounter documents.
		List<Document> encounterDocs = new ArrayList<Document>();
		
		//A map of new patientId and the corresponding new patient document.
		HashMap<String, Document> patientIdPatientDocMap = new HashMap<String, Document>();
		
		//A map of new patientId and a list of encounter documents for this new patient.
		HashMap<String, List<Document>> patientIdEncounterDocsMap = new HashMap<String, List<Document>>();
		
		//Loops through the xml texts creating Document objects for each and put
		//New patient docs in a map (patientIdEncounterDocsMap) which will point to a list of encounter
		//docs collected for the new patient keyed by the patient id.
		for (String xml : xforms) {
			//Create Document from xml text
			Document doc = db.parse(IOUtils.toInputStream(xml, XformConstants.DEFAULT_CHARACTER_ENCODING));
			
			//Set the openmrs form header values.
			setHeaderValues(doc, sessionId, enterer);
			
			//If new patient, put in the new patient map
			if (DOMUtil.isPatientDoc(doc)) {
				String patientId = DOMUtil.getPatientFormPatientId(doc);
				
				//Assuming we have not more than one new patient doc for each new patient.
				//If we have more, the are over writing them.
				patientIdEncounterDocsMap.put(patientId, new ArrayList<Document>());
				patientIdPatientDocMap.put(patientId, doc);
			} else
				encounterDocs.add(doc);
		}
		
		//This will have the final list of our documents after processing.
		List<Document> processedDocs = new ArrayList<Document>();
		
		//Now loop through all encounter docs (encounterDocs) while creating putting them
		//in the appropriate list as for the patientIdEncounterDocsMap
		for (Document doc : encounterDocs) {
			String patientId = DOMUtil.getEncounterFormPatientId(doc);
			//This works on the assumption that new patient docs have ids that
			//match those in the corresponding encounter forms which need to be merged.
			if (patientId != null && patientIdEncounterDocsMap.containsKey(patientId))
				patientIdEncounterDocsMap.get(patientId).add(doc); //encounter form collected for a new patient
			else
				processedDocs.add(doc); //encounter form collected for an existing patient
		}
		
		//Now merge the new patient documents together with their list of encounters.
		Set<Entry<String, List<Document>>> set = patientIdEncounterDocsMap.entrySet();
		for (Entry<String, List<Document>> entry : set) {
			String patientId = entry.getKey();
			List<Document> docs = entry.getValue();
			processedDocs.add(mergeDocs(patientIdPatientDocMap.get(patientId), docs));
		}
		
		return processedDocs;
	}
	
	/**
	 * Merges a new patient document with a list of documents having encounter obs entered in
	 * various forms for the new patient.
	 * 
	 * @param patientDoc the new patient document.
	 * @param encounterDocs a list of encounter documents for the new patient.
	 * @return the merged document.
	 * @throws Exception
	 */
	private static Document mergeDocs(Document patientDoc, List<Document> encounterDocs) throws Exception {
		//If no encounters, then just return the new patient document.
		if (encounterDocs.size() == 0)
			return patientDoc;
		
		//Create a new merge document with is in the format:
		//openmrs_data
		//	patient
		//	form
		// 	form
		//	an more form nodes
		//openmrs_data
		
		//Get the root node of the merge document. This node name is "openmrs_data"
		Document mergedDoc = getNewMergeDoc();
		Element mergeRootNode = mergedDoc.getDocumentElement();
		
		//Add the new patient node. This node name is "patient"
		mergeRootNode.appendChild(mergedDoc.adoptNode(patientDoc.getDocumentElement()));
		
		//Add a form node for each encounter for this new patient. The node name is "form"
		for (Document encounterDoc : encounterDocs)
			mergeRootNode.appendChild(mergedDoc.adoptNode(encounterDoc.getDocumentElement()));
		
		return mergedDoc;
	}
	
	/**
	 * Creates a new document in the format expected for new patients who also have encounters.
	 * 
	 * @return the document with only the root node
	 * @throws Exception
	 */
	private static Document getNewMergeDoc() throws Exception {
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		Document doc = db.newDocument();
		Element root = (Element) doc.createElement("openmrs_data");
		doc.appendChild(root);
		return doc;
	}
	
	/**
	 * Save xforms data in the xforms queue. As per from version 3 we are no longer queuing, we
	 * process immediately because users want to see their encounter obs without waiting.
	 * 
	 * @param xml - the xforms model.
	 */
	public static void queueForm(String xml, boolean propagateErrors, HttpServletRequest request) throws Exception {
		if (processor == null)
			processor = new XformsQueueProcessor();
		
		File file = OpenmrsUtil.getOutFile(XformsUtil.getXformsQueueDir(), new Date(), Context.getAuthenticatedUser());
		processor.processXForm(xml, file.getAbsolutePath(), propagateErrors, request);
		
		//We are not queing forms any more because the user wants to see data immediately.
		//But the xforms processor queue will still run for the sake of those who my dump
		//forms in the xforms queue.
		
		/*File file = OpenmrsUtil.getOutFile(XformsUtil.getXformsQueueDir(), new Date(), Context.getAuthenticatedUser());
		FileWriter writter = new FileWriter(file); //new FileWriter(pathName, false);
		writter.write(xml);
		writter.close();*/
	}
	
	/**
	 * Sets the values of openmrs form header
	 * 
	 * @param doc
	 * @param request
	 * @param enterer
	 */
	private static void setHeaderValues(Document doc, String sessionId, String enterer) {
		/*NodeList elemList = doc.getElementsByTagName(XformConstants.NODE_SESSION);
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
			((Element)elemList.item(0)).setTextContent(enterer);*/

		setTextContent(doc.getElementsByTagName(XformConstants.NODE_SESSION), sessionId);
		setTextContent(doc.getElementsByTagName(XformConstants.NODE_UID), FormEntryWrapper.generateFormUid());
		setTextContent(doc.getElementsByTagName(XformConstants.NODE_DATE_ENTERED),
		    FormUtil.dateToString(new java.util.Date()));
		setTextContent(doc.getElementsByTagName(XformConstants.NODE_ENTERER), enterer);
	}
	
	private static void setTextContent(NodeList elemList, String value) {
		if (elemList == null)
			return;
		
		for (int index = 0; index < elemList.getLength(); index++) {
			((Element) elemList.item(index)).setTextContent(value);
		}
	}
	
	/**
	 * Gets a map of xforms keyed by the formid
	 * 
	 * @return - the xforms map.
	 */
	private static Map<Integer, String> getXforms() throws Exception {
		
		String useStoredXform = Context.getAdministrationService().getGlobalProperty(
		    XformConstants.GLOBAL_PROP_KEY_USER_STORED_XFORMS);
		boolean createNew = false;
		if (XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(useStoredXform))
			createNew = true;
		
		XformsService xformsService = (XformsService) Context.getService(XformsService.class);
		FormService formService = (FormService) Context.getService(FormService.class);
		
		Map<Integer, String> xformMap = new HashMap<Integer, String>();
		List<Xform> xforms = xformsService.getXforms();
		boolean patientXformFound = false;
		String xformData;
		for (Xform xform : xforms) {
			if (xform.getFormId() == XformConstants.PATIENT_XFORM_FORM_ID)
				patientXformFound = true;
			
			xformData = xform.getXformXml();
			if (createNew)
				xformData = XformDownloadManager.createNewXform(formService, xform.getFormId());
			
			xformMap.put(xform.getFormId(), xformData);
		}
		if (!patientXformFound) //TODO Should we use the stored global property.
			xformMap.put(XformConstants.PATIENT_XFORM_FORM_ID, XformBuilder.getNewPatientXform());
		return xformMap;
	}
}
