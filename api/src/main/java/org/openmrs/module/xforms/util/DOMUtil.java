package org.openmrs.module.xforms.util;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
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
	
	public static Element getElement(Document doc,String name){
		return getElement(doc.getDocumentElement(),name);
	}
	
	public static Element getElement(Element root,String name){
		NodeList elemList = root.getElementsByTagName(name);
		if (!(elemList != null && elemList.getLength() > 0))
			return null;
		return (Element)elemList.item(0);
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
	 * Checks if a document is a create patient one.
	 * One which collected bio data about a patient.
	 * 
	 * @param doc - the document.
	 * @return - true if so, else false.
	 */
	public static boolean isPatientDoc(Document doc){
		return isPatientElementDoc(doc.getDocumentElement());
	}
	
	public static boolean isPatientElementDoc(Element element){
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
	
	public static List<String> getXformComplexObsNodeNames(String xml) throws Exception {
		return getXformComplexObsNodeNames(XformsUtil.fromString2Doc(xml).getDocumentElement());
	}
	
	public static List<String> getModelComplexObsNodeNames(String id) throws Exception{
		return getModelComplexObsNodeNames(Integer.parseInt(id));

	}
	
	public static List<String> getModelComplexObsNodeNames(int id) throws Exception{
		Xform xform = ((XformsService)Context.getService(XformsService.class)).getXform(id);
		
		if(xform == null)
			return new ArrayList<String>(); //could be a new patient xform which may not be saved yet.
		
		Document doc = XformsUtil.fromString2Doc(xform.getXformXml());

		return getXformComplexObsNodeNames(doc.getDocumentElement());
	}
	
	public static List<String> getXformComplexObsNodeNames(Element root) throws Exception{
		List<String> names = new ArrayList<String>();

		NodeList elemList = root.getElementsByTagName("xf:bind");
		if(elemList == null)
			elemList = root.getElementsByTagName("bind");
		
		if (elemList != null){
			for(int index = 0; index < elemList.getLength(); index++){
				Element node = (Element)elemList.item(index);
				if( "xsd:base64Binary".equalsIgnoreCase(node.getAttribute("type")) || 
						"binary".equalsIgnoreCase(node.getAttribute("type")) )
					names.add(node.getAttribute("id"));
			}
		}
		
		return names;
	}
}
