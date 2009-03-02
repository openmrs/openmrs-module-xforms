package org.openmrs.module.xforms;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Processes Xforms Queue entries.
 * When the processing is successful, the queue entry is submitted to the FormEntry Queue.
 * For unsuccessful processing, the queue entry is put in the Xforms error folder.
 * 
 * @author Daniel Kayiwa
 * @version 1.0
 */
@Transactional
public class XformsQueueProcessor {

	private static final Log log = LogFactory.getLog(XformsQueueProcessor.class);
	private static Boolean isRunning = false; // allow only one running
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	public XformsQueueProcessor(){
		
	}
	
	/**
	 * Starts up a thread to process all existing xforms queue entries
	 */
	public void processXformsQueue() throws APIException {
		synchronized (isRunning) {
			if (isRunning) {
				log.warn("XformsQueue processor aborting (another processor already running)");
				return;
			}
			isRunning = true;
		}
		try {			
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			HashMap<String,Integer> patientids = new HashMap<String,Integer>();
			HashMap<String,String> newPatientForms = new HashMap<String,String>();
			//FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);

			File queueDir = XformsUtil.getXformsQueueDir();
			for (File file : queueDir.listFiles()) 
				submitXForm(db,XformsUtil.readFile(file.getAbsolutePath()),/*formEntryService,*/patientids,newPatientForms,file.getAbsolutePath());
			
			//May need to store and read patientids from the hard disk.
			Set<String> keySet = newPatientForms.keySet();
			for(String pathName : keySet)
				submitXForm(db ,newPatientForms.get(pathName),/*formEntryService,*/patientids,null,pathName);
		}
		catch(Exception e){
			log.error("Problem occured while processing Xforms queue", e);
		}
		finally {
			isRunning = false;
		}
	}
		
