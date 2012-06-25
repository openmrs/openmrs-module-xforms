package org.openmrs.module.xforms.xpath;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Cosmin
 * @author daniel
 */
//TODO: descendant axis doesn't work

public class XPathLocationStep implements Serializable{
	String axis = null;
	String nodeTest = null;
	String nodePrefix = null;
	String functionName = null;
	String predicate = null;

	private void parseLocationStep(String locationStep) {
		// todo: should check if the whole xpath expression
		// is parameter to a function
		// todo: optimizations -> next.toCharArray !!!
		String next = locationStep;
		int pattIndex = 0;

		axis = "";
		if (next.equals("//")) {
			nodeTest = "//";
			return;
		} else if (next.equals("/")) {
			nodeTest = "/";
			return;
		}

		// test if we have a relative path
		// for example: ../../../@zipcode
		// in this case the nodeTest will be null
		if (next.startsWith("..")) {
			axis = "parent";
			nodeTest = "..";
		} else if (next.startsWith(".")) {
			// don't know what axis to set here
			// child:: is probably incorrect
			nodeTest = ".";
		} else
			// test if we have an axis
			if (next.indexOf("::") == -1)
				if (next.startsWith("@")) {
					axis = "attribute";
					next = new String(next.toCharArray(), 1, next.length() - 1);
				} else
					axis = "child";
			else {
				pattIndex = next.indexOf("::");
				if (pattIndex != -1) {
					axis = new String(next.toCharArray(), 0, pattIndex);
					next = new String(next.toCharArray(), pattIndex + 2, next
							.length()
							- pattIndex - 2);
				}
			}

		pattIndex = next.indexOf("[");
		if (pattIndex != -1) {
			nodeTest = new String(next.toCharArray(), 0, pattIndex);
			next = new String(next.toCharArray(), pattIndex + 1, next.length()
					- pattIndex - 1);

			pattIndex = next.lastIndexOf(']');
			// pattIndex shouldn't be -1 in this case
			// maybe we should throw an exception??
			// for now assume that the expression is
			// formed correctly
			predicate = new String(next.toCharArray(), 0, pattIndex);
		} else
			nodeTest = next;

		// test for prefix
		if ((pattIndex = nodeTest.indexOf(":")) != -1) {
			nodePrefix = new String(nodeTest.toCharArray(), 0, pattIndex);
			nodeTest = new String(nodeTest.toCharArray(), pattIndex + 1, next
					.length()
					- pattIndex - 1);
		}

		// System.out.println("this partial location: "+locationStep+" is parsed
		// into");
		// System.out.println("functionName="+functionName+" axis="+axis+"
		// nodeTest="+nodeTest+" predicate="+predicate);
	}

	public XPathLocationStep(String locationStep) {
		parseLocationStep(locationStep);
	}// constructor

