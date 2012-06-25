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
	
	//TODO Looks like these five lines below belong to MedicalHistoryFieldData where the field name is.
	public static final byte TYPE_STRING = 1;
	public static final byte TYPE_INT = 2;
	public static final byte TYPE_FLOAT = 3;
	public static final byte TYPE_DATE = 4;
	
	private byte type = TYPE_STRING;
	
	
	
	private Object value;
	private Date valueDate;

	public MedicalHistoryValue(){
		
	}
	
	/*public MedicalHistoryValue(String value, Date valueDate){
		this.value = value;
		this.valueDate = valueDate;
	}*/
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setType(dis.readByte());
		
		if(type == TYPE_STRING)
			setValue(dis.readUTF());
		else if(type == TYPE_INT)
			setValue(dis.readInt());
		else if(type == TYPE_FLOAT)
			setValue(dis.readFloat());
		else if(type == TYPE_DATE)
			setValue(new Date(dis.readLong()));
		
		setValueDate(new Date(dis.readLong()));
	}
	
	public void write(DataOutputStream dos) throws IOException {
		
		dos.writeByte(type);
		
		if(type == TYPE_STRING)
			dos.writeUTF((String)value);
		else if(type == TYPE_INT)
			dos.writeInt((Integer)value);
		else if(type == TYPE_FLOAT)
			dos.writeFloat((Float)value);
		else if(type == TYPE_DATE)
			dos.writeLong(((Date)value).getTime());
		
		dos.writeLong(getValueDate().getTime());
	}
}
