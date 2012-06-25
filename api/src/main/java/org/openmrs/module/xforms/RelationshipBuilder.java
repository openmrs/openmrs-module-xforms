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
import org.openmrs.api.context.Context;

/**
 * Utility for building relationship nodes in xforms.
 * 
 * @since 4.0.3
 */
public class RelationshipBuilder {
	
	public static final String NODE_RELATIVE = "xforms_relative";
	
	public static final String NODE_RELATIONSHIP = "xforms_relationship";
	
	public static final String BIND_PATIENT_RELATIONSHIP = "patient.patient_relationship";
	
	public static final String BIND_RELATIVE = "patient_relationship.relative";
	
	public static final String BIND_RELATIONSHIP = "patient_relationship.relationship";
	
	public static final String NODE_PATIENT_RELATIONSHIP = "patient_relationship";
	
	public static void build(Element modelElement, Element bodyNode, Element dataNode) {
		//Create the parent repeat ui node.
		Element groupNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		groupNode.setName(XformBuilder.NODE_GROUP);
		groupNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, BIND_RELATIVE);
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
		
		//Add relative data node.
		Element node = dataNode.createElement(null, null);
		node.setName(NODE_RELATIVE);
		dataNode.addChild(Element.ELEMENT, node);
		
		//Add relationship data node.
		node = dataNode.createElement(null, null);
		node.setName(NODE_RELATIONSHIP);
		dataNode.addChild(Element.ELEMENT, node);
		
		//Create relative input node.
		Element inputNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		inputNode.setName(XformBuilder.CONTROL_INPUT);
		inputNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, BIND_RELATIVE);
		repeatNode.addChild(Element.ELEMENT, inputNode);
		
		//Create relative label.
		labelNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		labelNode.setName(XformBuilder.NODE_LABEL);
		labelNode.addChild(Element.TEXT, "RELATIVE");
		inputNode.addChild(Element.ELEMENT, labelNode);
		
		//Create relationship input node.
		inputNode = bodyNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		inputNode.setName(XformBuilder.CONTROL_INPUT);
		inputNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, BIND_RELATIONSHIP);
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
		
		//Create bind node for relative.
		bindNode = modelElement.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		bindNode.setName(XformBuilder.NODE_BIND);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_ID, BIND_RELATIVE);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_NODESET, "/form/patient/patient_relationship/" + NODE_RELATIVE);
		modelElement.addChild(Element.ELEMENT, bindNode);
		
		//Create bind node for relationship.
		bindNode = modelElement.createElement(XformBuilder.NAMESPACE_XFORMS, null);
		bindNode.setName(XformBuilder.NODE_BIND);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_ID, BIND_RELATIONSHIP);
		bindNode.setAttribute(null, XformBuilder.ATTRIBUTE_NODESET, "/form/patient/patient_relationship/"
		        + NODE_RELATIONSHIP);
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
			
			String relative = relationship.getPersonA().getPersonName().toString() + " - " + getPatientIdentifier(relationship.getPersonA());
			String sSelationship = relationship.getRelationshipType().getaIsToB();
			sSelationship += " (" + getShortName(patient) + " is " + relationship.getRelationshipType().getbIsToA() + ")";
			
			if(getPersonId(patient) == relationship.getPersonA().getPersonId()){
				relative = relationship.getPersonB().getPersonName().toString() + " - " + getPatientIdentifier(relationship.getPersonB());
				sSelationship = relationship.getRelationshipType().getbIsToA();
				sSelationship += " (" + getShortName(patient) + " is " + relationship.getRelationshipType().getaIsToB() + ")";
			}
			
			XformBuilder.setNodeValue(patientRelationShipNode, NODE_RELATIVE, relative);
			XformBuilder.setNodeValue(patientRelationShipNode, NODE_RELATIONSHIP, sSelationship);
		}
	}
	
	private static Integer getPersonId(Patient patient) throws Exception {
		try{
			return patient.getPersonId();
		}
		catch(NoSuchMethodError ex){
			Method method = patient.getClass().getMethod("getPerson", null);
			return ((Person)method.invoke(patient, null)).getPersonId();
		}
	}
	
	private static String getShortName(Patient patient){
		if(patient.getGivenName() != null)
			return patient.getGivenName();
		
		return patient.getFamilyName();
	}
	
	private static String getPatientIdentifier(Person person) throws Exception {
		Patient patient = Context.getPatientService().getPatient(person.getPersonId());
		if(getPersonId(patient) == person.getPersonId())
			return patient.getPatientIdentifier().getIdentifier();
		
		return "";
	}
}
