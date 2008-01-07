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
	
	
	//First loop through all forms and 
	//1. create new patients for new patients forms
	//2. put the new patients in a hashtable keyed by the submitted client assigned negative patient id with the server assigned patient id as value
	//3. store forms with new patient ids (negative ones) in a list.
	//4. submit forms for existing patients normally as before.
	//5. At the end of this first loop, loop through the new patient forms as in (3) and 
	//   replace the negative patient ids with the server assigned ones as by the hashtable in (2)
	//   and then submit them to the server.
	public static void submitXforms(InputStream is, String sessionId){
		
		try{
			HashMap<String,Integer> patientids = new HashMap<String,Integer>();
			List<String> newPatientForms = new ArrayList<String>();
			FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
			
			String enterer = XformsUtil.getEnterer();
			User creator = getCreator();
			DocumentBuilder db = dbf.newDocumentBuilder();
			SerializableData sr = getXformSerializer();
			if(sr != null){
				List<String> xforms = (List<String>)sr.deSerialize(new DataInputStream(is),getXforms());
				for(String xml : xforms)
					submitXForm(db ,xml, sessionId, enterer,creator,formEntryService,patientids,newPatientForms);
				
				for(String xml : newPatientForms)
					submitXForm(db ,xml, sessionId, enterer,creator,formEntryService,patientids,null);
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
	 * Submits a single xform from an incoming http request.
	 * 
	 * @param request - the request object.
	 */
	public static void submitXform(String xml,String sessionId){
		
		try{
			FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
			DocumentBuilder db = dbf.newDocumentBuilder();
			submitXForm(db ,xml, sessionId, XformsUtil.getEnterer(),getCreator(),formEntryService,null,null);
			
		} catch (Exception e) {
			log.error("Error parsing form data", e);
		}
	}
	
	private static void submitXForm(DocumentBuilder db ,String xml, String sessionId, String enterer,User creator, FormEntryService formEntryService,HashMap<String,Integer> patientids,List<String> newPatientForms){
		String xmlOriginal = xml;
		String pathName = saveFormInQueue(xml); //Queue the form just incase we get an error during processing.
		try{	
			Document doc = db.parse(IOUtils.toInputStream(xml));
			
			if(patientids != null && newPatientForms != null){
				//If new patient form, create patient to get their correponding patient_id
				if(isNewPatientDoc(doc)){
					if(saveNewPatient(doc,creator,patientids))
						saveFormInArchive(xml,pathName);
					else
						saveFormInError(xml,pathName);
					return;
				}

				//Form collected for a new, will submit after creating the correspending
				//patient during the second pass in the loop.
				if(isNewPatientFormDoc(doc)){  
					newPatientForms.add(xml);
					return;
				}
			}
			
			//If true, this must be second pass after having got the new patient ids
			if(patientids != null && newPatientForms == null){
				if(!setNewPatientId(doc,patientids)){
					saveFormInError(xml,pathName); //may be new patient creation failed as new patient id is not found.
					log.warn("new patient id not found");
					return;
				}
			}
			
			setHeaderValues(doc,sessionId,enterer);
			setMultipleSelectValues(doc.getDocumentElement());
			
			xml = XformsUtil.doc2String(doc);
			FormEntryQueue formEntryQueue = new FormEntryQueue();
			formEntryQueue.setFormData(xml);
			formEntryService.createFormEntryQueue(formEntryQueue);
			
			saveFormInArchive(xmlOriginal,pathName);
		} catch (Exception e) {
			log.error(e);
			saveFormInError(xmlOriginal,pathName);
		}
	}
	
	private static SerializableData getXformSerializer(){
		try{
			String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER);
			if(className == null || className.length() == 0)
				className = XformConstants.DEFAULT_XFORM_SERIALIZER;
			return (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
			//return (SerializableData)Class.forName(className).newInstance();
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
	private static Map<Integer,String> getXforms(){
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);

		Map<Integer,String> xformMap = new HashMap();
		List<Xform> xforms = xformsService.getXforms();
		boolean patientXformFound = false;
		for(Xform xform : xforms){
			if(xform.getFormId() == XformConstants.PATIENT_XFORM_FORM_ID)
				patientXformFound= true;
			xformMap.put(xform.getFormId(), xform.getXformData());
		}
		if(!patientXformFound) //TODO Should we use the stored global property.
			xformMap.put(XformConstants.PATIENT_XFORM_FORM_ID, XformBuilder.getNewPatientXform("testing"));
		return xformMap;
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
	 * Checks if a document is a create new patient one.
	 * One which collected bio data about a new patient.
	 * 
	 * @param doc - the document.
	 * @return - true if so, else false.
	 */
	private static boolean isNewPatientDoc(Document doc){
		NodeList elemList = doc.getElementsByTagName(XformBuilder.NODE_PATIENT);
		if (!(elemList != null && elemList.getLength() > 0)) 
			return false;

		Element element = ((Element)elemList.item(0));
		String id = element.getAttribute(XformBuilder.ATTRIBUTE_ID);
		if(!String.valueOf(XformConstants.PATIENT_XFORM_FORM_ID).equals(id))
			return false;
		
		return true;
	}
	
	/**
	 * Check if a document represents a form collected for a new patient(One who
	 * is not yet in the openmrs server).
	 * 
	 * @param doc - the document.
	 * @return - true if so, else false.
	 */
	private static boolean isNewPatientFormDoc(Document doc){
		NodeList elemList = doc.getElementsByTagName(XformBuilder.NODE_PATIENT_PATIENT_ID);
		if (!(elemList != null && elemList.getLength() > 0)) 
			return false;

		Element element = ((Element)elemList.item(0));
		String val = element.getAttribute(XformBuilder.ATTRIBUTE_OPENMRS_TABLE);
		if(!XformBuilder.NODE_PATIENT.equals(val))
			return false;
		
		val = element.getAttribute(XformBuilder.ATTRIBUTE_OPENMRS_ATTRIBUTE);
		if(!XformBuilder.NODE_PATIENT_ID.equals(val))
			return false;
		
		val = element.getTextContent();
		int id = Integer.parseInt(val);
		if(id > 0)
			return false;
		
		return true;
	}
	
	/**
	 * Gets a random file name.
	 * 
	 * @return - the file name.
	 */
	private static String getRandomFileName(){
		return new Date().toString().replace(':', '_');
	}
	
	/**
	 * Achives a submitted form after processing.
	 * 
	 * @param xml - the form data.
	 * @param errorQueue - set to true to put in error queue, else set to false
	 */
	private static String saveForm(String xml,File folder,String queuePathName){
		String pathName = folder.getAbsolutePath()+File.separatorChar+getRandomFileName()+XformConstants.XML_FILE_EXTENSION;
		try{
			FileWriter writter = new FileWriter(pathName, false);
			writter.write(xml);
			writter.close();
			
			if(queuePathName != null){
				try{
					File file = new File(queuePathName);
					if(!file.delete())
						file.deleteOnExit();
				}catch(Exception e){
					log.error(e);
				}
			}
		}
		catch(Exception e){
			log.error(e);
		}
		
		return pathName;
	}
	
	/**
	 * Saves an xform in the xforms archive.
	 * 
	 * @param xml - the xml of the xform.
	 * @param queuePathName - the queue full path and file name of this xform.
	 * @return - the archive full path and file name.
	 */
	private static String saveFormInArchive(String xml,String queuePathName){
		return saveForm(xml,XformsUtil.getXformsArchiveDir(new Date()),queuePathName);
	}
	
	/**
	 * Saves an xform in the errors folder.
	 * 
	 * @param xml - the xml of the xform.
	 * @param queuePathName - the queue full path and file name of this xform.
	 * @return - the error full path and file name.
	 */
	private static String saveFormInError(String xml,String queuePathName){
		return saveForm(xml,XformsUtil.getXformsErrorDir(),queuePathName);
	}
	
	/**
	 * Save an xform in the xform queue.
	 * 
	 * @param xml - the xml of the xform.
	 * @return - the queue full path and file name.
	 */
	private static String saveFormInQueue(String xml){
		return saveForm(xml,XformsUtil.getXformsQueueDir(),null);
	}
	
	private static boolean setNewPatientId(Document doc, HashMap<String,Integer> patientIds){
		try{
			NodeList elemList = doc.getElementsByTagName(XformBuilder.NODE_PATIENT_PATIENT_ID);
			if (!(elemList != null && elemList.getLength() > 0)) 
				return false;
	
			Element element = (Element)elemList.item(0);
			String val  = element.getTextContent();
			if(!(Integer.parseInt(val) < 0))
				return false; //For now, new patient ids must be negatives.
			
			Integer id = patientIds.get(val);
			if(id == null){
				log.error("No patient created with an original id of:"+val);
				return false; //Patient id not found, may be there was a failure at creation.
			}
			
			element.setTextContent(id.toString());
			
			return true;
		}
		catch(Exception e){
			log.error(e);
		}
		
		return false;
	}
	
	/**
	 * Gets the value of an element with a given name in a document.
	 * 
	 * @param doc - the document.
	 * @param name - the name of the element.
	 * @return - the value.
	 */
	private static String getElementValue(Document doc,String name){
		NodeList elemList = doc.getElementsByTagName(name);
		if (!(elemList != null && elemList.getLength() > 0))
			return null;
		return ((Element)elemList.item(0)).getTextContent();
	}
	
	/** 
	 * Creates a new patient from an xform create new patient document.
	 * 
	 * @param doc - the document.
	 * @param creator - the logged on user.
	 * @param patientids - a hashtable to store a mapping between the patientid as in the document 
	 * 					   and that of the newly created patient.
	 * @return - true if the patient is created successfully, else false.
	 */
	private static boolean saveNewPatient(Document doc, User creator,HashMap<String,Integer> patientids){		
		PatientService patientService = Context.getPatientService();
		
		Patient pt = new Patient();
		PersonName pn = new PersonName();
		pn.setGivenName(getElementValue(doc,XformBuilder.NODE_GIVEN_NAME));
		pn.setFamilyName(getElementValue(doc,XformBuilder.NODE_FAMILY_NAME));
		pn.setMiddleName(getElementValue(doc,XformBuilder.NODE_MIDDLE_NAME));
		
		pn.setCreator(creator);
		pn.setDateCreated(new Date());
		pt.addName(pn);
		
		//TODO Needs better date from string conversion.
		String val = getElementValue(doc,XformBuilder.NODE_BIRTH_DATE);
		if(val != null && val.length() > 0)
			try{ pt.setBirthdate(new Date(Date.parse(val))); } catch(Exception e){log.error(e);}
		
		pt.setGender(getElementValue(doc,XformBuilder.NODE_GENDER));
		pt.setCreator(creator);
		pt.setDateCreated(new Date());		
		
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setCreator(creator);
		identifier.setDateCreated(new Date());
		identifier.setIdentifier(getElementValue(doc,XformBuilder.NODE_IDENTIFIER));
		
		int id = Integer.parseInt(getElementValue(doc,XformBuilder.NODE_IDENTIFIER_TYPE_ID));
		PatientIdentifierType identifierType = patientService.getPatientIdentifierType(id);
		identifier.setIdentifierType(identifierType);
		
		identifier.setLocation(getLocation(getElementValue(doc,XformBuilder.NODE_LOCATION_ID)));
				
		pt.addIdentifier(identifier);
		Patient pt2 = patientService.identifierInUse(identifier.getIdentifier(),identifier.getIdentifierType(),pt);
		if(pt2 == null){
			pt = patientService.createPatient(pt);
			patientids.put(getElementValue(doc,XformBuilder.NODE_PATIENT_ID), pt.getPatientId());
			return true;
		}
		//else. Should we just update the existing patient with this identifier? or not?
		return false;
	}
		
	/**
	 * Gets a location object given a locaton id
	 * 
	 * @param locationId - the id.
	 * @return
	 */
	private static Location getLocation(String locationId){
		return Context.getEncounterService().getLocation(Integer.parseInt(locationId));
	}
	
	private static User getCreator(){
		return Context.getAuthenticatedUser();
	}
	
	/**
	 * Converts xforms multiple select answer values to the format expected by
	 * the openmrs form model.
	 * 
	 * @param parentNode - the parent node of the document.
	 */
	private static void setMultipleSelectValues(Node parentNode){
		NodeList nodes = parentNode.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++){
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if(isMultipleSelectNode(node))
				setMultipleSelectNodeValues(node);
			setMultipleSelectValues(node);
		}
	}
	
	/** 
	 * Gets the values of a multiple select node.
	 * 
	 * @param parentNode- the node
	 * @return - a sting with values separated by space.
	 */
	private static String getMultipleSelectNodeValue(Node parentNode){
		String value = null;
		
		NodeList nodes = parentNode.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++){
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String name = node.getNodeName();
			if(name != null && name.equalsIgnoreCase(XformBuilder.NODE_XFORMS_VALUE)){
				value = node.getTextContent();
				parentNode.removeChild(node);
				break;
			}
		}
		
		return value;
	}
	
	/**
	 * Sets the values of an openmrs multiple select node.
	 * 
	 * @param parentNode - the node.
	 */
	private static void setMultipleSelectNodeValues(Node parentNode){
		String values = getMultipleSelectNodeValue(parentNode);
		if(values == null || values.length() == 0)
			return;
		
		String[] valueArray = values.split(XformBuilder.MULTIPLE_SELECT_VALUE_SEPARATOR);
		
		NodeList nodes = parentNode.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++){
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			String name = node.getNodeName();
			if(name.equalsIgnoreCase(XformBuilder.NODE_DATE) || name.equalsIgnoreCase(XformBuilder.NODE_TIME) || 
			   name.equalsIgnoreCase(XformBuilder.NODE_VALUE) || name.equalsIgnoreCase(XformBuilder.NODE_XFORMS_VALUE))
				continue;
			setMultipleSelectNodeValue(node,valueArray);
		}
	}
	
	/**
	 * Sets the value of an openmrs multiple select node.
	 * 
	 * @param node - the multiple select node.
	 * @param valueArray - an array of selected values.
	 */
	private static void setMultipleSelectNodeValue(Node node,String[] valueArray){
		for(String value : valueArray){
			if(!value.equalsIgnoreCase(node.getNodeName()))
				continue;
			node.setTextContent(XformBuilder.VALUE_TRUE);
			return;
		}
		
		node.setTextContent(XformBuilder.VALUE_FALSE);
	}
	
	/**
	 * Checks if a node is multiple select.
	 * 
	 * @param node - the node to check.
	 * @return - true if it is a multiple select node, else false.
	 */
	private static boolean isMultipleSelectNode(Node node){
		boolean multipSelect = false;
		
		NamedNodeMap attributes = node.getAttributes();
		if(attributes != null){
			Node multipleValue = attributes.getNamedItem(XformBuilder.ATTRIBUTE_MULTIPLE);
			if(attributes.getNamedItem(XformBuilder.ATTRIBUTE_OPENMRS_CONCEPT) != null &&  multipleValue != null && multipleValue.getNodeValue().equals("1"))
				multipSelect = true;
		}
		
		return multipSelect;
	}
}
