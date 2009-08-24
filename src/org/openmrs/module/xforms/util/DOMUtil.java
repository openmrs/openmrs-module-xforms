package org.openmrs.module.xforms.util;

import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * 
 * @author daniel
 *
 */
public class DOMUtil {

	/**
	 * Gets the value of an element with a given name in a document.
	 * 
	 * @param doc - the document.
	 * @param name - the name of the element.
	 * @return - the value.
	 */
	public static String getElementValue(Element root,String name){
		NodeList elemList = root.getElementsByTagName(name);
		if (!(elemList != null && elemList.getLength() > 0))
			return null;
		return elemList.item(0).getTextContent();
	}
	
	public static String getElementValue(Document doc,String name){
		return getElementValue(doc.getDocumentElement(),name);
	}
	
	public static boolean setElementValue(Element root,String name,String value){
		NodeList elemList = root.getElementsByTagName(name);
		if (!(elemList != null && elemList.getLength() > 0))
			return false;
		elemList.item(0).setTextContent(value);
		return true;
	}

	/**
	 * Checks if a document is a create new patient one.
	 * One which collected bio data about a new patient.
	 * 
	 * @param doc - the document.
	 * @return - true if so, else false.
	 */
	public static boolean isNewPatientDoc(Document doc){
		return isNewPatientElementDoc(doc.getDocumentElement());
	}
	
	public static boolean isNewPatientElementDoc(Element element){
		return (element.getNodeName().equalsIgnoreCase(XformBuilder.NODE_PATIENT) && 
				String.valueOf(XformConstants.PATIENT_XFORM_FORM_ID).equals(element.getAttribute(XformBuilder.ATTRIBUTE_ID)));
	}
	
	public static boolean isEncounterDoc(Document doc){
		return isEncounterElementDoc(doc.getDocumentElement());
	}
	
	public static boolean isEncounterElementDoc(Element element){
		return (element.getNodeName().equalsIgnoreCase(XformBuilder.NODE_FORM) && 
				element.getAttribute(XformBuilder.ATTRIBUTE_NAME) != null && element.getAttribute(XformBuilder.ATTRIBUTE_ID) != null);
	}
	
	public static String getEncounterFormPatientId(Document doc){
		return getElementValue(doc.getDocumentElement(),XformBuilder.NODE_PATIENT_PATIENT_ID);
	}
	
	public static String getPatientFormPatientId(Document doc){
		return getElementValue(doc.getDocumentElement(),XformBuilder.NODE_PATIENT_ID);
	}
}
