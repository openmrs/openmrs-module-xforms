package org.openmrs.module.xforms.util;

import java.util.Vector;

import org.openmrs.module.xforms.xpath.XPathExpression;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class contains utilities used during the translation of xforms and form layout
 * in various locales.
 * 
 * @author daniel
 *
 */
public class LanguageUtil {

	/** The xpath attribute name. */
	public static final String ATTRIBUTE_NAME_XPATH = "xpath";

	/** The value attribute name. */
	public static final String ATTRIBUTE_NAME_VALUE = "value";

	private static final String NODE_NAME_XFORM = "xform";
	private static final String NODE_NAME_FORM = "Form";

	public static final String NODE_NAME_LANGUAGE_TEXT = "LanguageText";


	/**
	 * Replaces localizable text in am xml document with that in the language document.
	 * 
	 * @param srcXml the document xml.
	 * @param languageXml the language document xml.
	 * @return the new document xml after its text has been replaced with that from the language document.
	 */
	public static String translate(String srcXml, String languageXml) throws Exception {
		if(languageXml == null || srcXml == null)
			return srcXml;

		Document srcXmlDoc = XformsUtil.fromString2Doc(srcXml);
		Document langXmlDoc = XformsUtil.fromString2Doc(languageXml);

		if(srcXmlDoc == null || langXmlDoc == null)
			return null;

		return translate(srcXmlDoc,langXmlDoc.getDocumentElement());
	}


	/**
	 * Replaces localizable text in am xml document with that in the language document.
	 * 
	 * @param doc the document whose localizable text to replace.
	 * @param languageXml the parent node of the language document
	 * @return the new document xml after its text has been replaced with that from the language document.
	 */
	public static String translate(Document doc, Element parentLangNode){
		NodeList nodes = parentLangNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String xpath = ((Element)node).getAttribute(ATTRIBUTE_NAME_XPATH);
			String value = ((Element)node).getAttribute(ATTRIBUTE_NAME_VALUE);
			if(xpath == null || value == null)
				continue;

			Vector<?> result = new XPathExpression(doc, xpath).getResult();
			if(result != null){

				//TODO We need to uniquely identify nodes and so each xpath should
				//point to no more than one node.
				if(result.size() > 2){
					System.out.println("xforms module error: " + result.size()+"..........."+xpath+"............"+value);
					continue;
				}
				else if(result.size() == 0)
					System.out.println("xforms module error: " + result.size()+"..........."+xpath+"............"+value);
				
				for(int item = 0; item < result.size(); item++){
					if(!(result.get(item) instanceof Element))
						continue;
					else{
						Element targetNode = (Element)result.get(item);
						int pos = xpath.lastIndexOf('@');
						if(pos > 0 && xpath.indexOf('=',pos) < 0){
							String attributeName = xpath.substring(pos + 1, xpath.indexOf(']',pos));
							targetNode.setAttribute(attributeName, value);
						}
						else
							targetNode.setTextContent(value);
					}
				}
			}
		}
		return XformsUtil.doc2String(doc);
	}

	public static Element getLocaleTextNode(String localeXml, String localeKey) throws Exception {

		Document localeDoc = XformsUtil.fromString2Doc(localeXml);

		NodeList nodes = localeDoc.getDocumentElement().getElementsByTagName(NODE_NAME_LANGUAGE_TEXT);
		if(nodes == null)
			return null;

		//If we do not have at least two locales, then there is no need wasting time to translate
		//We just use the original text in its default locale.
		if(nodes.getLength() < 2)
			return null;

		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(((Element)node).getAttribute("lang").equalsIgnoreCase(localeKey))
				return (Element)node;
		}

		return null;
	}

	public static String translateXformXml(String xml, Element languageTextNode) throws Exception {
		Element node = getXformsLocaleNode(languageTextNode);
		if(node == null)
			return xml; //no text yet for this locale.
		
		return translate(XformsUtil.fromString2Doc(xml), node);
	}

	public static String translateLayoutXml(String xml, Element languageTextNode) throws Exception {
		Element node = getLayoutLocaleNode(languageTextNode);
		if(node == null)
			return xml; //no text yet for this locale.
		
		return translate(XformsUtil.fromString2Doc(xml), node);
	}


	/**
	 * Extracts text for a given node name from a document.
	 * 
	 * @param doc the document.
	 * @param nodeName the node name.
	 * @return the node.
	 */
	private static Element getNode(Element root, String nodeName){
		NodeList nodes = root.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(node.getNodeName().equalsIgnoreCase(nodeName))
				return (Element)node;
		}

		return null;
	}

	/**
	 * Extracts xforms locale text from a combined locale document.
	 * 
	 * @param doc the locale document.
	 * @return the xforms locale text.
	 */
	public static Element getXformsLocaleNode(Element root){
		return getNode(root, NODE_NAME_XFORM);
	}

	/**
	 * Extracts layout locale text from a combined locale document.
	 * 
	 * @param doc the locale document.
	 * @return the layout locale text.
	 */
	public static Element getLayoutLocaleNode(Element root){
		return getNode(root, NODE_NAME_FORM);
	}
}
