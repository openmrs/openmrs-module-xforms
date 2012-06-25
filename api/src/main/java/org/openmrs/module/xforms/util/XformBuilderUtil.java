package org.openmrs.module.xforms.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.kxml2.kdom.Element;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;


public class XformBuilderUtil {

	public static final String NODE_NAME_PROVIDERS = "providers";
	public static final String NODE_NAME_ENCOUNTER_ROLE = "encounter_role";
	public static final String NODE_NAME_ENCOUNTER_ROLE_ID = "encounter_role_id";
	public static final String NODE_NAME_PROVIDER_ID = "provider_id";
	
	/**
	 * Populates a UI control node with providers using the 1.9 and above API.
	 */
	public static boolean populateProviders19(Element formNode, Element modelNode, Element groupNode) {
		
		try {
			String name = NODE_NAME_PROVIDERS;
			
			//add the model node
			Element dataNode = formNode.createElement(null, null);
			dataNode.setName(name);
			//formNode.addChild(Element.ELEMENT, dataNode);
			
			//add the model binding
			Element bindingNode = modelNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			bindingNode.setName(XformBuilder.NODE_BIND);
			bindingNode.setAttribute(null, XformBuilder.ATTRIBUTE_ID, name);
			bindingNode.setAttribute(null, XformBuilder.ATTRIBUTE_NODESET, "/" + XformBuilder.NODE_FORM + "/" + name);
			bindingNode.setAttribute(null, XformBuilder.ATTRIBUTE_TYPE, XformBuilder.DATA_TYPE_TEXT);
			
			//modelNode.addChild(Element.ELEMENT, bindingNode);
			
			
			//Create repeat group node
			Element repeatGroupNode = groupNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			repeatGroupNode.setName(XformBuilder.NODE_GROUP);
			//groupNode.addChild(Element.ELEMENT, repeatGroupNode);
			
			//add the repeat group label
			Element labelNode = repeatGroupNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			labelNode.setName(XformBuilder.NODE_LABEL);
			labelNode.addChild(Element.TEXT, "Providers");
			repeatGroupNode.addChild(Element.ELEMENT, labelNode);
			
			//add repeat node.
			Element repeatNode = repeatGroupNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			repeatNode.setName(XformBuilder.CONTROL_REPEAT);
			repeatNode.setAttribute(null, XformBuilder.ATTRIBUTE_BIND, name);
			repeatGroupNode.addChild(Element.ELEMENT, repeatNode);
			
			List<String> itemList = new ArrayList<String>();
			List<String> itemValueList = new ArrayList<String>();
			populateEncounterRoles(itemList, itemValueList);
			
			String[] items, itemValues;
			if (itemList.size() > 0) {
				items = itemList.toArray(new String[] {});
				itemValues = itemValueList.toArray(new String[] {});

				XformBuilder.addPatientNode(dataNode, modelNode, repeatNode, NODE_NAME_ENCOUNTER_ROLE_ID, XformBuilder.DATA_TYPE_INT, "Encounter Role",
				    "The provider's encounter role", true, false, XformBuilder.CONTROL_SELECT1, items, itemValues, true,
				    "/" + XformBuilder.NODE_FORM + "/" + name + "/encounter_role_id");
			}
			
			itemList.clear();
			itemValueList.clear();
			populateProviders(itemList, itemValueList);
			
			if (itemList.size() > 0) {
				items = itemList.toArray(new String[] {});
				itemValues = itemValueList.toArray(new String[] {});
				
				XformBuilder.addPatientNode(dataNode, modelNode, repeatNode, NODE_NAME_PROVIDER_ID, XformBuilder.DATA_TYPE_INT, "Provider",
				    "The provider", true, false, XformBuilder.CONTROL_SELECT1, items, itemValues, true,
				    "/" + XformBuilder.NODE_FORM + "/" + name + "/provider_id");
			}
			
			formNode.addChild(Element.ELEMENT, dataNode);
			modelNode.addChild(Element.ELEMENT, bindingNode);
			groupNode.addChild(Element.ELEMENT, repeatGroupNode);

			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	private static void populateProviders(List<String> items, List<String> itemValues) throws Exception {
		
		Method method = Context.class.getMethod("getProviderService", null);
		Object service = method.invoke(null);
		
		method = service.getClass().getMethod("getAllProviders", null);
		Object providers = method.invoke(service, null);
		
		method = providers.getClass().getMethod("size", null);
		int size = (Integer)method.invoke(providers, null);
		
		for(int index = 0; index < size; index++){
			Method meth = providers.getClass().getMethod("get", int.class);
			Object provider = meth.invoke(providers, index);
			
			meth = provider.getClass().getMethod("getName", null);
			String name = (String)meth.invoke(provider, null);
			if(name == null){
				meth = provider.getClass().getMethod("getPerson", null);
				Person person = (Person)meth.invoke(provider, null);
				if(person != null && person.getPersonName() != null)
					name = person.getPersonName().toString();
			}
			
			meth = provider.getClass().getMethod("getIdentifier", null);
			String identifier = (String)meth.invoke(provider, null);
			
			meth = provider.getClass().getMethod("getProviderId", null);
			Integer providerId = (Integer)meth.invoke(provider, null);
			
			items.add(name + " [" + identifier + "]");
			itemValues.add(providerId.toString());
		}
	}
	
	private static void populateEncounterRoles(List<String> items, List<String> itemValues) throws Exception {
		
		Method method = Context.class.getMethod("getEncounterService", null);
		Object service = method.invoke(null);
		
		method = service.getClass().getMethod("getAllEncounterRoles", boolean.class);
		Object encounterRoles = method.invoke(service, new Object[] {false});
		
		method = encounterRoles.getClass().getMethod("size", null);
		int size = (Integer)method.invoke(encounterRoles, null);
		
		for(int index = 0; index < size; index++){
			Method meth = encounterRoles.getClass().getMethod("get", int.class);
			Object encounterRole = meth.invoke(encounterRoles, index);
			
			meth = encounterRole.getClass().getMethod("getName", null);
			String name = (String)meth.invoke(encounterRole, null);
			
			meth = encounterRole.getClass().getMethod("getEncounterRoleId", null);
			Integer encounterRoleId = (Integer)meth.invoke(encounterRole, null);
			
			items.add(name + " [" + encounterRoleId.toString() + "]");
			itemValues.add(encounterRoleId.toString());
		}
	}
	
	public static void populateProviders(Element controlNode) throws Exception {
		
		Method method = Context.class.getMethod("getProviderService", null);
		Object service = method.invoke(null);
		
		method = service.getClass().getMethod("getAllProviders", null);
		Object providers = method.invoke(service, null);
		
		method = providers.getClass().getMethod("size", null);
		int size = (Integer)method.invoke(providers, null);
		
		for(int index = 0; index < size; index++){
			Method meth = providers.getClass().getMethod("get", int.class);
			Object provider = meth.invoke(providers, index);
			
			meth = provider.getClass().getMethod("getName", null);
			String name = (String)meth.invoke(provider, null);
			if(name == null){
				meth = provider.getClass().getMethod("getPerson", null);
				Person person = (Person)meth.invoke(provider, null);
				if(person != null && person.getPersonName() != null)
					name = person.getPersonName().toString();
			}
			
			meth = provider.getClass().getMethod("getIdentifier", null);
			String identifier = (String)meth.invoke(provider, null);
			
			meth = provider.getClass().getMethod("getProviderId", null);
			Integer providerId = (Integer)meth.invoke(provider, null);
			
			
			Element itemNode = controlNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			itemNode.setName(XformBuilder.NODE_ITEM);
			
			Element node = itemNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			node.setName(XformBuilder.NODE_LABEL);
			node.addChild(Element.TEXT, name + " [" + identifier + "]");
			itemNode.addChild(Element.ELEMENT, node);
			
			node = itemNode.createElement(XformBuilder.NAMESPACE_XFORMS, null);
			node.setName(XformBuilder.NODE_VALUE);
			node.addChild(Element.TEXT, providerId.toString());
			itemNode.addChild(Element.ELEMENT, node);
			
			controlNode.addChild(Element.ELEMENT, itemNode);
		}
	}
}
