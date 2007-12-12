package org.openmrs.module.xforms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.*;
import java.io.*;
import org.xmlpull.v1.*;
import org.kxml2.io.*;
import java.util.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.*;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.Role;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.xforms.XformConstants;

/**
 * Builds xforms from openmrs schema and template files.
 * This class also builds the XForm for creating new patients.
 * 
 * @author Daniel Kayiwa
 *
 */
public final class XformBuilder {
	
	public static final String CHARACTER_SET = XformConstants.DEFAULT_CHARACTER_ENCODING;
	public static final String NAMESPACE_XFORMS = "http://www.w3.org/2002/xforms";
	public static final String NAMESPACE_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final String NAMESPACE_XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String NAMESPACE_XHTML = "http://www.w3.org/1999/xhtml";
	public static final String NAMESPACE_OPENMRS = "http://localhost:8080/openmrs/moduleServlet/formentry/forms/schema/4-109";
	
	public static final String PREFIX_XFORMS = "xf";
	public static final String PREFIX_XML_SCHEMA = "xsd";
	public static final String PREFIX_XML_SCHEMA2 = "xs";
	public static final String PREFIX_XML_INSTANCES = "xsi";
	public static final String PREFIX_OPENMRS= "openmrs";
	
	public static final String CONTROL_INPUT = "input";
	public static final String CONTROL_SELECT = "select";
	public static final String CONTROL_SELECT1 = "select1";
	public static final String CONTROL_SUBMIT = "submit";
	
	public static final String NODE_LABEL = "label";
	public static final String NODE_HINT = "hint";
	public static final String NODE_VALUE= "value";
	public static final String NODE_ITEM = "item";
	public static final String NODE_HTML = "html";
	public static final String NODE_SCHEMA = "schema";
	public static final String NODE_HEAD = "head";
	public static final String NODE_BODY = "body";
	public static final String NODE_SUBMISSION= "submission";
	public static final String NODE_INSTANCE = "instance";
	public static final String NODE_MODEL = "model";
	public static final String NODE_BIND = "bind";
	public static final String NODE_TITLE = "title";
	public static final String NODE_ENUMERATION = "enumeration";
	public static final String NODE_DATE = "date";
	public static final String NODE_TIME = "time";
	public static final String NODE_SIMPLETYPE = "simpleType";
	public static final String NODE_COMPLEXTYPE = "complexType";
	public static final String NODE_SEQUENCE = "sequence";
	public static final String NODE_RESTRICTION = "restriction";
	public static final String NODE_ATTRIBUTE = "attribute";
	public static final String NODE_FORM = "form";
	public static final String NODE_PATIENT = "patient";
	public static final String NODE_XFORMS_VALUE= "xforms_value";
	
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_ACTION = "action";
	public static final String ATTRIBUTE_METHOD = "method";
	public static final String ATTRIBUTE_NODESET = "nodeset";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_BIND = "bind";
	public static final String ATTRIBUTE_REF = "ref";
	public static final String ATTRIBUTE_APPEARANCE = "appearance";
	public static final String ATTRIBUTE_NILLABLE = "nillable";
	public static final String ATTRIBUTE_MAXOCCURS = "maxOccurs";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_FIXED = "fixed";
	public static final String ATTRIBUTE_OPENMRS_DATATYPE = "openmrs_datatype";
	public static final String ATTRIBUTE_OPENMRS_CONCEPT = "openmrs_concept";
	public static final String ATTRIBUTE_OPENMRS_ATTRIBUTE = "openmrs_attribute";
	public static final String ATTRIBUTE_OPENMRS_TABLE = "openmrs_table";
	public static final String ATTRIBUTE_SUBMISSION = "submission";
	public static final String ATTRIBUTE_MULTIPLE = "multiple";
	public static final String ATTRIBUTE_READONLY= "readonly";
	public static final String ATTRIBUTE_REQUIRED = "required";
	public static final String ATTRIBUTE_DESCRIPTION_TEMPLATE = "description-template";
	
	public static final String XPATH_VALUE_TRUE = "true()";
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";
	public static final String NODE_SEPARATOR = "/";
	
	public static final String SUBMIT_ID = "submit";
	public static final String SUBMIT_LABEL= "Submit";
	public static final String SUBMISSION_METHOD = "post";
	
	public static final String HTML_TAG_TABLE = "table";
	public static final String HTML_TAG_TABLE_ROW = "tr";
	public static final String HTML_TAG_TABLE_CELL = "td";
	public static final String HTML_TAG_PARAGRAPH = "p";
	
	public static final String HTML_ATTRIBUTE_CELLSPACING = "cellspacing";
	public static final String HTML_ATTRIBUTE_CELLSPACING_VALUE = "10";
	public static final String HTML_ATTRIBUTE_CELLPADDING = "cellpadding";
	public static final String HTML_ATTRIBUTE_CELLPADDING_VALUE = "10";
	public static final String CONTROL_LABEL_PADDING = "     ";
	
	public static final String NODE_ENCOUNTER_LOCATION_ID = "encounter.location_id";
	public static final String NODE_ENCOUNTER_PROVIDER_ID = "encounter.provider_id";
	public static final String NODE_ENCOUNTER_ENCOUNTER_ID = "encounter.encounter_id";
	public static final String NODE_ENCOUNTER_ENCOUNTER_DATETIME = "encounter.encounter_datetime";
	public static final String NODE_PATIENT_PATIENT_ID = "patient.patient_id";
	public static final String NODE_PATIENT_FAMILY_NAME= "patient.family_name";
	public static final String NODE_PATIENT_MIDDLE_NAME = "patient.middle_name";
	public static final String NODE_PATIENT_GIVEN_NAME = "patient.given_name";
	
	public static final String NODE_PATIENT_ID = "patient_id";
	public static final String NODE_FAMILY_NAME = "family_name";
	public static final String NODE_MIDDLE_NAME = "middle_name";
	public static final String NODE_GIVEN_NAME = "given_name";
	public static final String NODE_GENDER = "gender";
	public static final String NODE_IDENTIFIER = "identifier";
	public static final String NODE_BIRTH_DATE = "birth_date";
	public static final String NODE_LOCATION_ID = "location_id";
	public static final String NODE_PROVIDER_ID = "provider_id";
	public static final String NODE_IDENTIFIER_TYPE_ID = "patient_identifier_type_id";
	
