package org.openmrs.module.xforms.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.openmrs.module.xforms.serialization.Persistent;


/**
 * Holds a value for a patient history field on a given visit date.
 * 
 * @author daniel
 *
 */
public class MedicalHistoryValue implements Persistent{
	
	private String value;
	private Date valueDate;

	public MedicalHistoryValue(){
		
	}
	
	public MedicalHistoryValue(String value, Date valueDate){
		this.value = value;
		this.valueDate = valueDate;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setValue(dis.readUTF());
		setValueDate(new Date(dis.readLong()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getValue());
		dos.writeLong(getValueDate().getTime());
	}
}
