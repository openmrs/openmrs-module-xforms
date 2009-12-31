package org.openmrs.module.xforms.test;

import java.io.File;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.util.LanguageUtil;


/**
 * 
 * @author daniel
 *
 */
public class LanguageUtilTest extends TestCase {

	public void testTranslate() throws Exception {
		
		String xformXml = XformBuilderTest.getFileAsString(new File("xformXml.xml"));
		String layoutXml = XformBuilderTest.getFileAsString(new File("layoutXml.xml"));
		String localeXml = XformBuilderTest.getFileAsString(new File("localeXml.xml"));

		try{
			org.w3c.dom.Element languageTextNode = null;
			languageTextNode = LanguageUtil.getLocaleTextNode(localeXml, Context.getLocale().getLanguage());
			if(languageTextNode != null)
				xformXml = LanguageUtil.translateXformXml(xformXml,languageTextNode);
			
			System.out.println(xformXml);
			
			if(languageTextNode != null)
				layoutXml = LanguageUtil.translateLayoutXml(layoutXml,languageTextNode);
			
			System.out.println(layoutXml);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
