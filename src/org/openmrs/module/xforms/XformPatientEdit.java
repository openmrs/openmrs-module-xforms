package org.openmrs.module.xforms;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.Element;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.util.DOMUtil;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;



/**
 * 
 * @author daniel
 *
 */
public class XformPatientEdit {

	private static final Log log = LogFactory.getLog(XformPatientEdit.class);
	
	
	public static boolean isPatientElement(Element element){
		return (element.getName().equalsIgnoreCase(XformBuilder.NODE_PATIENT) && 
				String.valueOf(XformConstants.PATIENT_XFORM_FORM_ID).equals(element.getAttributeValue(null,(XformBuilder.ATTRIBUTE_ID))));
	}
	
	
	public static Patient getEditedPatient(Element rootNode) throws Exception {
		
		String patientId = XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_PATIENT_ID);
		Patient patient = Context.getPatientService().getPatient(Integer.parseInt(patientId));
		
		PersonName personName = patient.getPersonName();
		personName.setFamilyName(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_FAMILY_NAME));
		personName.setMiddleName(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_MIDDLE_NAME));
		personName.setGivenName(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_GIVEN_NAME));		
		personName.setDateChanged(new Date());
		personName.setChangedBy(Context.getAuthenticatedUser());
		
		String val = XformBuilder.getNodeValue(rootNode,XformBuilder.NODE_BIRTH_DATE);
		try{ 
			patient.setBirthdate(XformsUtil.fromSubmitString2Date(val)); 
		} catch(Exception e){log.error(val,e); }
				
		patient.setBirthdateEstimated("true".equals(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_BIRTH_DATE_ESTIMATED)));
		
		patient.setGender(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_GENDER));
		patient.setDateChanged(new Date());
		patient.setChangedBy(Context.getAuthenticatedUser());
		
		PatientIdentifier patientIdentifier = patient.getPatientIdentifier();
		patientIdentifier.setIdentifier(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_IDENTIFIER));
		Location location =  Context.getLocationService().getLocation(Integer.parseInt(
				XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_LOCATION_ID)));
		patientIdentifier.setLocation(location);
		
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(
				Integer.parseInt(XformBuilder.getNodeValue(rootNode, XformBuilder.NODE_IDENTIFIER_TYPE_ID)));
		patientIdentifier.setIdentifierType(identifierType);
		
		savePersonAttributes(patient,rootNode);
		
		return patient;
	}
	
	
	private static void savePersonAttributes(Patient patient, Element rootNode) throws Exception {
		
		List<String> complexObs = DOMUtil.getModelComplexObsNodeNames(XformConstants.PATIENT_XFORM_FORM_ID);
		List<String> dirtyComplexObs = XformObsEdit.getEditedComplexObsNames();
		
		PersonService personService = Context.getPersonService();
		for (PersonAttributeType type : personService.getPersonAttributeTypes(PERSON_TYPE.PERSON, null)) {
			String name = "person_attribute"+type.getPersonAttributeTypeId();
			Element element = XformBuilder.getElement(rootNode,name);

			if(element == null)
				continue;
			
			String value = XformBuilder.getTextValue(element);
			
			if(complexObs.contains(name)){				
				if(!dirtyComplexObs.contains(name))
					continue;
				
				value = XformObsEdit.saveComplexObs(name,value,rootNode);
			}
			
			PersonAttribute personAttribute = patient.getAttribute(type.getPersonAttributeTypeId());
			
			if(personAttribute == null){
				personAttribute = new PersonAttribute();
				personAttribute.setAttributeType(type);
				patient.addAttribute(personAttribute);
			}
			else if(value == null || value.length() == 0)
				patient.removeAttribute(personAttribute);
			
			personAttribute.setValue(value);
		}
	}
}