	public static final String DATA_TYPE_DATE = "xs:date";
	public static final String DATA_TYPE_INT = "xs:int";
	public static final String DATA_TYPE_TEXT = "xs:string";
	
	public static final String MULTIPLE_SELECT_VALUE_SEPARATOR = " ";
	
	private static String xformAction;
	
	private static Element currentRow = null;
	private static boolean singleColumnLayout = true;
		
	/**
	 * Builds an Xform from an openmrs schema and template xml.
	 * 
	 * @param schemaXml - the schema xml.
	 * @param templateXml - the template xml.
	 * @return - the built xform's xml.
	 */
	public static String getXform4mStrings(String schemaXml, String templateXml,String xformAction){
		return getXform4mDocuments(getDocument(new StringReader(schemaXml)),getDocument(new StringReader(templateXml)),xformAction);
	}
	
	/**
	 * Creates an xform from schema and template files.
	 * 
	 * @param schemaPathName - the complete path and name of the schema file.
	 * @param templatePathName - the complete path and name of the template file
	 * @return the built xform's xml. 
	 */
	public static String getXform4mFiles(String schemaPathName, String templatePathName,String xformAction){
		try{
			Document schemaDoc = getDocument(new FileReader(schemaPathName));
			Document templateDoc = getDocument(new FileReader(templatePathName));
			return getXform4mDocuments(schemaDoc,templateDoc,xformAction);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Converts xml to a documnt object.
	 * 
	 * @param xml - the xml to convert.
	 * @return - the Document object containing the xml.
	 */
	public static Document getDocument(String xml){
		return getDocument(new StringReader(xml));
	}
	
	/**
	 * Sets the value of a node in a document.
	 * 
	 * @param doc - the document.
	 * @param name - the name of the node whose value to set.
	 * @param value - the value to set.
	 * @return - true if the node with the name was found, else false.
	 */
	public static boolean setNodeValue(Document doc, String name, String value){
		Element node = getElement(doc.getRootElement(),name);
		if(node == null)
			return false;
		
		setNodeValue(node,value);
		return true;
	}
	
	/**
	 * Sets the text value of a node.
	 * 
	 * @param node - the node whose value to set.
	 * @param value - the value to set.
	 */
	private static void setNodeValue(Element node, String value){
		for(int i=0; i<node.getChildCount(); i++){
			if(node.isText(i)){
				node.removeChild(i);
				node.addChild(Element.TEXT, value);
				return;
			}
		}

		node.addChild(Element.TEXT, value);
	}
	
	/**
	 * Sets the value of an attribute of a node in a document.
	 * 
	 * @param doc - the document.
	 * @param nodeName - the name of the node.
	 * @param attributeName - the name of the attribute.
	 * @param value - the value to set.
	 * @return
	 */
	public static boolean setNodeAttributeValue(Document doc, String nodeName, String attributeName, String value){
		Element node = getElement(doc.getRootElement(),nodeName);
		if(node == null)
			return false;
		
		node.setAttribute(null, attributeName, value);
		return true;
	}
	
	/**
	 * Gets a child element of a parent node with a given name.
	 * 
	 * @param parent - the parent element
	 * @param name - the name of the child.
	 * @return - the child element.
	 */
	private static Element getElement(Element parent, String name){
		for(int i=0; i<parent.getChildCount(); i++){
			if(parent.getType(i) != Element.ELEMENT)
				continue;
			
			Element child = (Element)parent.getChild(i);
			if(child.getName().equalsIgnoreCase(name))
				return child;
			
			child = getElement(child,name);
			if(child != null)
				return child;
		}
		
		return null;
	}
	
	/**
	 * Builds an Xfrom from an openmrs schema and template document.
	 * 
	 * @param schemaDoc - the schema document.
	 * @param templateDoc - the template document.
	 * @return - the built xform's xml.
	 */
	public static String getXform4mDocuments(Document schemaDoc, Document templateDoc, String xformAction){
		XformBuilder.xformAction = xformAction;
		Element formNode = (Element)templateDoc.getRootElement();
		
		currentRow = null;
		singleColumnLayout = false;
		if(VALUE_TRUE.equalsIgnoreCase(Context.getAdministrationService().getGlobalProperty("xforms.singleColumnLayout")))
				singleColumnLayout = true;
		
		Document doc = new Document();
		doc.setEncoding(CHARACTER_SET);
		Element htmlNode = doc.createElement(NAMESPACE_XHTML, null);
		htmlNode.setName(NODE_HTML);
		htmlNode.setPrefix(null, NAMESPACE_XHTML);
		htmlNode.setPrefix(PREFIX_XFORMS, NAMESPACE_XFORMS);
		htmlNode.setPrefix(PREFIX_XML_SCHEMA, NAMESPACE_XML_SCHEMA);
		htmlNode.setPrefix(PREFIX_XML_SCHEMA2, NAMESPACE_XML_SCHEMA);
		htmlNode.setPrefix(PREFIX_XML_INSTANCES, NAMESPACE_XML_INSTANCE);
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);
		
		Element headNode = doc.createElement(NAMESPACE_XHTML, null);
		headNode.setName(NODE_HEAD);
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);
		
		Element bodyNode = doc.createElement(NAMESPACE_XHTML, null);
		bodyNode.setName(NODE_BODY);
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, bodyNode);
		
		Element titleNode =  doc.createElement(NAMESPACE_XHTML, null);
		titleNode.setName(NODE_TITLE);
		titleNode.addChild(Element.TEXT,formNode.getAttributeValue(null, ATTRIBUTE_NAME));
		headNode.addChild(Element.ELEMENT,titleNode);
		
		Element modelNode =  doc.createElement(NAMESPACE_XFORMS, null);
		modelNode.setName(NODE_MODEL);
		headNode.addChild(Element.ELEMENT,modelNode);
		
