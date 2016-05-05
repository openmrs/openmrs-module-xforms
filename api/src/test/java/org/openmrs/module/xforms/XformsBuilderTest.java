/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms;

import java.io.IOException;

import junit.framework.Assert;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class XformsBuilderTest extends BaseModuleContextSensitiveTest {
	
	private static final String XFORM_WITH_NEW_RELATIONSHIP = "test_xform_with_new_relationships.xml";
	
	private static final String XFORM_WITH_EXISTING_RELATIONSHIP = "test_xform_with_existing_relationship.xml";
	
	private PatientService patientService;
	
	private PersonService personService;
	
	@Before
	public void before() {
		patientService = Context.getPatientService();
		personService = Context.getPersonService();
	}

	@Test
	public void shouldSuccessfullyGetTextValueFromNonNullNode() throws Exception{

		Element elm = new Element();
		XformBuilder.setNodeValue(elm, "1");
		String val = XformBuilder.getTextValue(elm);

		Assert.assertEquals("1", val);
	}

	@Test
	public void shouldThrowNullPointerExceptionIfGettingTextFromNullNode() throws Exception {
		boolean thrown = false;

  		try {
			Element elm = null;
			XformBuilder.setNodeValue(elm, "1");
			String val = XformBuilder.getTextValue(elm);
  		
  		} catch (NullPointerException e) {
    		thrown = true;
  		}
  		Assert.assertTrue(thrown);
	}

	//This test fails
	@Test
	public void shouldSuccessfullyGetNodeValue() throws Exception{
		
		String NAMESPACE_XFORMS = "http://www.w3.org/2002/xforms";
		Document doc = new Document();
		doc.setEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
		
		Element xformsNode = doc.createElement(NAMESPACE_XFORMS, "node");
		boolean set = XformBuilder.setNodeValue(xformsNode, "node", "10");

		Assert.assertFalse(set);
	}

	@Test
	public void shouldReturnParsedText() throws Exception{

		String name = "patient Alex encounter April person_address dummy patient_address dummy2 person_name Alex person_attribute dummy3 patient_identifier 1.0";
		name = XformBuilder.getDisplayText(name);
		System.out.println("[INFO]....... " + name);
		Assert.assertEquals("ALEX APRIL DUMMY DUMMY2 ALEX DUMMY3 1 0", name);
	}
}