	/**
	 * the contextNodeSet is made of nodes that are instances of Element A fix
	 * is needed here: to the result vector I only add Element-s or String. This
	 * is not correct. I should only add Node-s
	 */
	public Vector getResult(Vector contextNodeSet, Vector resultNodeSet) {
		Vector outputNodeSet = resultNodeSet;
		int nodeCount = contextNodeSet.size();
		int i = 0;

		if (axis.equals("child") || axis.equals("descendant")) {
			for (i = 0; i < nodeCount; i++) {
				Node node = (Node) contextNodeSet.elementAt(i);
				int childCount = node.getChildNodes().getLength();

				for (int j = 0; j < childCount; j++) {
					if (node.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element childNode = (Element) node.getChildNodes().item(j);
						String childName = childNode.getNodeName();

						//Small addition to cater for nodes with prefixes
						int pos = childName.indexOf(':');
						if(pos >= 0)
							childName = childName.substring(pos+1);
						
						String prefix = null;
						if (nodePrefix != null) {
							Element element = (Element) childNode;
							prefix = element.getNamespaceURI();
						}

						if (nodeTest.equals("*") || nodeTest.equalsIgnoreCase(childName) //TODO This was just changed from equals to make xpath expressions case insensitive
								|| nodeTest.equals("node()")) {
							if (((nodePrefix != null) && (nodePrefix
									.equals(prefix)))
									|| (nodePrefix == null))
								outputNodeSet.addElement(childNode);
						} else if (nodeTest.equals("text()"))
							outputNodeSet.addElement(childNode.getChildNodes().item(0).getNodeValue());

						if (axis.equals("descendant")) {
							Vector descendants = null;
							descendants = getMatchingDescendants(childNode);

							for (int k = 0; k < descendants.size(); k++)
								outputNodeSet.addElement(descendants
										.elementAt(k));
						}
					} else if (node.getChildNodes().item(j).getNodeType() == Node.TEXT_NODE) {
						if (nodeTest.equals("text()"))
							outputNodeSet.addElement(node.getChildNodes().item(j).getNodeValue());
					}
				}
			}
		}

		if (axis.equals("parent")) {
			for (i = 0; i < nodeCount; i++) {
				Node cn = (Node) contextNodeSet.elementAt(i);

				if (cn instanceof Element)
					outputNodeSet.addElement(((Element) cn).getParentNode());
			}
		}

		if (axis.equals("attribute")) {
			for (i = 0; i < nodeCount; i++) {
				Node n = (Node) contextNodeSet.elementAt(i);

				if (n instanceof Element) {
					String val = ((Element) n).getAttribute(nodeTest);
					if (val != null)
						outputNodeSet.addElement(val);
				}
			}
		}

		// other axes go here

		// no axis whatsoever (or maybe unknown to me :)
		if (axis.equals("")) {
			if (nodeTest.equals("/")) {
				Object startNode = null;
				// find first element in the contextNodeSet
				for (Enumeration nodes = contextNodeSet.elements(); nodes
				.hasMoreElements();) {
					startNode = nodes.nextElement();
					if (startNode instanceof Element)
						break;
				}

				if (startNode instanceof Element) {
					//Element tmp = null;
					Node tmp = null;
					//while ( (((Element)startNode).getParentNode() instanceof Element) && 
					//((tmp = (Element)((Element) startNode).getParentNode()) != null))
					
					//while((tmp = (Element)((Element) startNode).getParentNode()) != null)
					//	startNode = tmp;
					tmp = ((Node) startNode).getParentNode();
					while(tmp != null){
						startNode = tmp;
						tmp = ((Node) startNode).getParentNode();
					}
					
					outputNodeSet.addElement(startNode);
				} else {
					// System.out.println("couldn't find root");
					// couldn't find any elements in context
					return contextNodeSet;
				}
			} else if (nodeTest.equals(".")) {
				// simply copy the input vector
				for (Enumeration enumeration = contextNodeSet.elements(); enumeration
				.hasMoreElements();)
					outputNodeSet.addElement(enumeration.nextElement());
			}
		}

		if (predicate != null) {
			Predicate predicateEvaluator = new Predicate(outputNodeSet,
					predicate);
			outputNodeSet = predicateEvaluator.getResult();
		}
		return outputNodeSet;
	}

	private Vector getMatchingDescendants(Node node) {
		Vector matchingDescendants = new Vector();
		int childCount = node.getChildNodes().getLength();

		for (int j = 0; j < childCount; j++) {
			// this is were we test if the
			// node test part of our xpath expression
			// matches this node
			if (node.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
				Node childNode = (Node) node.getChildNodes().item(j);
				String name = ((Element) childNode).getNodeName();
				if (nodeTest.equals("*") || nodeTest.equalsIgnoreCase(name)) //TODO This was just changed from equals to make xpath expression case insensitive
					matchingDescendants.addElement(node);

				Node[] moreDescendants = null;

				Vector tmp = getMatchingDescendants(childNode);

				moreDescendants = new Node[tmp.size()];
				tmp.copyInto(moreDescendants);
				tmp = null;

				for (int i = 0; i < moreDescendants.length; i++)
					matchingDescendants.addElement(moreDescendants[i]);
			}
		}
		return matchingDescendants;
	}
}