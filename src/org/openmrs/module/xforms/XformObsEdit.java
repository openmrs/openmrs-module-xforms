package org.openmrs.module.xforms;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.reporting.export.DataExportUtil.VelocityExceptionHandler;
import org.openmrs.util.FormConstants;
import org.openmrs.util.FormUtil;


/**
 * 
 * @author daniel
 *
 */
public class XformObsEdit {

	private static final Log log = LogFactory.getLog(XformObsEdit.class);

	public static void fillObs(Document doc, Integer encounterId){

		Element formNode = XformBuilder.getElement(doc.getRootElement(),"form");
		if(formNode == null)
			return;

		Encounter encounter = Context.getEncounterService().getEncounter(encounterId);

		formNode.setAttribute(null, "encounterId", encounter.getEncounterId().toString());

		XformBuilder.setNodeValue(doc, XformBuilder.NODE_ENCOUNTER_LOCATION_ID, encounter.getLocation().getLocationId().toString());
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_ENCOUNTER_ENCOUNTER_DATETIME, XformsUtil.formDate2SubmitString(encounter.getEncounterDatetime()));
		XformBuilder.setNodeValue(doc, XformBuilder.NODE_ENCOUNTER_PROVIDER_ID, encounter.getProvider().getUserId().toString());

		Set<Obs> observations = encounter.getObs();
		for(Obs obs : observations){
			Concept concept = obs.getConcept();

			Element node = XformBuilder.getElement(formNode,FormUtil.getXmlToken(concept.getDisplayString()));
			if(node == null)
				continue;

			String value = obs.getValueAsString(Context.getLocale());
			if(concept.getDatatype().isCoded())
				value = FormUtil.conceptToString(obs.getValueCoded(), Context.getLocale());

			if("1".equals(node.getAttributeValue(null,"multiple"))){
				Element multNode = XformBuilder.getElement(node, "xforms_value");
				if(multNode != null){
					value = XformBuilder.getTextValue(multNode);
					if(value != null && value.trim().length() > 0)
						value += " ";
					else
						value = "";

					String xmlToken = FormUtil.getXmlToken(obs.getValueAsString(Context.getLocale()));
					XformBuilder.setNodeValue(node, "xforms_value", value + xmlToken);

					Element valueNode = XformBuilder.getElement(node, xmlToken);
					if(valueNode != null){
						XformBuilder.setNodeValue(valueNode,"true");
						valueNode.setAttribute(null, "obsId", obs.getObsId().toString());
					}
				}
			}
			else{
				Element valueNode = XformBuilder.getElement(node, "value");
				if(valueNode != null){
					XformBuilder.setNodeValue(valueNode,value);
					valueNode.setAttribute(null, "obsId", obs.getObsId().toString());
				}
				node.setAttribute(null, "obsId", obs.getObsId().toString());
				//XformBuilder.setNodeValue(node, "value", value);
			}
		}

