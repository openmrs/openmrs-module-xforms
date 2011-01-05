package org.openmrs.module.xforms.util;

import java.util.HashMap;

import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Parses an xforms document and puts text in all nodes referencing an itext block for a given language.
 * The first language in the itext block is taken to be the current or default one.
 * 
 * @author daniel
 *
 */
public class ItextParser {	

	/**
	 * Parses an xform and sets the text of various nodes based on a given a locale.
	 * 
	 * @param xml the xforms xml.
	 * @param locale is the locale to translate the form into.
	 * @return the document where all itext refs are filled with text for a given locale.
	 */
	public static String parse(String xml, String locale) throws Exception {

		Document doc = XformsUtil.fromString2Doc(xml);

		//Check if we have an itext block in this xform.
		NodeList nodes = doc.getElementsByTagName("itext");
		if(nodes == null || nodes.getLength() == 0)
			return xml;

		Element itextNode = ((Element)nodes.item(0));

		//Check if we have any translations in this itext block.
		nodes = itextNode.getElementsByTagName("translation");
		if(nodes == null || nodes.getLength() == 0)
			return xml;

		HashMap<String,String> itextMap = null; //Map of default id and itext (for multiple values of the itext node) for the default language.

		//Map of each locale key and map of its id and itext translations.
		for(int index = 0; index < nodes.getLength(); index++){
			Element translationNode = (Element)nodes.item(index);
			String lang = translationNode.getAttribute("lang");

			if(!locale.toLowerCase().equals(lang.toLowerCase()))
				continue;

			itextMap = new HashMap<String,String>();
			fillItextMap(translationNode, itextMap);

			break;
		}

		//TODO itextMap is null when there are no translations for the current locale.
		if(itextMap != null){
			//TODO Need not rely on the xf prefix.
			translateNodes("xf:label", doc, itextMap);
			translateNodes("xf:hint", doc, itextMap);
			translateNodes("xf:title", doc, itextMap);
			translateNodes("xf:bind", doc, itextMap);
		}

		//We do not need the itext block any more since we have finished translating the form.
		itextNode.getParentNode().removeChild(itextNode);

		return XformsUtil.doc2String(doc);
	}


	/**
	 * Fills a map of id and itext for a given locale as represented by a given translation node.
	 * 
	 * @param translationNode the translation node.
	 * @param itext the itext map.
	 */
	private static void fillItextMap(Element translationNode, HashMap<String,String> itextMap){
		NodeList nodes = translationNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node textNode = nodes.item(index);
			if(textNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			setValueText(((Element)textNode).getAttribute("id"), textNode, itextMap);
		}

	}

	/**
	 * Sets the text value of a node.
	 * 
	 * @param textNode the node.
	 * @return the text value.
	 */
	private static void setValueText(String id, Node textNode, HashMap<String,String> itextMap){
		String defaultValue = null, longValue = null, shortValue = null;

		NodeList nodes = textNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node valueNode = nodes.item(index);
			if(valueNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String form = ((Element)valueNode).getAttribute("form");
			String text = valueNode.getTextContent();
			if(text != null){
				if(form == null)
					defaultValue = text;
				else if(form.equalsIgnoreCase("long"))
					longValue = text;
				else if(form != null && form.equalsIgnoreCase("short"))
					shortValue = text;
				else
					defaultValue = text;
			}
		}

		if(longValue != null)
			defaultValue = longValue;
		else if(shortValue != null)
			defaultValue = shortValue;

		itextMap.put(id, defaultValue);
	}


	/**
	 * For a given xforms document, fills the text of all nodes having a given name with their 
	 * corresponding text based on the itext id in the ref attribute.
	 * 
	 * @param name the name of the nodes to look for.
	 * @param doc the xforms document.
	 * @param itextMap the id to itext map.
	 */
	private static void translateNodes(String name, Document doc, HashMap<String,String> itextMap){
		NodeList nodes = doc.getElementsByTagName(name);
		if(nodes == null || nodes.getLength() == 0)
			return;

		for(int index = 0; index < nodes.getLength(); index++){
			Element node = (Element)nodes.item(index);

			String id = getItextId(node);
			if(id == null || id.trim().length() == 0)
				continue;

			node.setTextContent(itextMap.get(id));
		}
	}


	/**
	 * Gets the itext id from a given itext expression as represented by the ref attribute of a given node.
	 * 
	 * @param node the node having the itext expression.
	 * @return the itext id.
	 */
	public static String getItextId(Element node) {		
		//Check if node has a ref attribute.
		String ref = node.getAttribute("ref");
		if(ref == null)
			return null;

		//Check if node has jr:itext value in the ref attribute value.
		int pos = ref.indexOf("jr:itext('");
		if(pos < 0)
			return null;

		//We do not need this ref any more and so lets reduce the form size.
		node.removeAttribute("ref");

		//Get the itext id which starts at the 11th character.
		return ref.substring(10,ref.lastIndexOf("'"));
	}

	private static boolean isBindNode(Element node){
		return (node.getNodeName().equalsIgnoreCase("bind") ||
				node.getNodeName().equalsIgnoreCase("xf:bind"));
	}
}
