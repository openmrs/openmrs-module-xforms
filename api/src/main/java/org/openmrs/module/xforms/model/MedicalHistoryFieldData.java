package org.openmrs.module.xforms.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.xforms.serialization.Persistent;
import org.openmrs.module.xforms.serialization.SerializationUtils;


/**
 * Holds a list of values for a patient history field. 
 * eg Past ARVSs as a history field can have a list of values
 * for various patient visit dates.
 * 
 * @author daniel
 *
 */
public class MedicalHistoryFieldData implements Persistent{
	
	private String fieldName;
	private List<MedicalHistoryValue> values = new ArrayList<MedicalHistoryValue>();
	
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public List<MedicalHistoryValue> getValues() {
		return values;
	}

	public void setValues(List<MedicalHistoryValue> values) {
		this.values = values;
	}
	
	public void addValue(MedicalHistoryValue value) {
		if(value == null)
			return;
		
		/*if(values != null && appendSameDateValue(value))
			return;
		else*/ if(values == null)
			values = new ArrayList<MedicalHistoryValue>();
		
		values.add(value);
	}
	
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setFieldName(dis.readUTF());
		setValues(SerializationUtils.read(dis,new MedicalHistoryValue().getClass()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getFieldName());
		SerializationUtils.write(getValues(), dos);
	}
	
	private boolean appendSameDateValue(MedicalHistoryValue value){
		Date valueDate = value.getValueDate();
		MedicalHistoryValue val;
		for(int index = 0; index < values.size(); index++){
			val = values.get(index);
			if(val.getValueDate().equals(valueDate)){
				val.setValue(val.getValue() + ", " + value.getValue());
				return true;
			}
		}
		return false;
	}
}
