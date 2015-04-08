/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.sms;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * @author daniel
 */
public class FormSmsParserTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void emptyTest() throws Exception {
		//at least one non-ignored test needed
	}
	
	@Test
	@Ignore("No xform found with id=15. We need to find a way to run this test with the in-memory db.")
	public void testSms2FormData() throws Exception {
		//username,password,formid, question=answer
		String text = "admin test 15 10=Vurugayo 15=m 9=11/12/2001 1=20/08/2009 2=2 3=1 5=10 11 12 6=2 7=1 2 3 8=223.789 12=DeleteMan";
		
		FormSmsParser formSmsParser = new FormSmsParser();
		String xml = formSmsParser.sms2FormXml("+256782380638", text);
		Assert.assertNotNull(xml);
	}
}