	//First loop through all forms and 
	//1. create new patients for new patients forms
	//2. put the new patients in a hashtable keyed by the submitted client assigned negative patient id with the server assigned patient id as value
	//3. store forms with new patient ids (negative ones) in a list.
	//4. submit forms for existing patients normally as before.
	//5. At the end of this first loop, loop through the new patient forms as in (3) and 
	//   replace the negative patient ids with the server assigned ones as by the hashtable in (2)
	//   and then submit them to the server.
	private void submitXForm(DocumentBuilder db ,String xml, /*FormEntryService formEntryService,*/HashMap<String,Integer> patientids,HashMap<String,String>  newPatientForms, String pathName){
		String xmlOriginal = xml;
		//String pathName = saveFormInQueue(xml); //Queue the form just incase we get an error during processing.
		try{	
			Document doc = db.parse(IOUtils.toInputStream(xml));
			
			//Check if this is the first loop where we save forms for existing patients,
			//create new patients for new patients forms, store their server to source
			//patientid mappings, store their collected forms for the second loop where
			//we shall have the real server patientids to put in their collected forms.
			if(patientids != null && newPatientForms != null){
				//If new patient form, create patient to get their correponding server patient_id
				if(isNewPatientDoc(doc)){
					if(saveNewPatient(doc,getCreator(doc),patientids))
						saveFormInArchive(xml,pathName);
					else
						saveFormInError(xml,pathName);
					return;
				}

				//Form collected for a new, will submit after creating the correspending
				//patient during the second pass in the loop.
				if(isNewPatientFormDoc(doc)){  
					newPatientForms.put(pathName,xml);
					return;
				}
			}
			
			//If true, this must be second pass after having got the new patient ids
			if(patientids != null && newPatientForms == null){
				if(!setNewPatientId(doc,patientids)){
					//may be new patient creation failed as new patient id is not found.
					//TODO What do we do when the new patiet creation succeeds but saving of the
					//corresponding data collected form fails. This becomes a problem because the
					//new patient document patientids to server patientid mappings are store in
					//the patientids map which is memory and not in peristent storage. So the 
					//collected data form will reference a source patient id which can no longer
					//map to a patient. One solutions could be saving the document in the error 
					//folder with the server created patient id instead of source document patientid.
					//The problem with this is that we lose information as to the original source
					//patient id, just incase we need it for anything.
					saveFormInError(xml,pathName); 
					log.warn("new patient id not found");
					return;
				}
			}

			setMultipleSelectValues(doc.getDocumentElement());
			
			xml = XformsUtil.doc2String(doc);
			/*FormEntryQueue formEntryQueue = new FormEntryQueue();
			formEntryQueue.setFormData(xml);
			formEntryService.createFormEntryQueue(formEntryQueue);*/
			FormEntryWrapper.createFormEntryQueue(xml);
			
			saveFormInArchive(xmlOriginal,pathName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			saveFormInError(xmlOriginal,pathName);
		}
	}
	
	/**
	 * Checks if a document is a create new patient one.
	 * One which collected bio data about a new patient.
	 * 
	 * @param doc - the document.
	 * @return - true if so, else false.
	 */
	private boolean isNewPatientDoc(Document doc){
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
	private boolean isNewPatientFormDoc(Document doc){
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
	 * Achives a submitted form after processing.
	 * 
	 * @param xml - the form data.
	 * @param folder - the folder to save in.
	 * @param queuePathName - the path and name of this file in the queue. If you dont supply this,
	 * 						  a new radom file is created in this folder, else the a file with the
	 * 						  same name as the queued one is created in this folder.
	 */
	private String saveForm(String xml,File folder,String queuePathName){
		String pathName;// = folder.getAbsolutePath()+File.separatorChar+XformsUtil.getRandomFileName()+XformConstants.XML_FILE_EXTENSION;
		if(queuePathName == null)
			pathName = OpenmrsUtil.getOutFile(folder, new Date(), Context.getAuthenticatedUser()).getAbsolutePath();
		else
			pathName = folder.getAbsolutePath()+File.separatorChar+queuePathName.substring(queuePathName.lastIndexOf(File.separatorChar)+1);
		
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
					log.error(e.getMessage(),e);
				}
			}
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
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
	private String saveFormInArchive(String xml,String queuePathName){
		return saveForm(xml,XformsUtil.getXformsArchiveDir(new Date()),queuePathName);
	}
	
	/**
	 * Saves an xform in the errors folder.
	 * 
	 * @param xml - the xml of the xform.
	 * @param queuePathName - the queue full path and file name of this xform.
	 * @return - the error full path and file name.
	 */
	private String saveFormInError(String xml,String queuePathName){
		return saveForm(xml,XformsUtil.getXformsErrorDir(),queuePathName);
	}
	
	/**
	 * Save an xform in the xform queue.
	 * 
	 * @param xml - the xml of the xform.
	 * @return - the queue full path and file name.
	 */
	private String saveFormInQueue(String xml){
		return saveForm(xml,XformsUtil.getXformsQueueDir(),null);
	}
	
	/**
	 * Sets the patientid node to the value of the patient_id as got from the server.
	 * 
	 * @param doc - the document whose patientid node to set.
	 * @param patientIds - a mapping of document patientids to server patientids.
	 * @return - true if set, else false.
	 */
	private boolean setNewPatientId(Document doc, HashMap<String,Integer> patientIds){
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
                //TODO May need to look for new patient forms in the achive to retrieve the ids
				log.error("No patient created with an original id of:"+val);
				return false; //Patient id not found, may be there was a failure at creation.
			}
			
			element.setTextContent(id.toString());
			
			return true;
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
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
	private String getElementValue(Document doc,String name){
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
	private boolean saveNewPatient(Document doc, User creator,HashMap<String,Integer> patientids){		
		PatientService patientService = Context.getPatientService();
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		
		Patient pt = new Patient();
		PersonName pn = new PersonName();
		pn.setGivenName(getElementValue(doc,XformBuilder.NODE_GIVEN_NAME));
		pn.setFamilyName(getElementValue(doc,XformBuilder.NODE_FAMILY_NAME));
		pn.setMiddleName(getElementValue(doc,XformBuilder.NODE_MIDDLE_NAME));
		
		pn.setCreator(creator);
		pn.setDateCreated(new Date());
		pt.addName(pn);
		
		String val = getElementValue(doc,XformBuilder.NODE_BIRTH_DATE);
		if(val != null && val.length() > 0)
			try{ pt.setBirthdate(XformsUtil.formString2Date(val)); } catch(Exception e){log.error(val,e); }
		
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
		
		addPersonAttributes(pt,doc,xformsService);
		
		Patient pt2 = patientService.identifierInUse(identifier.getIdentifier(),identifier.getIdentifierType(),pt);
		if(pt2 == null){
			pt = patientService.createPatient(pt);
			patientids.put(getElementValue(doc,XformBuilder.NODE_PATIENT_ID), pt.getPatientId());
			addPersonRepeatAttributes(pt,doc,xformsService);
			return true;
		}
		else if(rejectExistingPatientCreation()){
			log.error("Tried to create patient who already exists with the identifier:"+identifier.getIdentifier()+" REJECTED.");
			return false;
		}
		else{
			patientids.put(getElementValue(doc,XformBuilder.NODE_PATIENT_ID), pt2.getPatientId());
			log.warn("Tried to create patient who already exists with the identifier:"+identifier.getIdentifier()+" ACCEPTED.");
			return true;
		}
	}
	
	private void addPersonAttributes(Patient pt, Document doc,XformsService xformsService){
		// look for person attributes in the xml doc and save to person
		PersonService personService = Context.getPersonService();
		for (PersonAttributeType type : personService.getPersonAttributeTypes(PERSON_TYPE.PERSON, null)) {
			NodeList nodes = doc.getElementsByTagName("person_attribute"+type.getPersonAttributeTypeId());
			
			if(nodes == null || nodes.getLength() == 0)
				continue;
			
			String value = ((Element)nodes.item(0)).getTextContent();
			if(value == null || value.length() == 0)
				continue;
			
			pt.addAttribute(new PersonAttribute(type, value));
		}
	}
	
	private void addPersonRepeatAttributes(Patient pt, Document doc,XformsService xformsService){
		NodeList nodes = doc.getDocumentElement().getChildNodes();
		if(nodes == null)
			return;
		
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			String name = node.getNodeName();
			if(name.startsWith("person_attribute_repeat_section")){
				//System.out.println("name="+name);
				String attributeId = name.substring("person_attribute_repeat_section".length());
				//System.out.println("attribute id="+attributeId);
				
				addPersonRepeatAttribute(pt,node,attributeId,xformsService);
			}
		}
	}
	
	private void addPersonRepeatAttribute(Patient pt,Node repeatNode,String attributeId,XformsService xformsService){
		NodeList nodes = repeatNode.getChildNodes();
		if(repeatNode == null)
			return;

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			String name = node.getNodeName();
			if(name.startsWith("person_attribute"))		
				addPersonRepeatAttributeValues(pt,node,attributeId,xformsService,index+1);
		}
	}
	
	private void addPersonRepeatAttributeValues(Patient pt,Node repeatNode,String attributeId,XformsService xformsService, int displayOrder){
		NodeList nodes = repeatNode.getChildNodes();
		if(repeatNode == null)
			return;

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			String name = node.getNodeName();
			if(name.startsWith("person_attribute_concept")){
				//System.out.println("name="+name);
				String valueId = name.substring("person_attribute_concept".length());
				//System.out.println("concept id="+valueId);
				
				PersonRepeatAttribute personRepeatAttribute = new PersonRepeatAttribute();
				personRepeatAttribute.setPersonId(pt.getPersonId());
				personRepeatAttribute.setCreator(Context.getAuthenticatedUser().getUserId());
				personRepeatAttribute.setDateCreated(new Date());
				personRepeatAttribute.setValue(node.getTextContent());
				personRepeatAttribute.setValueId(Integer.parseInt(valueId));
				personRepeatAttribute.setValueIdType(PersonRepeatAttribute.VALUE_ID_TYPE_CONCEPT);
				personRepeatAttribute.setValueDisplayOrder(displayOrder);
				personRepeatAttribute.setAttributeTypeId(Integer.parseInt(attributeId));
				
				xformsService.savePersonRepeatAttribute(personRepeatAttribute);
			}
		}
	}
	
