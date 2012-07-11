/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms;

import java.io.IOException;

import junit.framework.Assert;

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

public class RelationshipSubmissionTest extends BaseModuleContextSensitiveTest {
	
	private static final String XFORM_WITH_NEW_RELATIONSHIP = "test_xform_with_new_relationships.xml";
	
	private static final String XFORM_WITH_EXISTING_RELATIONSHIP = "test_xform_with_existing_relationship.xml";
	
	private PatientService patientService;
	
	private PersonService personService;
	
	@Before
	public void before() {
		patientService = Context.getPatientService();
		personService = Context.getPersonService();
	}
	
	/**
	 * @see {@link RelationshipSubmission#submit(Element,Patient)}
	 */
	@Test
	@Verifies(value = "should create relationships", method = "submit(Element,Patient)")
	public void submit_shouldCreateRelationships() throws Exception {
		Patient patient = patientService.getPatient(8);
		
		final String childUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
		Person child = personService.getPersonByUuid(childUuid);
		RelationshipType parentChild = personService.getRelationshipType(2);
		//sanity check that the relationship doesn't exist
		Assert.assertEquals(0, personService.getRelationships(patient, child, parentChild).size());
		
		final String doctorUuid = "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562";
		Person doctor = personService.getPersonByUuid(doctorUuid);
		RelationshipType doctorPatient = personService.getRelationshipType(1);
		Assert.assertEquals(0, personService.getRelationships(patient, doctor, doctorPatient).size());
		
		Document doc = XformBuilder.getDocument(readXformFileToString(XFORM_WITH_NEW_RELATIONSHIP));
		RelationshipSubmission.submit(doc.getRootElement(), patient);
		
		Assert.assertEquals(1, personService.getRelationships(patient, child, parentChild).size());
		Assert.assertEquals(1, personService.getRelationships(doctor, patient, doctorPatient).size());
	}
	
	/**
	 * @see {@link RelationshipSubmission#submit(Element,Patient)}
	 */
	@Test
	@Verifies(value = "should edit an editing relationship", method = "submit(Element,Patient)")
	public void submit_shouldEditAnEditingRelationship() throws Exception {
		Patient patient = patientService.getPatient(2);
		
		Person currentDoctor = personService.getPerson(502);
		Person newDoctor = personService.getPerson(8);
		RelationshipType doctorPatient = personService.getRelationshipType(1);
		//sanity checks
		Assert.assertEquals(1, personService.getRelationships(currentDoctor, patient, doctorPatient).size());
		Assert.assertEquals(0, personService.getRelationships(newDoctor, patient, doctorPatient).size());
		
		Document doc = XformBuilder.getDocument(readXformFileToString(XFORM_WITH_EXISTING_RELATIONSHIP));
		RelationshipSubmission.submit(doc.getRootElement(), patient);
		
		Assert.assertEquals(0, personService.getRelationships(currentDoctor, patient, doctorPatient).size());
		Assert.assertEquals(1, personService.getRelationships(newDoctor, patient, doctorPatient).size());
	}
	
	private String readXformFileToString(String filename) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename));
	}
}