		//System.out.println(XformBuilder.fromDoc2String(doc));
	}

	public static Encounter getEditedEncounter(Document doc,Set<Obs> obs2Void) throws Exception{
		return getEditedEncounter(XformBuilder.getElement(doc.getRootElement(),"form"),obs2Void);
	}

	public static Encounter getEditedEncounter(Element formNode,Set<Obs> obs2Void) throws Exception{
		if(formNode == null || !"form".equals(formNode.getName()))
			return null;

		Date datetime = new Date();

		Integer encounterId = Integer.parseInt(formNode.getAttributeValue(null, "encounterId"));
		Encounter encounter = Context.getEncounterService().getEncounter(encounterId);
		setEncounterHeader(encounter,formNode);

		Hashtable<String,String[]> multipleSelValues = new Hashtable<String,String[]>();

		Set<Obs> observations = encounter.getObs();
		for(Obs obs : observations){
			Concept concept = obs.getConcept();

			String nodeName = FormUtil.getXmlToken(concept.getDisplayString());
			Element node = XformBuilder.getElement(formNode,nodeName);
			if(node == null)
				continue;

			if(isMultipleSelNode(node)){
				String xmlToken = FormUtil.getXmlToken(obs.getValueAsString(Context.getLocale()));
				if(multipleSelValueContains(nodeName,xmlToken,node,multipleSelValues))
					continue;

				voidObs(obs,datetime,obs2Void);
			}
			else{
				Element valueNode = XformBuilder.getElement(node, "value");
				if(valueNode != null){
					String oldValue = obs.getValueAsString(Context.getLocale());
					String newValue = XformBuilder.getTextValue(valueNode);

					if(concept.getDatatype().isCoded())
						oldValue = conceptToString(obs.getValueCoded(), Context.getLocale());

					if(oldValue.equals(newValue))
						continue; //obs has not changed

					voidObs(obs,datetime,obs2Void);

					if(newValue == null || newValue.trim().length() == 0)
						continue;

					//setObsValue(obs,newValue);

					//obs.setDateChanged(datetime);
					//obs.setChangedBy(Context.getAuthenticatedUser());

					encounter.addObs(createObs(concept,newValue,datetime));
				}
				else
					throw new IllegalArgumentException("cannot locate node for concept: " + concept.getDisplayString());

			}
		}

		addNewObs(encounter,XformBuilder.getElement(formNode,"obs"),datetime);

		return encounter;
	}

	private static void voidObs(Obs obs, Date datetime, Set<Obs> obs2Void){
		obs.setVoided(true);
		obs.setVoidedBy(Context.getAuthenticatedUser());
		obs.setDateVoided(datetime);
		obs.setVoidReason("xformsmodule"); //TODO Need to set this from user.

		obs2Void.add(obs);
	}

	private static void setEncounterHeader(Encounter encounter, Element formNode) throws Exception{
		encounter.setLocation(Context.getLocationService().getLocation(Integer.valueOf(XformBuilder.getNodeValue(formNode, XformBuilder.NODE_ENCOUNTER_LOCATION_ID))));
		encounter.setProvider(Context.getUserService().getUser(Integer.valueOf(XformBuilder.getNodeValue(formNode, XformBuilder.NODE_ENCOUNTER_PROVIDER_ID))));
		encounter.setEncounterDatetime(XformsUtil.fromSubmitString2Date(XformBuilder.getNodeValue(formNode, XformBuilder.NODE_ENCOUNTER_ENCOUNTER_DATETIME)));
	}

	private static void addNewObs(Encounter encounter, Element obsNode, Date datetime) throws Exception{
		if(obsNode == null)
			return;

		for(int i=0; i<obsNode.getChildCount(); i++){
			if(obsNode.getType(i) != Element.ELEMENT)
				continue;

			Element node = (Element)obsNode.getChild(i);
			String conceptStr = node.getAttributeValue(null, "openmrs_concept");
			if(conceptStr == null || conceptStr.trim().length() == 0)
				continue;

			Concept concept = Context.getConceptService().getConcept(Integer.parseInt(getConceptId(conceptStr)));

			if(isMultipleSelNode(node))
				addMultipleSelObs(encounter,concept,node,datetime);
			else{
				String obsId = node.getAttributeValue(null, "obsId");
				if(obsId != null && obsId.trim().length() > 0)
					continue; //new obs cant have an obs id

				Element valueNode = XformBuilder.getElement(node, "value");
				if(valueNode == null)
					continue;

				String value = XformBuilder.getTextValue(valueNode);
				if(value == null || value.trim().length() == 0)
					continue;

				Obs obs = createObs(concept,value,datetime);
				encounter.addObs(obs);
			}
		}
	}

	private static void addMultipleSelObs(Encounter encounter, Concept concept,Element node, Date datetime) throws Exception{
		Element multValueNode = XformBuilder.getElement(node, "xforms_value");
		if(multValueNode == null)
			return;

		String	value = XformBuilder.getTextValue(multValueNode);
		if(value == null || value.trim().length() == 0)
			return;

		String[] valueArray = value.split(XformBuilder.MULTIPLE_SELECT_VALUE_SEPARATOR);

		for(int i=0; i<node.getChildCount(); i++){
			if(node.getType(i) != Element.ELEMENT)
				continue;

			Element valueNode = (Element)node.getChild(i);

			String obsId = valueNode.getAttributeValue(null, "obsId");
			if(obsId != null && obsId.trim().length() > 0)
				continue; //new obs cant have an obs id

			String conceptStr = valueNode.getAttributeValue(null, "openmrs_concept");
			if(conceptStr == null || conceptStr.trim().length() == 0)
				continue; //must have an openmrs_concept attribute hence nothing like date or value nodes

			if(!contains(valueNode.getName(),valueArray))
				continue; //name must be in the xforms_value

			Obs obs = createObs(concept,conceptStr,datetime);
			encounter.addObs(obs);
		}
	}

	private static boolean multipleSelValueContains(String nodeName, String valueName,Element node,Hashtable<String,String[]> multipleSelValues){
		String[] values = multipleSelValues.get(nodeName);
		if(values == null){
			Element multNode = XformBuilder.getElement(node, "xforms_value");
			if(multNode != null){
				String value = XformBuilder.getTextValue(multNode);
				if(value == null)
					value = "";
				values = value.split(XformBuilder.MULTIPLE_SELECT_VALUE_SEPARATOR);

				multipleSelValues.put(nodeName, values);
			}
		}

		return contains(valueName,values);
	}

	/*private static void setObsValue(Obs obs,Element valueNode, boolean isNew) throws Exception{
		setObsValue(obs,XformBuilder.getTextValue(valueNode),isNew);
	}*/

	private static boolean setObsValue(Obs obs,String value) throws Exception{
		ConceptDatatype dt = obs.getConcept().getDatatype();

		if (dt.isNumeric())
			obs.setValueNumeric(Double.parseDouble(value.toString()));
		else if (dt.isText())
			obs.setValueText(value);
		else if (dt.isCoded())
			obs.setValueCoded((Concept) convertToType(getConceptId(value), Concept.class));
		else if (dt.isBoolean()){
			boolean booleanValue = value != null && !Boolean.FALSE.equals(value) && !"false".equals(value);
			obs.setValueNumeric(booleanValue ? 1.0 : 0.0);
		}
		else if (dt.isDate())
			obs.setValueDatetime(XformsUtil.fromSubmitString2Date(value));
		else if ("ZZ".equals(dt.getHl7Abbreviation())) {
			// don't set a value
		}else
			throw new IllegalArgumentException("concept datatype not yet implemented: " + dt.getName() + " with Hl7 Abbreviation: " + dt.getHl7Abbreviation());

		return false;
	}

	private static String getConceptId(String conceptStr){
		return conceptStr.substring(0, conceptStr.indexOf('^'));
	}

	private static Object convertToType(String val, Class<?> clazz) {
		if (val == null)
			return null;
		if ("".equals(val) && !String.class.equals(clazz))
			return null;
		if (Location.class.isAssignableFrom(clazz)) {
			LocationEditor ed = new LocationEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else if (User.class.isAssignableFrom(clazz)) {
			UserEditor ed = new UserEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else if (Date.class.isAssignableFrom(clazz)) {
			try {
				DateFormat df = Context.getDateFormat();
				df.setLenient(false);
				return df.parse(val);
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (Double.class.isAssignableFrom(clazz)) {
			return Double.valueOf(val);
		} else if (Concept.class.isAssignableFrom(clazz)) {
			ConceptEditor ed = new ConceptEditor();
			ed.setAsText(val);
			return ed.getValue();
		} else {
			return val;
		}
	}

	private static Obs createObs(Concept concept, String value, Date datetime) throws Exception{
		Obs obs = new Obs();
		obs.setConcept(concept);
		setObsValue(obs,value);

		if (datetime != null)
			obs.setObsDatetime(datetime);

		obs.setCreator(Context.getAuthenticatedUser());

		return obs;
	}

	private static boolean isMultipleSelNode(Element node){
		return "1".equals(node.getAttributeValue(null,"multiple"));
	}

	private static boolean contains(String name,String[] valueArray){
		for(String value : valueArray){
			if(!value.equalsIgnoreCase(name))
				continue;
			return true;
		}

		return false;
	}

	public static String conceptToString(Concept concept, Locale locale) {
		ConceptName localizedName = concept.getName(locale);
		return conceptToString(concept, localizedName);
	}

	/**
	 * Turn the given concept/concept-name pair into a string acceptable for hl7 and forms
	 * 
	 * @param concept Concept to convert to a string
	 * @param localizedName specific localized concept-name
	 * @return String representation of the given concept
	 */
	public static String conceptToString(Concept concept, ConceptName localizedName) {
		return concept.getConceptId() + "^" + localizedName.getName() + "^" + FormConstants.HL7_LOCAL_CONCEPT; // + "^"
		// + localizedName.getConceptNameId() + "^" + localizedName.getName() + "^" + FormConstants.HL7_LOCAL_CONCEPT_NAME;
	}
}


	//String s = "";
	//if(formNode != null)
	//	s = "NOT NULL";
	//else
	//	s = "NULL";
	//for(Obs obs : observations){
	//	Concept concept = obs.getConcept();
	//	s+=":" + FormUtil.conceptToString(concept, Context.getLocale());
	//	s+="+++" + FormUtil.getXmlToken(concept.getDisplayString());
	//	s+="=" + obs.getValueAsString(Context.getLocale());
	//
	//	ConceptDatatype dataType = concept.getDatatype();
	//	s += " & " + FormUtil.getXmlToken(obs.getValueAsString(Context.getLocale()));
	//	if(dataType.isCoded())
	//		s += " !! " + FormUtil.conceptToString(obs.getValueCoded(), Context.getLocale());
	//
