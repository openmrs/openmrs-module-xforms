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
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Advice for detecting when a new location has been added or an existing one deleted and then
 * refresh all affected xforms.
 * 
 * @since 4.0.3
 */
public class XformsLocationAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
	
	private static final long serialVersionUID = 1L;
	
	public boolean matches(Method method, Class targetClass) {
		if (method.getName().equals("saveLocation") || method.getName().equals("deleteLocation")
		        || method.getName().equals("purgeLocation") || method.getName().equals("retireLocation")) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public Advice getAdvice() {
		return new LocationAdvice();
	}
	
	private class LocationAdvice implements MethodInterceptor {
		
		public Object invoke(MethodInvocation invocation) throws Throwable {
			
			Location location = (Location) invocation.getArguments()[0];
			boolean isNewLocation = location.getLocationId() == null;
			
			String oldName = null;
			RefreshOperation operation = RefreshOperation.EDIT;
			
			if (!isNewLocation) {
				//TODO If name has changed, is there an easy way of getting the old value before saving to database?
				//oldName = XformBuilder.getLocationName(location);
				oldName = Context.getService(XformsService.class).getLocationName(location.getLocationId());
				if(oldName !=  null)
					oldName += " [" + location.getLocationId() + "]";
				else
					oldName = XformBuilder.getLocationName(location);
			}
			
			Object o = invocation.proceed();
			
			String methodName = invocation.getMethod().getName();
			if (methodName.equals("saveLocation")) {
				if (isNewLocation) {
					operation = RefreshOperation.ADD;
				}
			} else {
				operation = RefreshOperation.DELETE;
			}
			
			refreshXforms(operation, location, oldName);
			
			return o;
		}
	}
	
	/**
	 * Refreshes all xforms with the changes in a location.
	 * 
	 * @param operation the refresh operation.
	 * @param location the location.
	 * @param oldName the name the location had before editing.
	 * @throws Exception
	 */
	private void refreshXforms(RefreshOperation operation, Location location, String oldName) throws Exception {
		
		XformsService xformsService = Context.getService(XformsService.class);
		List<Xform> xforms = xformsService.getXforms();
		if (xforms == null)
			return; //No xforms in the database.
			
		//Loop through the xforms refreshing one by one
		for (Xform xform : xforms) {
			
			try{
				String xml = xform.getXformXml();
				Document doc = XformsUtil.fromString2Doc(xml);
				
				//Get all xf:select1 nodes in the xforms document.
				NodeList elements = doc.getDocumentElement().getElementsByTagName(
				    XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.CONTROL_SELECT1);
				
				//Look for the location node which has a bind attribute value of: encounter.location_id.
				for (int index = 0; index < elements.getLength(); index++) {
					Element element = (Element) elements.item(index);
					if ("encounter.location_id".equalsIgnoreCase(element.getAttribute(XformBuilder.ATTRIBUTE_BIND))) {
						refreshLocationWithId(operation, element, location, oldName, doc, xform, xformsService);
						break; //We can have only one location element, as of now.
					}
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
				continue; //failure for one form should not stop others from proceeding.
			}
		}
	}
	
	/**
	 * Refreshes a location in a given xforms select1 node.
	 * 
	 * @param operation the refresh operation.
	 * @param locationSelect1Element the location select1 node.
	 * @param location the location.
	 * @param oldName the location name before editing.
	 * @param doc the xforms document.
	 * @param xform the xform object.
	 * @param xformsService the xforms service.
	 * @throws Exception
	 */
	private void refreshLocationWithId(RefreshOperation operation, Element locationSelect1Element, Location location,
	                                   String oldName, Document doc, Xform xform, XformsService xformsService)
	                                                                                                          throws Exception {
		String sLocationId = location.getLocationId().toString();
		
		if (operation == RefreshOperation.DELETE || operation == RefreshOperation.EDIT) {
			
			//Get all xf:item nodes.
			NodeList elements = locationSelect1Element.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");
			
			boolean locationFound = false;
			
			//Look for an item node having an id attribute equal to the locationId.
			for (int index = 0; index < elements.getLength(); index++) {
				Element itemElement = (Element) elements.item(index);
				if (!sLocationId.equals(itemElement.getAttribute(XformBuilder.ATTRIBUTE_ID)))
					continue; //Not the location we are looking for.
					
				//If the location has been deleted, then remove their item node from the xforms document.
				if (operation == RefreshOperation.DELETE) {
					locationSelect1Element.removeChild(itemElement);
				} else {
					//New name for the location after editing.
					String newName = XformBuilder.getLocationName(location);
					
					//If name has not changed, then just do nothing.
					if (newName.equals(oldName))
						return;
					
					//If the location name has been edited, then change the xf:label node text.
					NodeList labels = itemElement.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "label");
					
					//If the existing xforms label is not the same as the previous location's name, then
					//do not change it, possibly the location wants the xforms value not to match the location's name.
					Element labelElement = (Element) labels.item(0);
					if (!oldName.equals(labelElement.getTextContent()))
						return;
					
					labelElement.setTextContent(newName);
				}
				
				locationFound = true;
				break;
			}
			
			//select1 node does not have the location to delete or edit.
			if (!locationFound) {
				if (operation == RefreshOperation.DELETE)
					return;
				
				addNewLocationNode(doc, locationSelect1Element, location);
			}
			
		} else {
			
			//Older versions of openmrs call AOP advisors more than once hence resulting into duplicates
			//if this check is not performed.
			if (locationExists(locationSelect1Element, sLocationId))
				return;
			
			//Add new location
			addNewLocationNode(doc, locationSelect1Element, location);
		}
		
		xform.setXformXml(XformsUtil.doc2String(doc));
		xformsService.saveXform(xform);
	}
	
	/**
	 * Adds a new location node to the xforms document.
	 * 
	 * @param doc the xforms document.
	 * @param locationSelect1Element the select1 element to add the location node.
	 * @param location the location to add.
	 * @param personId the person id represented by the location.
	 */
	private void addNewLocationNode(Document doc, Element locationSelect1Element, Location location) {
		Element itemNode = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_ITEM);
		itemNode.setAttribute(XformBuilder.ATTRIBUTE_ID, location.getLocationId().toString());
		
		Element node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_LABEL);
		node.setTextContent(XformBuilder.getLocationName(location));
		itemNode.appendChild(node);
		
		node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_VALUE);
		node.setTextContent(location.getLocationId().toString());
		itemNode.appendChild(node);
		
		locationSelect1Element.appendChild(itemNode);
	}
	
	/**
	 * Checks if a location item node exists in a select1 node of an xforms document.
	 * 
	 * @param locationSelect1Element the select1 node.
	 * @param locationId the location id string.
	 * @return true if exists, else false.
	 */
	private boolean locationExists(Element locationSelect1Element, String locationId) {
		//Get all xf:item nodes.
		NodeList elements = locationSelect1Element.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");
		
		//Look for an item node having an id attribute equal to the locationId.
		for (int index = 0; index < elements.getLength(); index++) {
			Element itemElement = (Element) elements.item(index);
			if (locationId.equals(itemElement.getAttribute(XformBuilder.ATTRIBUTE_ID)))
				return true;
		}
		
		return false;
	}
}
