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
			meth = provider.getClass().getMethod("getPerson", null);
			Person person = (Person) meth.invoke(provider, null);
			if (name == null) {
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
						Integer personId = (person == null) ? null
								: person.getId();

						if ("encounter.provider_id".equalsIgnoreCase(element
								.getAttribute(XformBuilder.ATTRIBUTE_BIND))) {
							refreshProvider(name, personId, element, doc,
									xform, xformsService);
							
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					continue; // failure for one form should not stop others
								// from proceeding.
				}
			}
		}
	}

	public static void refreshProvider(String providerName, Integer personId,
			Element providerSelect1Element, Document doc, Xform xform,
			XformsService xformsService) throws Exception {
		String sPersonId = (personId == null) ? null : personId.toString();
		NodeList elements = providerSelect1Element
				.getElementsByTagName(XformBuilder.PREFIX_XFORMS + ":" + "item");

		if (providerExists(providerSelect1Element, sPersonId))
			return;

		// Add new provider
		addNewProviderNode(providerName, doc, providerSelect1Element, personId);
		xform.setXformXml(XformsUtil.doc2String(doc));
		xformsService.saveXform(xform);
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
		String spersonId = (personId == null) ? null : personId.toString();
		itemNode.setAttribute(XformBuilder.ATTRIBUTE_ID, spersonId);

		Element node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":"
				+ XformBuilder.NODE_LABEL);
		node.setTextContent(providerName);
		itemNode.appendChild(node);

		node = doc.createElement(XformBuilder.PREFIX_XFORMS + ":"
				+ XformBuilder.NODE_VALUE);
		node.setTextContent(spersonId);
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
			if (personId != null && personId.equals(itemElement
					.getAttribute(XformBuilder.ATTRIBUTE_ID)))
				return true;
		}

		return false;
	}
}
