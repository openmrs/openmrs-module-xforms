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
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Advice for detecting when a new provider has been added or an existing one deleted and then
 * refresh all affected xforms. Where a refresh may mean: (1) Removing a deleted provider or a
 * person who no longer has a provider role. (2) Adding a new provider who has just been added as a
 * new person or an existing one who has just got the provider role. (3) Changing the name of an
 * edited provider name.
 * 
 * @since 4.0.3
 */
public class XformsProviderAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
	
	private static final long serialVersionUID = 1L;
	
	public boolean matches(Method method, Class targetClass) {
		if (method.getName().equals("saveUser") || method.getName().equals("deleteUser")
		        || method.getName().equals("purgeUser") || method.getName().equals("voidUser")
		        || method.getName().equals("retireUser")) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public Advice getAdvice() {
		return new ProviderAdvice();
	}
	
	private class ProviderAdvice implements MethodInterceptor {
		
		public Object invoke(MethodInvocation invocation) throws Throwable {
			
			User user = (User) invocation.getArguments()[0];
			boolean isNewUser = user.getUserId() == null;
			
			String oldName = null;
			RefreshOperation operation = RefreshOperation.EDIT;
			
			if (!isNewUser) {
				//TODO If name has changed, is there an easy way of getting the old value before saving to database?
				//oldName = XformBuilder.getProviderName(user, XformsUtil.getPersonId(user));
				Integer personId = XformsUtil.getPersonId(user);
				oldName = Context.getService(XformsService.class).getPersonName(personId);
				if(oldName != null)
					oldName += " [" + personId + "]";
				else
					oldName = XformBuilder.getProviderName(user, personId);
			}
			
			Object o = invocation.proceed();
			
			if (user.hasRole(OpenmrsConstants.PROVIDER_ROLE)) {
				String methodName = invocation.getMethod().getName();
				if (methodName.equals("saveUser")) {
					if (isNewUser) {
						operation = RefreshOperation.ADD;
					}
				} else {
					operation = RefreshOperation.DELETE;
				}
			} else {
				//The user may have had the provider role and it could now be removed from them.
				operation = RefreshOperation.DELETE;
			}
			
			refreshXforms(operation, user, oldName);
			
			return o;
		}
	}
	
	/**
	 * Refreshes all xforms with the changes in a provider.
	 * 
	 * @param operation the refresh operation.
	 * @param user the provider.
	 * @param oldName the name the provider had before editing.
	 * @throws Exception
	 */
	private void refreshXforms(RefreshOperation operation, User user, String oldName) throws Exception {
		
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
				
				//Look for the provider node which has a bind attribute value of: encounter.provider_id.
				for (int index = 0; index < elements.getLength(); index++) {
					Element element = (Element) elements.item(index);
					if ("encounter.provider_id".equalsIgnoreCase(element.getAttribute(XformBuilder.ATTRIBUTE_BIND))) {
						refreshProviderWithId(operation, element, user, oldName, doc, xform, xformsService);
						break; //We can have only one provider element, as of now.
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
	 * Refreshes a provider in a given xforms select1 node.
	 * 
	 * @param operation the refresh operation.
	 * @param providerSelect1Element the provider select1 node.
	 * @param user the provider.
	 * @param oldName the provider name before editing.
	 * @param doc the xforms document.
	 * @param xform the xform object.
	 * @param xformsService the xforms service.
	 * @throws Exception
	 */
	private void refreshProviderWithId(RefreshOperation operation, Element providerSelect1Element, User user,
	                                   String oldName, Document doc, Xform xform, XformsService xformsService)
	                                                                                                          throws Exception {
		Integer personId = XformsUtil.getPersonId(user);
		String sPersonId = personId.toString();
		
		if (operation == RefreshOperation.DELETE || operation == RefreshOperation.EDIT) {
			
			//Get all xf:item nodes.
			NodeList elements = providerSelect1Element.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");
			
			boolean providerFound = false;
			
			//Look for an item node having an id attribute equal to the userId.
			for (int index = 0; index < elements.getLength(); index++) {
				Element itemElement = (Element) elements.item(index);
				if (!sPersonId.equals(itemElement.getAttribute(XformBuilder.ATTRIBUTE_ID)))
					continue; //Not the provider we are looking for.
					
				//If the user has been deleted, then remove their item node from the xforms document.
				if (operation == RefreshOperation.DELETE) {
					providerSelect1Element.removeChild(itemElement);
				} else {
					//New name for the provider after editing.
					String newName = XformBuilder.getProviderName(user, personId);
					
					//If name has not changed, then just do nothing.
					if (newName.equals(oldName))
						return;
					
					//If the user name has been edited, then change the xf:label node text.
					NodeList labels = itemElement.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "label");
					
					//If the existing xforms label is not the same as the previous user's name, then
					//do not change it, possibly the user wants the xforms value not to match the user's name.
					Element labelElement = (Element) labels.item(0);
					if (!oldName.equals(labelElement.getTextContent()))
						return;
					
					labelElement.setTextContent(newName);
				}
				
				providerFound = true;
				break;
			}
			
			//select1 node does not have the provider to delete or edit.
			if (!providerFound) {
				if (operation == RefreshOperation.DELETE)
					return;
				
				//This must be a person who has just got a provider role which he or she did not have before.
				addNewProviderNode(doc, providerSelect1Element, user, personId);
			}
			
		} else {
			
			//Older versions of openmrs call AOP advisors more than once hence resulting into duplicates
			//if this check is not performed.
			if (providerExists(providerSelect1Element, sPersonId))
				return;
			
			//Add new provider
			addNewProviderNode(doc, providerSelect1Element, user, personId);
		}
		
		xform.setXformXml(XformsUtil.doc2String(doc));
		xformsService.saveXform(xform);
	}
	
	/**
	 * Adds a new provider node to the xforms docuement.
	 * 
	 * @param doc the xforms document.
	 * @param providerSelect1Element the select1 element to add the provider node.
	 * @param user the provider to add.
	 * @param personId the person id represented by the provider.
	 */
	private void addNewProviderNode(Document doc, Element providerSelect1Element, User user, Integer personId) {
		Element itemNode = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_ITEM);
		itemNode.setAttribute(XformBuilder.ATTRIBUTE_ID, personId.toString());
		
		Element node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_LABEL);
		node.setTextContent(XformBuilder.getProviderName(user, personId));
		itemNode.appendChild(node);
		
		node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":" + XformBuilder.NODE_VALUE);
		node.setTextContent(personId.toString());
		itemNode.appendChild(node);
		
		providerSelect1Element.appendChild(itemNode);
	}
	
	/**
	 * Checks if a provider item node exists in a select1 node of an xforms document.
	 * 
	 * @param providerSelect1Element the select1 node.
	 * @param personId the person id string for the provider.
	 * @return true if exists, else false.
	 */
	private boolean providerExists(Element providerSelect1Element, String personId) {
		//Get all xf:item nodes.
		NodeList elements = providerSelect1Element.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");
		
		//Look for an item node having an id attribute equal to the personId.
		for (int index = 0; index < elements.getLength(); index++) {
			Element itemElement = (Element) elements.item(index);
			if (personId.equals(itemElement.getAttribute(XformBuilder.ATTRIBUTE_ID)))
				return true;
		}
		
		return false;
	}
}
