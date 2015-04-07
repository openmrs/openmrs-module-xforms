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


/**
 * This class encapsulates a patient database field value.
 * 
 * @author Daniel
 *
 */
public class PatientTableFieldValue {

	private int fieldId;
	private int patientId;
	private Object value;
	
	public PatientTableFieldValue(){
		
	}

	public PatientTableFieldValue(int fieldId, int patientId, Object value) {
		super();
		this.fieldId = fieldId;
		this.patientId = patientId;
		this.value = value;
	}
	
	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
