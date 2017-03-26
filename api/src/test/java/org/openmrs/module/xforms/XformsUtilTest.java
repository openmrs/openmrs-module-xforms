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

public class XformsUtilTest extends BaseModuleContextSensitiveTest {
	
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
	public void mutantAuthenticateInLineUser1() throws ContextAuthenticationException {

		boolean wasAuth = false;
		
		//Start of mutant method: public static void authenticateInlineUser(HttpServletRequest request) throws ContextAuthenticationException {
		if (!Context.isAuthenticated()) {
			String name = "uname";
			String pw = "pw";
 				
 			if (name == null & pw == null)
				Context.authenticate(name, pw);
				wasAuth = true;
			}

		Assert.assertFalse(wasAuth);
	}

	@Test
	public void mutantAuthenticateInLineUser2() throws ContextAuthenticationException {

		boolean wasAuth = false;
		
		//Start of mutant method: public static void authenticateInlineUser(HttpServletRequest request) throws ContextAuthenticationException {
		if (!Context.isAuthenticated()) {
			String name = "uname";
			String pw = "pw";
 				
 			if (name != null & pw == null)
				Context.authenticate(name, pw);
				wasAuth = true;
			}

		Assert.assertFalse(wasAuth);
	}

	@Test
	public void mutantAuthenticateInLineUser3() throws ContextAuthenticationException {

		boolean wasAuth = false;

		//Start of mutant method: public static void authenticateInlineUser(HttpServletRequest request) throws ContextAuthenticationException {
		if (!Context.isAuthenticated()) {
			String name = "uname";
			String pw = "pw";
 				
 			if (name == null & pw != null)
				Context.authenticate(name, pw);
				wasAuth = true;
			}

		Assert.assertFalse(wasAuth);
	}

}