		Element instanceNode =  doc.createElement(NAMESPACE_XFORMS, null);
		instanceNode.setName(NODE_INSTANCE);
		modelNode.addChild(Element.ELEMENT,instanceNode);
		
		instanceNode.addChild(Element.ELEMENT, formNode);
		
		Element submitNode =  doc.createElement(NAMESPACE_XFORMS, null);
		submitNode.setName(NODE_SUBMISSION);
		submitNode.setAttribute(null, ATTRIBUTE_ID, SUBMIT_ID);
		submitNode.setAttribute(null, ATTRIBUTE_ACTION, xformAction);
		submitNode.setAttribute(null, ATTRIBUTE_METHOD, SUBMISSION_METHOD);
		modelNode.addChild(Element.ELEMENT,submitNode);
		
		Document xformSchemaDoc = new Document();
		xformSchemaDoc.setEncoding(CHARACTER_SET);
		Element xformSchemaNode = doc.createElement(NAMESPACE_XML_SCHEMA, null);
		xformSchemaNode.setName(NODE_SCHEMA);
		xformSchemaNode.setPrefix(PREFIX_XML_SCHEMA2, NAMESPACE_XML_SCHEMA);
		xformSchemaNode.setPrefix(PREFIX_OPENMRS, NAMESPACE_OPENMRS);
		xformSchemaNode.setAttribute(null, "elementFormDefault", "qualified");
		xformSchemaNode.setAttribute(null, "attributeFormDefault", "unqualified");
		xformSchemaDoc.addChild(org.kxml2.kdom.Element.ELEMENT, xformSchemaNode);
		
		Hashtable bindings = new Hashtable();
		parseTemplate(modelNode,formNode,bindings,bodyNode);
		parseSchema(schemaDoc.getRootElement(),bodyNode,modelNode,xformSchemaNode,bindings);
		
