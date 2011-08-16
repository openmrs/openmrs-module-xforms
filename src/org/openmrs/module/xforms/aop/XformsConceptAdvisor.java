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
package org.openmrs.module.xforms.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormUtil;
import org.springframework.aop.AfterReturningAdvice;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Advice for detecting when a coded concept has any newly added answers or existing ones deleted
 * and then refresh all affected xforms.
 * 
 * @since 4.0.3
 */
public class XformsConceptAdvisor implements AfterReturningAdvice {
	
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		if (method.getName().equals("saveConcept")) {
			
			Concept concept = (Concept) args[0];
			if (!concept.getDatatype().isCoded()){
				
				String newName = concept.getName().getName();
				String oldName = Context.getService(XformsService.class).getConceptName(concept.getConceptId(), Context.getLocale().getLanguage());
				if(!newName.equals(oldName)){
					refreshConceptName(concept, newName, oldName);
				}
				
				return; //For the rest, we only deal with coded concepts.
			}
			
			String conceptId = concept.getConceptId().toString();
			
			XformsService xformsService = Context.getService(XformsService.class);
			List<Xform> xforms = xformsService.getXforms();
			if (xforms == null)
				return;
			
			//Loop through the xforms refreshing one by one
			for (Xform xform : xforms) {
				
				try{
					String xml = xform.getXformXml();
					Document doc = XformsUtil.fromString2Doc(xml);
					
					//Get all xf:select1 nodes in the xforms document.
					NodeList elements = doc.getDocumentElement().getElementsByTagName(
						XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.CONTROL_SELECT1);
					
					//Look for the node which has a concept_id attribute value of conceptId
					for (int index = 0; index < elements.getLength(); index++) {
						Element element = (Element) elements.item(index);
						if (conceptId.equalsIgnoreCase(element.getAttribute(XformBuilder.ATTRIBUTE_CONCEPT_ID))) {
							refreshConceptWithId(element, concept, doc, xform, xformsService);
							break;
						}
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					continue; //failure for one form should not stop others from proceeding.
				}
			}
			
		}
	}
	
	/**
	 * Refreshes a coded concept in a given xforms select1 node. Where refreshing is simply adding
	 * newly added answers and deleting those that have been removed from the coded concept.
	 * 
	 * @param conceptSelect1Element the concept's select1 node.
	 * @param concept the coded concept.
	 * @param doc the xforms document.
	 * @param xform the xforms object.
	 * @param xformsService the xforms service.
	 */
	private void refreshConceptWithId(Element conceptSelect1Element, Concept concept, Document doc, Xform xform,
	                                  XformsService xformsService) {
		
		boolean xformModified = false;
		List<String> xformConceptAnswers = new ArrayList<String>();
		
		Collection<ConceptAnswer> conceptAnswers = concept.getAnswers();
		
		//Remove all deleted answers from the xform.
		NodeList elements = conceptSelect1Element.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");
		for (int index = 0; index < elements.getLength(); index++) {
			Element itemElement = (Element) elements.item(index);
			
			String conceptId = itemElement.getAttribute(XformBuilder.ATTRIBUTE_CONCEPT_ID);
			
			//If concept is not in the answers, it must have been deleted and so we need to remove it from the xform.
			if (!conceptAnswerInCollection(conceptAnswers, conceptId)) {
				conceptSelect1Element.removeChild(itemElement);
				xformModified = true;
				index--;
			} else
				xformConceptAnswers.add(conceptId);
		}
		
		//Add all new answers to the xform.
		for (ConceptAnswer conceptAnswer : conceptAnswers) {
			Concept answerConcept = conceptAnswer.getAnswerConcept();
			if (answerConcept == null)
				continue;
			
			//Check if the xform already has this answer and add it if it does not exist.
			String conceptId = answerConcept.getConceptId().toString();
			if (!xformConceptAnswers.contains(conceptId)) {
				addNewConceptAnswer(doc, answerConcept, conceptSelect1Element);
				xformModified = true;
			}
		}
		
		//Update name if changed.
		String newName = concept.getName().getName();
		String oldName = Context.getService(XformsService.class).getConceptName(concept.getConceptId(), Context.getLocale().getLanguage());
		if(!newName.equals(oldName)){
			refreshConceptName(concept, newName, oldName, conceptSelect1Element);
			xformModified = true;
		}
		
		//Only save if there are changes to the xforms document.
		if (xformModified) {
			xform.setXformXml(XformsUtil.doc2String(doc));
			xformsService.saveXform(xform);
		}
	}
	
