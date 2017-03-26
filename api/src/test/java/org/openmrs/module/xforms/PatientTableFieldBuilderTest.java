/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.model;

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
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;

public class PatientTableFieldBuilderTest extends BaseModuleContextSensitiveTest {
	
	private static final String XFORM_WITH_NEW_RELATIONSHIP = "test_xform_with_new_relationships.xml";
	
	private static final String XFORM_WITH_EXISTING_RELATIONSHIP = "test_xform_with_existing_relationship.xml";
	
	private PatientService patientService;
	
	private PersonService personService;
	
	@Before
	public void before() {
		patientService = Context.getPatientService();
		personService = Context.getPersonService();
	}

	//Mutant
	@Test
	public void shouldAssertTrueIfFormIdsIsNotPopulatedByXformService() throws Exception{

		boolean ret;

		FormService formService = (FormService)Context.getService(FormService.class);
		List<PatientTableField> fields = new ArrayList<PatientTableField>();
		//Treat this like a filled XformsService object, we could not instantiate an abstract object
		List<Integer> formIds = new ArrayList();

 		if(formIds == null && formIds.size() == 0)
 			//return null;
 			ret = false;
 		else{
			for(Integer formId : formIds){
			//addFormTableFields(formId,fields,formService); This normally makes reference to a private class
			System.out.print("[INFO]......DUMMY");
		}

		ret = true; //ret = true; represents fields being populated
			//return fields;
		}
		Assert.assertTrue(ret);
	}

	//Non-equivalent mutant
	@Test
	public void shouldAssertFalseIfFormIdsIsNotPopulatedByXformService() throws Exception{

		boolean ret;

		FormService formService = (FormService)Context.getService(FormService.class);
		List<PatientTableField> fields = new ArrayList<PatientTableField>();
		//Treat this like a filled XformsService object, we could not instantiate an abstract object
		List<Integer> formIds = new ArrayList();

 		if(formIds != null || formIds.size() == 0)
 			//return null;
 			ret = false;
 		else{
			for(Integer formId : formIds){
			//addFormTableFields(formId,fields,formService); This normally makes reference to a private class
			System.out.print("[INFO]......DUMMY");
			}
			ret = true; //ret = true; represents fields being populated
			//return fields;
		}
		Assert.assertFalse(ret);
	}

	//Mutant Test
	@Test
	public void shouldAssertTrueIfFormIdsIsNotPopulatedByXformServiceButSizeNotEqualToZero() throws Exception{

		boolean ret;

		FormService formService = (FormService)Context.getService(FormService.class);
		List<PatientTableField> fields = new ArrayList<PatientTableField>();
		//Treat this like a filled XformsService object, we could not instantiate an abstract object
		List<Integer> formIds = new ArrayList();

 		if(formIds == null || formIds.size() != 0)
 			//return null;
 			ret = false;
 		else{
			for(Integer formId : formIds){
			//addFormTableFields(formId,fields,formService); This normally makes reference to a private class
			System.out.print("[INFO]......DUMMY");
		}

		ret = true; //ret = true; represents fields being populated
			//return fields;
		}
		Assert.assertTrue(ret);
	}
}
