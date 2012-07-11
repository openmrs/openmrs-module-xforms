/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.kxml2.kdom.Element;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;

/**
 * Utility for building relationship nodes in xforms.
 * 
 * @since 4.0.3
 */
public class RelationshipBuilder {
	
	public static final String NODE_PATIENT_RELATIONSHIP = "patient_relationship";
	
	public static final String BIND_PATIENT_RELATIONSHIP = "patient." + NODE_PATIENT_RELATIONSHIP;
	
	public static final String BIND_RELATIVE = NODE_PATIENT_RELATIONSHIP + ".relative";
	
	public static final String BIND_RELATIONSHIP_TYPE_ID = NODE_PATIENT_RELATIONSHIP + ".relationship_type_id";
	
	public static final String NODE_RELATIVE = "relative";
	
	public static void build(Element modelElement, Element bodyNode, Element dataNode) {
		//Create the parent repeat ui node.
		Element groupNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		groupNode.setName(XformBuilder.NODE_GROUP);
		//groupNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, BIND_RELATIVE);
		bodyNode.addChild(Element.ELEMENT, groupNode);
		
		Element labelNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		labelNode.setName(XformBuilder.NODE_LABEL);
		labelNode.addChild(Element.TEXT, "RELATIONSHIPS");
		groupNode.addChild(Element.ELEMENT, labelNode);
		
		Element hintNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		hintNode.setName(XformBuilder.NODE_HINT);
		hintNode.addChild(Element.TEXT, "Relationships that this patient has.");
		groupNode.addChild(Element.ELEMENT, hintNode);
		
		Element repeatNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		repeatNode.setName(XformBuilder.CONTROL_REPEAT);
		repeatNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, BIND_PATIENT_RELATIONSHIP);
		groupNode.addChild(Element.ELEMENT, repeatNode);
		