	/**
	 * Check if we are to reject forms for patients considered new when they already exist, 
	 * by virture of patient identifier.
	 * @return true if we are to reject, else false.
	 */
	private boolean rejectExistingPatientCreation(){
		String reject = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_REJECT_EXIST_PATIENT_CREATE,XformConstants.DEFAULT_REJECT_EXIST_PATIENT_CREATE);
		return !("false".equalsIgnoreCase(reject));
	}
		
	/**
	 * Gets a location object given a locaton id
	 * 
	 * @param locationId - the id.
	 * @return
	 */
	private Location getLocation(String locationId){
		return Context.getEncounterService().getLocation(Integer.parseInt(locationId));
	}
	
	private User getCreator(Document doc){
		//return Context.getAuthenticatedUser();
		NodeList elemList = doc.getElementsByTagName(XformConstants.NODE_ENTERER);
		if (elemList != null && elemList.getLength() > 0) {
			String s = ((Element)elemList.item(0)).getTextContent();
			User user = Context.getUserService().getUser(Integer.valueOf(String.valueOf(s.charAt(0))));
			return user;
		}
		return null;
	}
	
	/**
	 * Converts xforms multiple select answer values to the format expected by
	 * the openmrs form model.
	 * 
	 * @param parentNode - the parent node of the document.
	 */
	private void setMultipleSelectValues(Node parentNode){
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
	private String getMultipleSelectNodeValue(Node parentNode){
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
	private void setMultipleSelectNodeValues(Node parentNode){
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
	private void setMultipleSelectNodeValue(Node node,String[] valueArray){
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
	private boolean isMultipleSelectNode(Node node){
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