		return fromDoc2String(doc);
	}
	
	/**
	 * Gets the label of an openmrs standard form node
	 * 
	 * @param name - the name of the node.
	 * @return - the label.
	 */
	private static String getDisplayText(String name){
		if(name.equalsIgnoreCase(NODE_ENCOUNTER_ENCOUNTER_DATETIME))
			return "ENCOUNTER DATE";
		else if(name.equalsIgnoreCase(NODE_ENCOUNTER_LOCATION_ID))
			return "LOCATION";
		else if(name.equalsIgnoreCase(NODE_ENCOUNTER_PROVIDER_ID))
			return "PROVIDER";
		else if(name.equalsIgnoreCase(NODE_PATIENT_PATIENT_ID))
			return "PATIENT ID";
		else if(name.equalsIgnoreCase(NODE_PATIENT_MIDDLE_NAME))
			return "MIDDLE NAME";
		else if(name.equalsIgnoreCase(NODE_PATIENT_GIVEN_NAME))
			return "GIVEN NAME";
		else if(name.equalsIgnoreCase(NODE_PATIENT_FAMILY_NAME))
			return "FAMILY NAME";
		else
			return name.replace('_', ' ');
	}
	
	/**
	 * Builds the bindings in the model.
	 * 
	 * @param modelElement
	 * @param formElement
	 * @param bindings
	 * @param bodyNode
	 */
	private static void parseTemplate(Element modelElement, Element formElement, Hashtable bindings,Element bodyNode){
		int numOfEntries = formElement.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (formElement.isText(i))
				continue;
			
			Element child = formElement.getElement(i);
			
			if(child.getAttributeValue(null, ATTRIBUTE_OPENMRS_DATATYPE) == null && child.getAttributeValue(null, ATTRIBUTE_OPENMRS_CONCEPT) != null)
				continue; //these could be like options for multiple select.
			
			String name =  child.getName();
			if((child.getAttributeValue(null, ATTRIBUTE_OPENMRS_CONCEPT) != null && !child.getName().equals("obs")) ||
			  (child.getAttributeValue(null, ATTRIBUTE_OPENMRS_ATTRIBUTE) != null && child.getAttributeValue(null, ATTRIBUTE_OPENMRS_TABLE) != null)){
				Element element = modelElement.createElement(NAMESPACE_XFORMS, null);
				element.setName(ATTRIBUTE_BIND);
				element.setAttribute(null, ATTRIBUTE_ID, child.getName());
				element.setAttribute(null, ATTRIBUTE_NODESET, getNodesetAttValue(child));
				modelElement.addChild(Element.ELEMENT, element);
				bindings.put(child.getName(), element);
				
				if(isMultSelectNode(child)){
					Element xformsValueNode = modelElement.createElement(null, null);
					xformsValueNode.setName(NODE_XFORMS_VALUE);
					xformsValueNode.setAttribute(null, "xsi:nil", VALUE_TRUE);
					child.addChild(Element.ELEMENT, xformsValueNode);
				}
					
				//set data types for the openmrs fixed table fields.
				if(child.getAttributeValue(null, ATTRIBUTE_OPENMRS_ATTRIBUTE) != null && child.getAttributeValue(null, ATTRIBUTE_OPENMRS_TABLE) != null){			
					if(name.equalsIgnoreCase(NODE_ENCOUNTER_ENCOUNTER_DATETIME))
						element.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_DATE);
					else if(name.equalsIgnoreCase(NODE_ENCOUNTER_LOCATION_ID))
						element.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_INT);
					else if(name.equalsIgnoreCase(NODE_ENCOUNTER_PROVIDER_ID))
						element.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_INT);
					else if(name.equalsIgnoreCase(NODE_PATIENT_PATIENT_ID))
						element.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_INT);
					else
						element.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_TEXT);
				}
			}
			
			//build controls for the openmrs fixed table fields
			if(child.getAttributeValue(null, ATTRIBUTE_OPENMRS_ATTRIBUTE) != null && child.getAttributeValue(null, ATTRIBUTE_OPENMRS_TABLE) != null){
				Element controlNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
				if(name.equalsIgnoreCase(NODE_ENCOUNTER_LOCATION_ID) || name.equalsIgnoreCase(NODE_ENCOUNTER_PROVIDER_ID))
					controlNode.setName(CONTROL_SELECT1);
				else
					controlNode.setName(CONTROL_INPUT);
				controlNode.setAttribute(null, ATTRIBUTE_BIND, child.getName());
				
				Element labelNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
				labelNode.setName(NODE_LABEL);
				labelNode.addChild(Element.TEXT, getDisplayText(child.getName())+ "     ");
				controlNode.addChild(Element.ELEMENT, labelNode);
				
				addControl(bodyNode,controlNode);
				
				if(name.equalsIgnoreCase(NODE_ENCOUNTER_LOCATION_ID)){
					List<Location> locations = Context.getEncounterService().getLocations();
					for(Location loc : locations){
						Element itemNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
						itemNode.setName(NODE_ITEM);
						
						Element node = itemNode.createElement(NAMESPACE_XFORMS, null);
						node.setName(NODE_LABEL);
						node.addChild(Element.TEXT, loc.getName());
						itemNode.addChild(Element.ELEMENT, node);
						
						node = itemNode.createElement(NAMESPACE_XFORMS, null);
						node.setName(NODE_VALUE);
						node.addChild(Element.TEXT, loc.getLocationId().toString());
						itemNode.addChild(Element.ELEMENT, node);
						
						controlNode.addChild(Element.ELEMENT, itemNode);
					}
				}
				
				if(name.equalsIgnoreCase(NODE_ENCOUNTER_PROVIDER_ID)){
					List<User> providers = Context.getUserService().getUsersByRole(new Role("Provider"));
					for(User provider : providers){
						Element itemNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
						itemNode.setName(NODE_ITEM);
						
						Element node = itemNode.createElement(NAMESPACE_XFORMS, null);
						node.setName(NODE_LABEL);
						node.addChild(Element.TEXT, provider.getPersonName().toString());
						itemNode.addChild(Element.ELEMENT, node);
						
						node = itemNode.createElement(NAMESPACE_XFORMS, null);
						node.setName(NODE_VALUE);
						node.addChild(Element.TEXT, provider.getUserId().toString());
						itemNode.addChild(Element.ELEMENT, node);
						
						controlNode.addChild(Element.ELEMENT, itemNode);
					}
				}
			}
			
			parseTemplate(modelElement,child,bindings, bodyNode);
		}
	}
	
	/**
	 * Checks whether a node is multiple select or not.
	 * 
	 * @param child - the node to check.
	 * @return - true if multiple select, else false.
	 */
	private static boolean isMultSelectNode(Element child){
		return (child.getAttributeValue(null, ATTRIBUTE_MULTIPLE) != null && child.getAttributeValue(null, ATTRIBUTE_MULTIPLE).equals("1"));
	}
	
	/**
	 * Parses the openmrs schema document.
	 * 
	 * @param rootNode
	 * @param bodyNode
	 * @param modelNode
	 * @param xformSchemaNode
	 * @param bindings
	 */
	private static void parseSchema(Element rootNode,Element bodyNode, Element modelNode, Element xformSchemaNode, Hashtable bindings){
		int numOfEntries = rootNode.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (rootNode.isText(i))
				continue;
			
			Element child = rootNode.getElement(i);
			String name = child.getName();
			if(name.equalsIgnoreCase(NODE_COMPLEXTYPE))
				parseComplexType(child,child.getAttributeValue(null, ATTRIBUTE_NAME),bodyNode,xformSchemaNode,bindings);
			else if(name.equalsIgnoreCase(NODE_SIMPLETYPE) && isUserDefinedSchemaElement(child))
				xformSchemaNode.addChild(0, Element.ELEMENT, child);
		}
		
		if(xformSchemaNode.getChildCount() > 0)
			modelNode.addChild(0, Element.ELEMENT, xformSchemaNode);
	}
		
	/**
	 * Parses a complex type node in an openmrs schema document.
	 * 
	 * @param complexTypeNode - the complex type node
	 * @param name
	 * @param bodyNode
	 * @param xformSchemaNode
	 * @param bindings
	 */
	private static void parseComplexType(Element complexTypeNode,String name, Element bodyNode, Element xformSchemaNode, Hashtable bindings){
		if(name == null)
			return;
		
		if(name.indexOf("_type") == -1)
			return;
		
		name = name.substring(0, name.length() - 5);
		Element bindingNode = (Element)bindings.get(name);
		if(bindingNode == null)
			return;

		Element labelNode = null;
		for(int i=0; i<complexTypeNode.getChildCount(); i++){
			if(complexTypeNode.isText(i))
				continue;
			
			Element node = (Element)complexTypeNode.getChild(i);
			if(node.getName().equalsIgnoreCase(NODE_SEQUENCE))
				labelNode = parseSequenceNode(name,node,bodyNode,xformSchemaNode,bindingNode);
			
			if(labelNode != null && node.getName().equalsIgnoreCase(NODE_ATTRIBUTE) && node.getAttributeValue(null, ATTRIBUTE_NAME) != null && node.getAttributeValue(null, ATTRIBUTE_NAME).equalsIgnoreCase(ATTRIBUTE_OPENMRS_CONCEPT))
			{
				labelNode.addChild(Element.TEXT , getConceptName(node.getAttributeValue(null, ATTRIBUTE_FIXED)) + CONTROL_LABEL_PADDING);
				
				String hint = getConceptDescription(node);
				if(hint != null && hint.length() > 0){
					Element hintNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
					hintNode.setName(NODE_HINT);
					hintNode.addChild(Element.TEXT, getConceptDescription(node));
					labelNode.getParent().addChild(1,Element.ELEMENT, hintNode);
				}
			}
		}
	}
	
	/**
	 * Gets the concept id from a name and id combination.
	 * 
	 * @param conceptName - the concept name.
	 * @return - the id
	 */
	private static Integer getConceptId(String conceptName){
		return Integer.parseInt(conceptName.substring(0, conceptName.indexOf("^")));
	}
	
	/**
	 * Gets the description of a concept.
	 * 
	 * @param node - the node having the concept.
	 * @return - the concept description.
	 */
	private static String getConceptDescription(Element node){
		String name = node.getAttributeValue(null, ATTRIBUTE_FIXED);
		Concept concept = Context.getConceptService().getConcept(getConceptId(name));
		ConceptName conceptName = concept.getName();
		return conceptName.getDescription();
	}
	
	/**
	 * Gets the name of a concept from the name and id combination value.
	 * 
	 * @param val - the name and id combination.
	 * @return - the cencept name.
	 */
	private static String getConceptName(String val){
		return val.substring(val.indexOf('^')+1, val.lastIndexOf('^'));
	}
	
	/**
	 * Parses a sequence node from an openmrs schema document.
	 * 
	 * @param name
	 * @param sequenceNode
	 * @param bodyNode
	 * @param xformSchemaNode
	 * @param bindingNode
	 * @return
	 */
	private static Element parseSequenceNode(String name,Element sequenceNode,Element bodyNode, Element xformSchemaNode, Element bindingNode ){
		Element labelNode = null,controlNode = bodyNode.createElement(NAMESPACE_XFORMS, null);;
		
		for(int i=0; i<sequenceNode.getChildCount(); i++){
			if(sequenceNode.isText(i))
				continue;
			
			Element node = (Element)sequenceNode.getChild(i);
			String itemName = node.getAttributeValue(null, ATTRIBUTE_NAME);
		
			if(!itemName.equalsIgnoreCase(NODE_VALUE)){
				if(!(itemName.equalsIgnoreCase(NODE_DATE) || itemName.equalsIgnoreCase(NODE_TIME)) && node.getAttributeValue(null, ATTRIBUTE_OPENMRS_CONCEPT) == null)
					labelNode = parseMultiSelectNode(name,itemName, node,controlNode,bodyNode, labelNode,bindingNode);
				continue;
			}
			
			//We are interested in the element whose name attribute is equal to value, for single select lists.
			if(node.getAttributeValue(null, ATTRIBUTE_NILLABLE).equalsIgnoreCase("0"))
				bindingNode.setAttribute(null, ATTRIBUTE_REQUIRED, XPATH_VALUE_TRUE);
			
			String type = node.getAttributeValue(null, ATTRIBUTE_TYPE);
			if(type != null){
				if(type.indexOf(":") == -1)
					type = "xs:" + type;
				bindingNode.setAttribute(null, ATTRIBUTE_TYPE, type);
				
				Element inputNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
				inputNode.setName(CONTROL_INPUT);
				inputNode.setAttribute(null, ATTRIBUTE_BIND, name);
				
				labelNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
				labelNode.setName(NODE_LABEL);
				inputNode.addChild(Element.ELEMENT, labelNode);
								
				addControl(bodyNode,inputNode); //bodyNode.addChild(Element.ELEMENT, inputNode);
				return labelNode;
			}
			else{
				for(int j=0; j<node.getChildCount(); j++){
					if(node.isText(j))
						continue;
					
					Element simpleTypeNode = (Element)node.getChild(j);
					if(!simpleTypeNode.getName().equalsIgnoreCase(NODE_SIMPLETYPE))
						continue;
					
					return parseSimpleType(name,simpleTypeNode,bodyNode,bindingNode);
				}
			}
		}
		
		return labelNode;
	}
	
	/**
	 * Parses a simple type node in an openmrs schema document.
	 * 
	 * @param name
	 * @param simpleTypeNode
	 * @param bodyNode
	 * @param bindingNode
	 * @return
	 */
	private static Element parseSimpleType(String name,Element simpleTypeNode,Element bodyNode,Element bindingNode){
		for(int i=0; i<simpleTypeNode.getChildCount(); i++){
			if(simpleTypeNode.isText(i))
				continue;
			
			Element restrictionChild = (Element)simpleTypeNode.getElement(i);
			if(restrictionChild.getName().equalsIgnoreCase(NODE_RESTRICTION))
				return parseRestriction(name,(Element)simpleTypeNode.getParent(),restrictionChild,bodyNode,bindingNode);
		}
		
		return null;
	}
	
	private static Element getMultiSelectItemNode(Element node){
		Element retNode;
		for(int i=0; i<node.getChildCount(); i++){
			if(node.isText(i))
				continue;
			
			Element child = (Element)node.getChild(i);
			if(child.getName().equalsIgnoreCase(NODE_ATTRIBUTE))
				return child;
			
			retNode = getMultiSelectItemNode(child);
			if(retNode != null)
				return retNode;
		}
		
		return null;
	}
	
	/**
	 * Parses a multi select node and builds its corresponding items.
	 * 
	 * @param name
	 * @param itemName
	 * @param valueNode
	 * @param controlNode
	 * @param bodyNode
	 * @param labelNode
	 * @param bindingNode
	 * @return
	 */
	private static Element parseMultiSelectNode(String name,String itemName, Element valueNode,Element controlNode,Element bodyNode, Element labelNode, Element bindingNode){		
		//String binding = bindingNode.getAttributeValue(null, ATTRIBUTE_NODESET); //+"/"+itemName;
		
		if(controlNode.getChildCount() == 0){
			//controlNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
			controlNode.setName(CONTROL_SELECT);
			controlNode.setAttribute(null, ATTRIBUTE_BIND, name);
			controlNode.setAttribute(null, ATTRIBUTE_APPEARANCE, Context.getAdministrationService().getGlobalProperty("xforms.multiSelectAppearance"));
			
			labelNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
			labelNode.setName(NODE_LABEL);
			controlNode.addChild(Element.ELEMENT, labelNode);
			
			addControl(bodyNode,controlNode); //bodyNode.addChild(Element.ELEMENT, controlNode);
			
			bindingNode.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_TEXT);
		}
		
		Element node  = getMultiSelectItemNode(valueNode);
		String value = node.getAttributeValue(null, ATTRIBUTE_FIXED);
		String lable = getConceptName(value);
		
		Element itemLabelNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
		itemLabelNode.setName(NODE_LABEL);	
		itemLabelNode.addChild(Element.TEXT, lable);
		
		Element itemValNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
		itemValNode.setName(NODE_VALUE);	
		itemValNode.addChild(Element.TEXT, itemName /*value*/ /*binding*/);
		
		Element itemNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
		itemNode.setName(NODE_ITEM);
		itemNode.addChild(Element.ELEMENT, itemLabelNode);
		itemNode.addChild(Element.ELEMENT, itemValNode);
		
		controlNode.addChild(Element.ELEMENT, itemNode);
		
		return labelNode;

	}
	
	/**
	 * Adds a UI control to the document body.
	 * 
	 * @param bodyNode - the body node.
	 * @param controlNode - the UI control.
	 */
	private static void addControl(Element bodyNode, Element controlNode){
		Element tableNode = null;
		if(bodyNode.getChildCount() == 0){
			tableNode = bodyNode.createElement(NAMESPACE_XHTML, null);
			tableNode.setName(HTML_TAG_TABLE);
			tableNode.setAttribute(null, HTML_ATTRIBUTE_CELLSPACING, HTML_ATTRIBUTE_CELLSPACING_VALUE);
			//tableNode.setAttribute(null, HTML_ATTRIBUTE_CELLPADDING, HTML_ATTRIBUTE_CELLPADDING_VALUE);
			bodyNode.addChild(Element.ELEMENT, tableNode);
						
			Element paragraphNode = bodyNode.createElement(NAMESPACE_XHTML, null);
			paragraphNode.setName(HTML_TAG_PARAGRAPH);
			bodyNode.addChild(Element.ELEMENT, paragraphNode);
			
			Element submitNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
			submitNode.setName(CONTROL_SUBMIT);
			submitNode.setAttribute(null, ATTRIBUTE_SUBMISSION, SUBMIT_ID);
			paragraphNode.addChild(Element.ELEMENT, submitNode);
			
			Element labelNode = submitNode.createElement(NAMESPACE_XFORMS, null);
			labelNode.setName(NODE_LABEL);
			labelNode.addChild(Element.TEXT, SUBMIT_LABEL);
			submitNode.addChild(Element.ELEMENT, labelNode);
		}
		else
			tableNode = bodyNode.getElement(0);
		
		boolean newRowCreated = false;
		if(singleColumnLayout || (!singleColumnLayout && currentRow == null)){
			currentRow = tableNode.createElement(NAMESPACE_XHTML, null);
			currentRow.setName(HTML_TAG_TABLE_ROW);
			tableNode.addChild(Element.ELEMENT, currentRow);
			newRowCreated = true;
		}
		
		Element cell = currentRow.createElement(NAMESPACE_XHTML, null);
		cell.setName(HTML_TAG_TABLE_CELL);
		currentRow.addChild(Element.ELEMENT, cell);
		
		cell.addChild(Element.ELEMENT, controlNode);
		
		if(!newRowCreated)
			currentRow = null;
	}
	
	private static Element parseRestriction(String name, Element valueNode,Element restrictionChild,Element bodyNode, Element bindingNode){
		String type = restrictionChild.getAttributeValue(null, "base");
		if(type.indexOf(":") == -1)
			type = "xs:" + type;
		bindingNode.setAttribute(null, ATTRIBUTE_TYPE, type);
		
		String controlName = CONTROL_SELECT;
		String maxOccurs = valueNode.getAttributeValue(null, ATTRIBUTE_MAXOCCURS);
		if(maxOccurs != null && maxOccurs.equalsIgnoreCase("1"))
			controlName = CONTROL_SELECT1;
		
		Element controlNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
		controlNode.setName(controlName);
		controlNode.setAttribute(null, ATTRIBUTE_BIND, name);
		controlNode.setAttribute(null, ATTRIBUTE_APPEARANCE, Context.getAdministrationService().getGlobalProperty("xforms.singleSelectAppearance"));
		addControl(bodyNode,controlNode); //bodyNode.addChild(Element.ELEMENT, controlNode);
		
		Element labelNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
		labelNode.setName(NODE_LABEL);
		controlNode.addChild(Element.ELEMENT, labelNode);
	
		Element itemValNode = null;
		Element itemLabelNode = null;
		for(int i=0; i<restrictionChild.getChildCount(); i++){
			
			if(restrictionChild.getType(i) == Element.ELEMENT){
				Element child = restrictionChild.getElement(i);
				if(child.getName().equalsIgnoreCase(NODE_ENUMERATION))
				{
					itemValNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
					itemValNode.setName(NODE_VALUE);	
					itemValNode.addChild(Element.TEXT, child.getAttributeValue(null, NODE_VALUE));
				}
			}
			
			if(restrictionChild.getType(i) == Element.COMMENT){
				itemLabelNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
				itemLabelNode.setName(NODE_LABEL);	
				itemLabelNode.addChild(Element.TEXT, restrictionChild.getChild(i));
			}
			
			if(itemLabelNode != null && itemValNode != null){
				Element itemNode = bodyNode.createElement(NAMESPACE_XFORMS, null);
				itemNode.setName(NODE_ITEM);
				controlNode.addChild(Element.ELEMENT, itemNode);

				itemNode.addChild(Element.ELEMENT, itemLabelNode);
				itemNode.addChild(Element.ELEMENT, itemValNode);
				itemLabelNode = null;
				itemValNode = null;
			}
		}
		
		return labelNode;
	}
	
	private static boolean hasValueNode(Element node){
		for(int i=0; i<node.getChildCount(); i++){
			if(node.isText(i))
				continue;
			
			Element child = node.getElement(i);
			if(child.getName().equalsIgnoreCase(NODE_VALUE))
				return true;
		}
		
		return false;
	}
	
	private static String getNodesetAttValue(Element node){
		if(hasValueNode(node))
			return getNodePath(node) + "/value";
		else if(isMultSelectNode(node))
			return getNodePath(node) + "/xforms_value";
		else
			return getNodePath(node);
	}
	
	private static String getNodePath(Element node){
		String path = node.getName();
		Element parent = (Element)node.getParent();
		while(parent != null && !parent.getName().equalsIgnoreCase(NODE_INSTANCE)){
			path = parent.getName() + NODE_SEPARATOR + path;
			parent = (Element)parent.getParent();
		}
		return NODE_SEPARATOR + path;
	}
	
	/**
	 * Converts an xml document to a string.
	 * 
	 * @param doc - the document.
	 * @return the xml string in in the document.
	 */
	public static String fromDoc2String(Document doc){
		KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try{
			serializer.setOutput(dos,null);
			doc.write(serializer);
			serializer.flush();
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		byte[] byteArr = bos.toByteArray();
		char[]charArray = new char[byteArr.length];
		for(int i=0; i<byteArr.length; i++)
			charArray[i] = (char)byteArr[i];
		
		return String.valueOf(charArray);
	}
	
	/**
	 * Gets a document from a stream reader.
	 * 
	 * @param reader - the reader.
	 * @return the document.
	 */
	public static Document getDocument(Reader reader){
		Document doc = new Document();
		
		try{
			KXmlParser parser = new KXmlParser();
			parser.setInput(reader);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			
			doc.parse(parser);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public static void setPatientTableFieldValues(Element parentNode, Integer patientId, XformsService xformsService){
		int numOfEntries = parentNode.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (parentNode.getType(i) != Element.ELEMENT)
				continue;
			
			Element child = (Element)parentNode.getChild(i);
			String tableName = child.getAttributeValue(null, ATTRIBUTE_OPENMRS_TABLE);
			String columnName = child.getAttributeValue(null, ATTRIBUTE_OPENMRS_ATTRIBUTE);
			if(tableName != null && columnName != null && isUserDefinedNode(child.getName())){
				Object value  = getPatientValue(xformsService,patientId,tableName,columnName);
				if(value != null)
					XformBuilder.setNodeValue(child, value.toString());
			}
			
			setPatientTableFieldValues(child, patientId, xformsService);
		}
	}
	
	private static Object getPatientValue(XformsService xformsService, Integer patientId, String tableName, String columnName){
		
		Object value = null;
		
		try{
			value = xformsService.getPatientValue(patientId, tableName, columnName);
		}
		catch(Exception e){
			System.out.println("No column called: "+columnName + " in table: " + tableName);
		}
		
		return value;
	}
	
	private static boolean isUserDefinedNode(String name){
		return !(name.equalsIgnoreCase(NODE_ENCOUNTER_ENCOUNTER_DATETIME)||
				name.equalsIgnoreCase(NODE_ENCOUNTER_LOCATION_ID)||
				name.equalsIgnoreCase(NODE_ENCOUNTER_PROVIDER_ID)||
				name.equalsIgnoreCase(NODE_PATIENT_MIDDLE_NAME)||
				name.equalsIgnoreCase(NODE_PATIENT_GIVEN_NAME)||
				name.equalsIgnoreCase(NODE_PATIENT_PATIENT_ID)||
				name.equalsIgnoreCase(NODE_PATIENT_FAMILY_NAME));
	}
	
	private static boolean isUserDefinedSchemaElement(Element node){
		
		if(!(node.getName().equalsIgnoreCase(NODE_SIMPLETYPE) || node.getName().equalsIgnoreCase(NODE_COMPLEXTYPE)))
			return false;
		
		String name = node.getAttributeValue(null, ATTRIBUTE_NAME);
		
		return !(name.equalsIgnoreCase("form") ||
				name.equalsIgnoreCase("_header_section") ||
				name.equalsIgnoreCase("_other_section") ||
				name.equalsIgnoreCase("_requiredString") ||
				name.equalsIgnoreCase("_infopath_boolean") ||
				name.equalsIgnoreCase("encounter_section") ||
				name.equalsIgnoreCase("obs_section") ||
				name.equalsIgnoreCase("patient_section"));
	}
	
	public static String getNewPatientXform(String xformAction){
		XformBuilder.xformAction = xformAction;
			
		Document doc = new Document();
		doc.setEncoding(CHARACTER_SET);
		Element htmlNode = doc.createElement(NAMESPACE_XHTML, null);
		htmlNode.setName(NODE_HTML);
		htmlNode.setPrefix(null, NAMESPACE_XHTML);
		htmlNode.setPrefix(PREFIX_XFORMS, NAMESPACE_XFORMS);
		htmlNode.setPrefix(PREFIX_XML_SCHEMA, NAMESPACE_XML_SCHEMA);
		htmlNode.setPrefix(PREFIX_XML_SCHEMA2, NAMESPACE_XML_SCHEMA);
		htmlNode.setPrefix(PREFIX_XML_INSTANCES, NAMESPACE_XML_INSTANCE);
		doc.addChild(org.kxml2.kdom.Element.ELEMENT, htmlNode);
		
		Element headNode = doc.createElement(NAMESPACE_XHTML, null);
		headNode.setName(NODE_HEAD);
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, headNode);
		
		Element bodyNode = doc.createElement(NAMESPACE_XHTML, null);
		bodyNode.setName(NODE_BODY);
		htmlNode.addChild(org.kxml2.kdom.Element.ELEMENT, bodyNode);
		
		Element titleNode =  doc.createElement(NAMESPACE_XHTML, null);
		titleNode.setName(NODE_TITLE);
		titleNode.addChild(Element.TEXT,"Patient");
		headNode.addChild(Element.ELEMENT,titleNode);
		
		Element modelNode =  doc.createElement(NAMESPACE_XFORMS, null);
		modelNode.setName(NODE_MODEL);
		headNode.addChild(Element.ELEMENT,modelNode);
		
		Element instanceNode =  doc.createElement(NAMESPACE_XFORMS, null);
		instanceNode.setName(NODE_INSTANCE);
		modelNode.addChild(Element.ELEMENT,instanceNode);
		
		Element formNode =  doc.createElement(null, null);
		formNode.setName(NODE_PATIENT);
		formNode.setAttribute(null, ATTRIBUTE_ID, "0");
		formNode.setAttribute(null, ATTRIBUTE_DESCRIPTION_TEMPLATE,"${/patient/family_name}$ ${/patient/middle_name}$ ${/patient/given_name}$");
		
		instanceNode.addChild(Element.ELEMENT, formNode);
		
		Element submitNode =  doc.createElement(NAMESPACE_XFORMS, null);
		submitNode.setName(NODE_SUBMISSION);
		submitNode.setAttribute(null, ATTRIBUTE_ID, SUBMIT_ID);
		submitNode.setAttribute(null, ATTRIBUTE_ACTION, xformAction);
		submitNode.setAttribute(null, ATTRIBUTE_METHOD, SUBMISSION_METHOD);
		modelNode.addChild(Element.ELEMENT,submitNode);
		
		addPatientNode(formNode,modelNode,bodyNode,NODE_FAMILY_NAME,DATA_TYPE_TEXT,"Family Name","The patient family name",true,false,CONTROL_INPUT,null,null);
		addPatientNode(formNode,modelNode,bodyNode,NODE_MIDDLE_NAME,DATA_TYPE_TEXT,"Middle Name","The patient middle name",false,false,CONTROL_INPUT,null,null);
		addPatientNode(formNode,modelNode,bodyNode,NODE_GIVEN_NAME,DATA_TYPE_TEXT,"Given Name","The patient given name",false,false,CONTROL_INPUT,null,null);
		addPatientNode(formNode,modelNode,bodyNode,NODE_BIRTH_DATE,DATA_TYPE_DATE,"Birth Date","The patient birth date",false,false,CONTROL_INPUT,null,null);
		addPatientNode(formNode,modelNode,bodyNode,NODE_IDENTIFIER,DATA_TYPE_TEXT,"Identifier","The patient identifier",true,false,CONTROL_INPUT,null,null);
		addPatientNode(formNode,modelNode,bodyNode,NODE_PATIENT_ID,DATA_TYPE_INT,"Patient ID","The patient ID",false,true,CONTROL_INPUT,null,null);
		
		addPatientNode(formNode,modelNode,bodyNode,NODE_GENDER,DATA_TYPE_TEXT,"Gender","The patient's sex",false,false,CONTROL_SELECT1,new String[]{"Male","Female"},new String[]{"M","F"});
		
		String[] items, itemValues; int i=0;
		List<Location> locations = Context.getEncounterService().getLocations();
		if(locations != null){
			items = new String[locations.size()];
			itemValues = new String[locations.size()];
			for(Location loc : locations){
				items[i] = loc.getName();
				itemValues[i++] = loc.getLocationId().toString();
			}
			addPatientNode(formNode,modelNode,bodyNode,NODE_LOCATION_ID,DATA_TYPE_INT,"Location","The patient's location",true,false,CONTROL_SELECT1,items,itemValues);
		}
		
		List<PatientIdentifierType> identifierTypes = Context.getPatientService().getPatientIdentifierTypes();
		if(identifierTypes != null){
			i=0;
			items = new String[identifierTypes.size()];
			itemValues = new String[identifierTypes.size()];
			for(PatientIdentifierType identifierType : identifierTypes){
				items[i] = identifierType.getName();
				itemValues[i++] = identifierType.getPatientIdentifierTypeId().toString();
			}
			addPatientNode(formNode,modelNode,bodyNode,"patient_identifier_type_id",DATA_TYPE_INT,"Identifier Type","The patient's identifier type",true,false,CONTROL_SELECT1,items,itemValues);
		}
		
		return XformBuilder.fromDoc2String(doc);
	}
	
	private static void addPatientNode(Element formNode,Element modelNode,Element bodyNode,String name,String type,String label, String hint,boolean required, boolean readonly, String controlType, String[] items, String[] itemValues){
		//add the model node
		Element element = formNode.createElement(null, null);
		element.setName(name);
		formNode.addChild(Element.ELEMENT, element);
		
		//add the model binding
		element = modelNode.createElement(NAMESPACE_XFORMS, null);
		element.setName(NODE_BIND);
		element.setAttribute(null, ATTRIBUTE_ID, name);
		element.setAttribute(null, ATTRIBUTE_NODESET, "/"+NODE_PATIENT+"/"+name);
		element.setAttribute(null, ATTRIBUTE_TYPE, type);
		if(readonly)
			element.setAttribute(null, ATTRIBUTE_READONLY, XPATH_VALUE_TRUE);
		if(required)
			element.setAttribute(null, ATTRIBUTE_REQUIRED, XPATH_VALUE_TRUE);
		modelNode.addChild(Element.ELEMENT, element);
		
		//add the control
		element = bodyNode.createElement(NAMESPACE_XFORMS, null);
		element.setName(controlType);
		element.setAttribute(null, ATTRIBUTE_BIND, name);
		bodyNode.addChild(Element.ELEMENT, element);
		
		//add the label
		Element child = element.createElement(NAMESPACE_XFORMS, null);
		child.setName(NODE_LABEL);
		child.addChild(Element.TEXT, label);
		element.addChild(Element.ELEMENT, child);
		
		//add the hint
		child = element.createElement(NAMESPACE_XFORMS, null);
		child.setName(NODE_HINT);
		child.addChild(Element.TEXT, hint);
		element.addChild(Element.ELEMENT, child);
		
		//add control items
		if(items != null){
			for(int i=0; i<items.length; i++){
				child = element.createElement(NAMESPACE_XFORMS, null);
				child.setName(NODE_ITEM);
				element.addChild(Element.ELEMENT, child);
				
				Element elem = element.createElement(NAMESPACE_XFORMS, null);
				elem.setName(NODE_LABEL);
				elem.addChild(Element.TEXT, items[i]);
				child.addChild(Element.ELEMENT, elem);
				
				elem = element.createElement(NAMESPACE_XFORMS, null);
				elem.setName(NODE_VALUE);
				elem.addChild(Element.TEXT, itemValues[i]);
				child.addChild(Element.ELEMENT, elem);
			}
		}
	}
}