		//Create relative input node.
		Element inputNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		inputNode.setName(XformBuilder.CONTROL_INPUT);
		inputNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, NODE_RELATIVE);
		repeatNode.addChild(Element.ELEMENT, inputNode);
		
		//TODO, Fix the input node for the relative to support person search widget
		
		//Create relative label.
		labelNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		labelNode.setName(XformBuilder.NODE_LABEL);
		labelNode.addChild(Element.TEXT, "RELATIVE");
		inputNode.addChild(Element.ELEMENT, labelNode);
		
		//Create relationship type input node.
		inputNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		inputNode.setName(XformBuilder.CONTROL_SELECT1);
		inputNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, BIND_RELATIONSHIP_TYPE_ID);
		
		populateRelationshipTypes(inputNode);
		
		repeatNode.addChild(Element.ELEMENT, inputNode);
		
		//Create relationship label.
		labelNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		labelNode.setName(XformBuilder.NODE_LABEL);
		labelNode.addChild(Element.TEXT, "RELATIONSHIP");
		inputNode.addChild(Element.ELEMENT, labelNode);
		
		//Create bind node for patient relationship.
		Element bindNode = modelElement.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		bindNode.setName(XformBuilder.NODE_BIND);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_ID, BIND_PATIENT_RELATIONSHIP);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_NODESET, "/form/patient/patient_relationship");
		modelElement.addChild(Element.ELEMENT, bindNode);
		
		//Create bind node for patient relationship type.
		bindNode = modelElement.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		bindNode.setName(XformBuilder.NODE_BIND);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_ID, BIND_RELATIONSHIP_TYPE_ID);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_NODESET,
		    "/form/patient/patient_relationship/patient_relationship.relationship_type_id");
		modelElement.addChild(Element.ELEMENT, bindNode);
		
		//Create bind node for relative.
		bindNode = modelElement.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		bindNode.setName(XformBuilder.NODE_BIND);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_ID, NODE_RELATIVE);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_NODESET, "/form/patient/patient_relationship/relative");
		modelElement.addChild(Element.ELEMENT, bindNode);
	}
	
	public static void fillRelationships(Patient patient, Element dataNode) throws Exception {
		Element patientRelationShipNode = XformBuilder.getElement(dataNode, NODE_PATIENT_RELATIONSHIP);
		if (patientRelationShipNode == null)
			return; //For does not need relationships.
			
		int index = 0;
		List<Relationship> relationships = Context.getPersonService().getRelationshipsByPerson(patient);
		for (Relationship relationship : relationships) {
			if (++index > 1)
				patientRelationShipNode = XformBuilder.createCopy(patientRelationShipNode, new ArrayList<String>());
			
			String relative;
			
			if (getPersonId(patient).equals(relationship.getPersonA().getPersonId())) {
				relative = relationship.getPersonB().getPersonName().toString() + " - "
				        + getPatientIdentifier(relationship.getPersonB());
			} else {
				relative = relationship.getPersonA().getPersonName().toString() + " - "
				        + getPatientIdentifier(relationship.getPersonA());
			}
			
			//This sets the display field and not really the hidden field for the personId/uuid
			//TODO Add logic to set the person id/uuid as the  value id the hidden field
			XformBuilder.setNodeValue(patientRelationShipNode, NODE_RELATIVE, relative);
			XformBuilder.setNodeValue(patientRelationShipNode, BIND_RELATIONSHIP_TYPE_ID, relationship.getRelationshipType()
			        .getRelationshipTypeId()
			        + ":"
			        + ((relationship.getPersonA().getPersonId().equals(patient.getPersonId())) ? "B" : "A"));
		}
	}
	
	private static Integer getPersonId(Patient patient) throws Exception {
		try {
			return patient.getPersonId();
		}
		catch (NoSuchMethodError ex) {
			Method method = patient.getClass().getMethod("getPerson", null);
			return ((Person) method.invoke(patient, null)).getPersonId();
		}
	}
	
	private static String getShortName(Patient patient) {
		if (patient.getGivenName() != null)
			return patient.getGivenName();
		
		return patient.getFamilyName();
	}
	
	private static String getPatientIdentifier(Person person) throws Exception {
		Patient patient = Context.getPatientService().getPatient(person.getPersonId());
		if (getPersonId(patient) == person.getPersonId())
			return patient.getPatientIdentifier().getIdentifier();
		
		return "";
	}
	
	private static void populateRelationshipTypes(Element controlNode) {
		List<RelationshipType> relationshipTypes = Context.getPersonService().getAllRelationshipTypes(false);
		for (RelationshipType type : relationshipTypes) {
			Element itemNode;
			//The value is of the form relationTypeId:A
			itemNode = createRelationTypeOptionNode(type, controlNode, true);
			controlNode.addChild(Element.ELEMENT, itemNode);
			
			//For relationships like sibling/sibling just display one option. Otherwise, we need 2
			//items for each side of the relationship, one for each side of the relationship so that 
			//the user can select which side the of the relationship the relative is i.e A Vs B
			if (!type.getbIsToA().equalsIgnoreCase(type.getaIsToB())) {
				itemNode = createRelationTypeOptionNode(type, controlNode, false);
				controlNode.addChild(Element.ELEMENT, itemNode);
			}
		}
	}
	
	/**
	 * Creates a node for a select option for the specified relation type
	 * 
	 * @param relationshipType the relationshipType object.
	 * @param controlNode the select node
	 * @param isA specifies which side of the relationship we are adding the option for
	 * @return the Element for the select option
	 */
	private static Element createRelationTypeOptionNode(RelationshipType relationshipType, Element controlNode, boolean isA) {
		Element itemNode = controlNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		itemNode.setName(XformBuilder.NODE_ITEM);
		
		Element node = itemNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		node.setName(XformBuilder.NODE_LABEL);
		node.addChild(Element.TEXT, "is the " + ((isA) ? relationshipType.getaIsToB() : relationshipType.getbIsToA()) + " ["
		        + relationshipType.getRelationshipTypeId() + "]");
		itemNode.addChild(Element.ELEMENT, node);
		
		node = itemNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		node.setName(XformBuilder.NODE_VALUE);
		node.addChild(Element.TEXT, relationshipType.getRelationshipTypeId() + ":" + ((isA) ? "A" : "B"));
		itemNode.addChild(Element.ELEMENT, node);
		return itemNode;
	}
}
