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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Patient;

public class PatientData {

    /** The list of patients. */
    private List<Patient> patients = new ArrayList<Patient>();

    /** The list of patient database fields. */
    private List<PatientTableField> fields = new ArrayList<PatientTableField>();

    /** The list of patient database field values. */
    private List<PatientTableFieldValue> fieldValues = new ArrayList<PatientTableFieldValue>();

    /** The patient medical history. */
	private List<PatientMedicalHistory> medicalHistory;
	
	
    public PatientData() {

    }

    public PatientData(List<Patient> patients, List<PatientTableField> fields,
            List<PatientTableFieldValue> fieldValues) {
        super();
        this.patients = patients;
        this.fields = fields;
        this.fieldValues = fieldValues;
    }

    public List<PatientTableField> getFields() {
        return fields;
    }

    public void setFields(List<PatientTableField> fields) {
        this.fields = fields;
    }

    public List<PatientTableFieldValue> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<PatientTableFieldValue> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }
    
    public List<PatientMedicalHistory> getMedicalHistory() {
		return medicalHistory;
	}

	public void setMedicalHistory(List<PatientMedicalHistory> medicalHistory) {
		this.medicalHistory = medicalHistory;
	}
	
	public void addMedicalHistory(PatientMedicalHistory history){
		if(history == null)
			return;
		
		if(medicalHistory == null)
			medicalHistory = new ArrayList<PatientMedicalHistory>();
		medicalHistory.add(history);
	}
}