	/**
	 * Checks if a given conceptId is in a concept answers collection.
	 * 
	 * @param conceptAnswers the concept answers collection.
	 * @param conceptId the concept id.
	 * @return true if it exists, else false.
	 */
	private boolean conceptAnswerInCollection(Collection<ConceptAnswer> conceptAnswers, String conceptId) {
		for (ConceptAnswer conceptAnswer : conceptAnswers) {
			Concept answerConcept = conceptAnswer.getAnswerConcept();
			if (answerConcept == null)
				continue;
			
			if (answerConcept.getConceptId().toString().equals(conceptId))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Adds a new concept answer node to its corresponding coded concept select1 node in as xforms
	 * document.
	 * 
	 * @param doc the xforms document.
	 * @param concept the concept answer whose xforms node to add.
	 * @param conceptSelect1Element the select1 element for the coded concept that has the answer we
	 *            are adding.
	 */
	private void addNewConceptAnswer(Document doc, Concept concept, Element conceptSelect1Element) {
		String hl7Name = StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, Context.getLocale()));
		
		Element itemNode = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_ITEM);
		itemNode.setAttribute(XformBuilder.ATTRIBUTE_CONCEPT_ID, concept.getConceptId().toString());
		
		Element node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_LABEL);
		node.setTextContent(XformBuilder.getConceptName(hl7Name));
		itemNode.appendChild(node);
		
		node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_VALUE);
		node.setTextContent(hl7Name);
		itemNode.appendChild(node);
		
		conceptSelect1Element.appendChild(itemNode);
	}
	
	
	private void refreshConceptName(Concept concept, String newName, String oldName) throws Exception {
		XformsService xformsService = Context.getService(XformsService.class);
		List<Xform> xforms = xformsService.getXforms();
		if (xforms == null)
			return;
		
		String sConceptId = concept.getId().toString();
		
		//Loop through the xforms refreshing one by one
		for (Xform xform : xforms) {
			
			try{
				String xml = xform.getXformXml();
				Document doc = XformsUtil.fromString2Doc(xml);
				
				//Get all xf:item nodes in the xforms document.
				NodeList elements = doc.getDocumentElement().getElementsByTagName(
					XformBuilder.PREFIX_XFORMS + ":" + "item");
				
				boolean xformModified = false;
				
				//Look for the node which has a concept_id attribute value of conceptId
				for (int index = 0; index < elements.getLength(); index++) {
					Element element = (Element) elements.item(index);
					
					if (sConceptId.equalsIgnoreCase(element.getAttribute(XformBuilder.ATTRIBUTE_CONCEPT_ID))) {
						boolean ret = refreshConceptName(concept, newName, oldName, element);
						if(ret){
							xformModified = true;
						}
					}
				}
				
				if (xformModified) {
					xform.setXformXml(XformsUtil.doc2String(doc));
					xformsService.saveXform(xform);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
				continue; //failure for one form should not stop others from proceeding.
			}
		}
	}
	
	private boolean refreshConceptName(Concept concept, String newName, String oldName, Element parentElement){
		NodeList elements = parentElement.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "label");
		if(elements.getLength() > 0) {
			Element labelElement = (Element) elements.item(0); //We deal with only the first label node.
			
			//Assuming label element for the select1 node comes first.
			if (oldName.equals(labelElement.getTextContent())){
				labelElement.setTextContent(newName);
				setItemValueText(parentElement, StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, Context.getLocale())));
				return true;
			}
		}
		
		return false;
	}
	
	private void setItemValueText(Element parentElement, String valueText){
		NodeList elements = parentElement.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "value");
		if(elements.getLength() > 0) {
			Element valueElement = (Element) elements.item(0);
			valueElement.setTextContent(valueText);
		}
	}
}
