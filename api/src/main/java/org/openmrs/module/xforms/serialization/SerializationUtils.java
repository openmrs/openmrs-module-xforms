package org.openmrs.module.xforms.serialization;

import java.util.*;
import java.io.*;


/**
 * Helper class to write and read collection to and from streams. This class also
 * writes the built in types taking care of nulls if any.
 * 
 * @author Daniel Kayiwa
 *
 */
public class SerializationUtils {
	
	/**
	 * Writes a string to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the string to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeUTF(DataOutputStream dos,String data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeUTF(data);
		}
		else
			dos.writeBoolean(false);
	}
	
	/**
	 * Writes an Integer to the stream.
	 * 
	 * @param dos - the stream for writing.
	 * @param data - the Interger to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeInteger(DataOutputStream dos,Integer data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeInt(data.intValue());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a Date to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the Date to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeDate(DataOutputStream dos,Date data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeLong(data.getTime());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Writes a boolean to a stream.
	 * 
	 * @param dos - the stream to write to.
	 * @param data - the boolean to write.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void writeBoolean(DataOutputStream dos,Boolean data) throws IOException{
		if(data != null){
			dos.writeBoolean(true);
			dos.writeBoolean(data.booleanValue());
		}
		else
			dos.writeBoolean(false);
	} 
	
	/**
	 * Reads a string from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read string or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static String readUTF(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return dis.readUTF();
		return null;
	}
	
	/**
	 * Reads an Integer from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Integer or null of none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Integer readInteger(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Integer(dis.readInt());
		return null;
	}
	
	/**
	 * Reads a Date from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read Date or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Date readDate(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Date(dis.readLong());
		return null;
	}
	
	/**
	 * Reads a boolean from a stream.
	 * 
	 * @param dis - the stream to read from.
	 * @return - the read boolean or null if none.
	 * @throws IOException - thrown when a problem occurs during the reading from stream.
	 */
	public static Boolean readBoolean(DataInputStream dis) throws IOException {
		if(dis.readBoolean())
			return new Boolean(dis.readBoolean());
		return null;
	}
	
	/**
	 * Write a hashtable of string keys and values to a stream.
	 * 
	 * @param stringHashtable - a hashtable of string keys and values.
	 * @param dos - that stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	/*public static void write(Hashtable stringHashtable, DataOutputStream dos) throws IOException {	
		if(stringHashtable != null){
			dos.writeInt(stringHashtable.size());
			Enumeration keys = stringHashtable.keys();
			String key;
			while(keys.hasMoreElements()){
				key  = (String)keys.nextElement();
				dos.writeUTF(key);
				dos.writeUTF((String)stringHashtable.get(key));
			}
		}
		else
			dos.writeInt(0);
	}*/
	
	public static List read(DataInputStream dis, Class cls) throws IOException, InstantiationException,IllegalAccessException {
		
		int len = dis.readInt();
		if(len == 0)
			return null;

		List<Persistent> persistentList = new ArrayList<Persistent>();
		
		for(byte i=0; i<len; i++ ){
			Persistent persistent = (Persistent)cls.newInstance();
			persistent.read(dis);
			persistentList.add(persistent);
		}
		
		return persistentList;
	}

	/**
	 * Writes a small vector (byte size) of Persistent objects to a stream.
	 * 
	 * @param persistentVector - the vector of persistent objects.
	 * @param dos - the stream to write to.
	 * @throws IOException - thrown when a problem occurs during the writing to stream.
	 */
	public static void write(List persistentList, DataOutputStream dos) throws IOException {	
		if(persistentList != null){
			dos.writeInt(persistentList.size());
			for(int i=0; i<persistentList.size(); i++ )
				((Persistent)persistentList.get(i)).write(dos);
		}
		else
			dos.writeInt(0);
	}
}
