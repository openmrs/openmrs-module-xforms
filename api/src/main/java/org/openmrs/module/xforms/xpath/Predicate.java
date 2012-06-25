package org.openmrs.module.xforms.xpath;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import org.w3c.dom.Element;



/**
 * @author Cosmin
 *
 * since I don't want to implement a full-blown xpath engine
 * our predicate 
 */
public class Predicate  implements Serializable 
{
	Vector resultSet;

	Predicate(Vector inNodeSet, String predicateExpr)
	{
		int nodeIndex = -1;

		if(predicateExpr == null || predicateExpr.length() <= 0) {
			resultSet = inNodeSet;
			return;
		} else //we need to parse predicateExpr
			resultSet = new Vector();
		//check if this predicate is just a logical condition or a complete XPath query.
		//for now we support only logical conditions
		/*If it is a complete XPath expression
		{
			ResultSet = (new XPathExpression(XML_DOM, XPath Expression))
				.getResult();
		}//If it is a complete XPath expression */
		try {
			nodeIndex = Integer.parseInt(predicateExpr);
		} catch(NumberFormatException nfe) {
			//we do nothing, the predicate was not an index
		}

		if(nodeIndex != -1) {
			resultSet.addElement(inNodeSet.elementAt(nodeIndex));
			return;
		}
		

		String[] tokens = predicateExpr.split(" and ");
		int pos = predicateExpr.indexOf(" and ");
		if(pos > 0){
			/*Vector resultSet1 = new Vector();
			String predicateExpr1 = predicateExpr.substring(0,pos);
			fillResultSet(resultSet1, predicateExpr1, inNodeSet);
			
			Vector resultSet2 = new Vector();
			String predicateExpr2 = predicateExpr.substring(pos + 5,predicateExpr.length());
			fillResultSet(resultSet2, predicateExpr2, inNodeSet);
			
			for(int index = 0; index < resultSet1.size(); index++){
				Object obj = resultSet1.get(index);
				if(resultSet2.contains(obj))
					resultSet.add(obj);
			}*/
			Vector[] resultSets = new Vector[tokens.length];
			
			for(int index = 0; index < tokens.length; index++){
				resultSets[index] = new Vector();
				fillResultSet(resultSets[index], tokens[index], inNodeSet);
			}
			
			Vector resultSet1 = resultSets[0];
			for(int index = 0; index < resultSet1.size(); index++){
				Object obj = resultSet1.get(index);
				
				boolean allFound = true;
				
				for(int i = 1; i < tokens.length; i++){
					Vector curResultSet = resultSets[i];
					if(!curResultSet.contains(obj)){
						allFound = false;
						break;
					}
				}
				
				if(allFound)
					resultSet.add(obj);
			}
		}
		else
			fillResultSet(resultSet, predicateExpr, inNodeSet);

		//here we should start parsing the predicateExpr
	}//constructor
	
	private void fillResultSet(Vector resultSet, String predicateExpr, Vector inNodeSet){
		String operation = null;
		int index = -1;

		if((index = predicateExpr.indexOf("=")) != -1) {
			operation = "=";
		} else if((index = predicateExpr.indexOf("<")) != -1) {
			operation = "<";
		} else if((index = predicateExpr.indexOf(">")) != -1) {
			operation = ">";
		} else {
			//shouldn't be here

			//Added by me on 11/05/2009 to cater for attributes without criteria values. eg [@name]
			if(predicateExpr.indexOf("@") != -1){
				for(Enumeration e = inNodeSet.elements(); e.hasMoreElements(); ) {
					Object obj = e.nextElement();

					String val = ((Element)obj).getAttribute(predicateExpr.substring(1,predicateExpr.length()));
					if(val != null && val.trim().length() > 0)
						resultSet.addElement(obj);	
				}
			}

			return;
		}

		Member member1 = new Member(new String(predicateExpr.toCharArray(), 0, index));
		Member member2 = new Member(new String(predicateExpr.toCharArray(), index+1, predicateExpr.length()-index-1));

		for(Enumeration e = inNodeSet.elements(); e.hasMoreElements(); ) {
			Object obj = e.nextElement();

			if(operation.equals("="))
				if(!member1.eval(obj).equals(member2.eval(obj)))
					continue;
				else if(operation.equals(">"))
					if(member1.eval(obj).compareTo(member2.eval(obj))<0)
						continue;
					else if(operation.equals("<"))
						if(member1.eval(obj).compareTo(member2.eval(obj))>0)
							continue;
			resultSet.addElement(obj);					
		}		
	}

	class Member 
	{
		String m = null;
		String attribute = null;

		Member(String op)
		{
			this.m = op;

			if(op.startsWith("@")) {
				attribute = new String(op.toCharArray(), 1, op.length()-1);
			}
			//for expath expressions enclosed with quotes.
			else if( (m.startsWith("'") && m.endsWith("'")) || (m.startsWith("\"") && m.endsWith("\"")) )
				m = m.substring(1,m.length()-1);
		}

		public String eval(Object obj) 
		{
			if(attribute == null)
				return m;

			if(!(obj instanceof Element))
				return "";

			Element element = (Element)obj;
			String attr = element.getAttribute(attribute);

			return attr!=null?attr:"";
		}
	}

	public Vector getResult()
	{
		return resultSet;
	}//getResult
}