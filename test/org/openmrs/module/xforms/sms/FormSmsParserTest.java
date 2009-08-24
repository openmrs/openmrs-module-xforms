package org.openmrs.module.xforms.sms;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;


/**
 * 
 * @author daniel
 *
 */
public class FormSmsParserTest extends BaseModuleContextSensitiveTest{

	public Boolean useInMemoryDatabase() {
		return false;
	}
	
	@Test
	public void testSms2FormData() throws Exception{
		authenticate();
		
		//username,password,formid, 14=197
		String text = "guyzb daniel123 15 10=Vurugayo 15=m 9=11/12/2001 1=20/08/2009 2=2 3=1 5=10 11 12 6=2 7=1 2 3 8=223.789 12=DeleteMan";
		
		FormSmsParser formSmsParser = new FormSmsParser();
		String xml = formSmsParser.sms2FormXml("+256782380638", text);
		assert(xml != null);
		System.out.println(xml);
		
		//commitTransaction(false);
	}
}
