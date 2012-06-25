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
