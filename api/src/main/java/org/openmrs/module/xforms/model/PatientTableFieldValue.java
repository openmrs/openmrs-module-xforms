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
