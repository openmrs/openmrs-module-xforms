package org.openmrs.module.xforms.web;

import java.lang.reflect.Method;
import java.util.List;

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.util.XformsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DWRXformsRefreshOperationImpl {
	public static void refreshXforms() throws Exception {
		// Get a list of xforms in the database
		XformsService xformsService = Context.getService(XformsService.class);
		List<Xform> xforms = xformsService.getXforms();

		if (xforms == null)
			return; // No xforms in the database.

		// Get a list of all provider in the database
		Method method = Context.class.getMethod("getProviderService", null);
		Object service = method.invoke(null);

		method = service.getClass().getMethod("getAllProviders", null);
		Object providers = method.invoke(service, null);

		method = providers.getClass().getMethod("size", null);
		int size = (Integer) method.invoke(providers, null);

		// For each provider, get it identifier, name and personId reference
		// then go through the list of xforms
		// check if this provider has already exist in its list of providers
		// or not, if not then add it into the list
		for (int idx = 0; idx < size; idx++) {
			Method meth = providers.getClass().getMethod("get", int.class);
			Object provider = meth.invoke(providers, idx);

			meth = provider.getClass().getMethod("getName", null);
			String name = (String) meth.invoke(provider, null);
			Person person = (Person) meth.invoke(provider, null);
			if (name == null) {
				meth = provider.getClass().getMethod("getPerson", null);
				if (person != null && person.getPersonName() != null)
					name = person.getPersonName().toString();
			}

			meth = provider.getClass().getMethod("getIdentifier", null);
			String identifier = (String) meth.invoke(provider, null);

			meth = provider.getClass().getMethod("getProviderId", null);
			Integer providerId = (Integer) meth.invoke(provider, null);

			// Loop through the xforms refreshing one by one
			for (Xform xform : xforms) {

				try {
					String xml = xform.getXformXml();
					Document doc = XformsUtil.fromString2Doc(xml);

					// Get all xf:select1 nodes in the xforms document.
					NodeList elements = doc.getDocumentElement()
							.getElementsByTagName(
									XformBuilder.PREFIX_XFORMS + ":"
											+ XformBuilder.CONTROL_SELECT1);

					// Look for the provider node which has a bind attribute
					// value of: encounter.provider_id.
					for (int index = 0; index < elements.getLength(); index++) {
						Element element = (Element) elements.item(index);
						Integer personId = (person == null) ? providerId
								: person.getId();

						if ("encounter.provider_id".equalsIgnoreCase(element
								.getAttribute(XformBuilder.ATTRIBUTE_BIND))) {
							refreshProvider(name, personId, element, doc,
									xform, xformsService);
							break; // We can have only one provider element, as
									// of now.
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					continue; // failure for one form should not stop others
								// from proceeding.
				}
			}
		}

		// This is where the code went wrong as I pointed out previously
		// List<Provider> providerService =
		// Context.getProviderService().getAllProviders();

	}

	public static void refreshProvider(String providerName, Integer personId,
			Element providerSelect1Element, Document doc, Xform xform,
			XformsService xformsService) throws Exception {
		String sPersonId = personId.toString();
		NodeList elements = providerSelect1Element
				.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");

		if (providerExists(providerSelect1Element, sPersonId))
			return;

		// Add new provider
		addNewProviderNode(providerName, doc, providerSelect1Element, personId);
		xform.setXformXml(XformsUtil.doc2String(doc));
		xformsService.saveXform(xform);

		// Integer personId = XformsUtil.getPersonId(user);
		// String sPersonId = personId.toString();
		// //Get all xf:item nodes.
		// NodeList elements =
		// providerSelect1Element.getElementsByTagName(XformBuilder.PREFIX_XFORMS
		// + ":" + "item");
		//
		// boolean providerFound = false;
		//
		// //Look for an item node having an id attribute equal to the userId.
		// for (int index = 0; index < elements.getLength(); index++) {
		// Element itemElement = (Element) elements.item(index);
		// if
		// (!sPersonId.equals(itemElement.getAttribute(XformBuilder.ATTRIBUTE_ID)))
		// continue; //Not the provider we are looking for.
		//
		// //If the user has been deleted, then remove their item node from the
		// xforms document.
		//
		// //New name for the provider after editing.
		// String newName = XformBuilder.getProviderName(user, personId);
		//
		// //If name has not changed, then just do nothing.
		// if (newName.equals(oldName))
		// return;
		//
		// //If the user name has been edited, then change the xf:label node
		// text.
		// NodeList labels =
		// itemElement.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" +
		// "label");
		//
		// //If the existing xforms label is not the same as the previous user's
		// name, then
		// //do not change it, possibly the user wants the xforms value not to
		// match the user's name.
		// Element labelElement = (Element) labels.item(0);
		// if (!oldName.equals(labelElement.getTextContent()))
		// return;
		//
		// labelElement.setTextContent(newName);
		//
		//
		// providerFound = true;
		// break;

		// //select1 node does not have the provider to delete or edit.
		// if (!providerFound) {
		// if (operation == RefreshOperation.DELETE)
		// return;
		//
		// //This must be a person who has just got a provider role which he or
		// she did not have before.
		// addNewProviderNode(doc, providerSelect1Element, user, personId);
		// }
		//
		// } else {
		//
		// //Older versions of openmrs call AOP advisors more than once hence
		// resulting into duplicates
		// //if this check is not performed.
		// if (providerExists(providerSelect1Element, sPersonId))
		// return;
		//
		// //Add new provider
		// addNewProviderNode(doc, providerSelect1Element, user, personId);
		// }
		//
		// xform.setXformXml(XformsUtil.doc2String(doc));
		// xformsService.saveXform(xform);
	}

	/**
	 * Adds a new provider node to the xforms docuement.
	 * 
	 * @param doc
	 *            the xforms document.
	 * @param providerSelect1Element
	 *            the select1 element to add the provider node.
	 * @param user
	 *            the provider to add.
	 * @param personId
	 *            the person id represented by the provider.
	 */
	private static void addNewProviderNode(String providerName, Document doc,
			Element providerSelect1Element, Integer personId) {
		Element itemNode = doc.createElement(XformBuilder.PREFIX_XFORMS + ":"
				+ XformBuilder.NODE_ITEM);
		itemNode.setAttribute(XformBuilder.ATTRIBUTE_ID, personId.toString());

		Element node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":"
				+ XformBuilder.NODE_LABEL);
		node.setTextContent(providerName);
		itemNode.appendChild(node);

		node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":"
				+ XformBuilder.NODE_VALUE);
		node.setTextContent(personId.toString());
		itemNode.appendChild(node);

		providerSelect1Element.appendChild(itemNode);
	}

	/**
	 * Checks if a provider item node exists in a select1 node of an xforms
	 * document.
	 * 
	 * @param providerSelect1Element
	 *            the select1 node.
	 * @param personId
	 *            the person id string for the provider.
	 * @return true if exists, else false.
	 */
	private static boolean providerExists(Element providerSelect1Element,
			String personId) {
		// Get all xf:item nodes.
		NodeList elements = providerSelect1Element
				.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");

		// Look for an item node having an id attribute equal to the personId.
		for (int index = 0; index < elements.getLength(); index++) {
			Element itemElement = (Element) elements.item(index);
			if (personId.equals(itemElement
					.getAttribute(XformBuilder.ATTRIBUTE_ID)))
				return true;
		}

		return false;
	}
}
