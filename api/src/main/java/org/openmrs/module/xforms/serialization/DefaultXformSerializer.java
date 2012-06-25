package org.openmrs.module.xforms.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Privides the default xform serialization and deserialization from and to the sever.
 * An example of such clients could be mobile devices collecting data in for instance 
 * offline mode, and then send it to the server when connected.
 * 
 * For those who want a different serialization format for xforms,
 * just implement the SerializableData interface and specify the class
 * using the openmrs global property {xforms.xformSerializer}. 
 * The jar containing this class can then be
 * put under the webapps/openmrs/web-inf/lib folder.
 * One of the reasons one could want a different serialization format
 * is for performance by doing a more optimized and compact format. Such an example
 * exists in the EpiHandy compact implementation of xforms.
 * 
 * @author Daniel
 *
 */
public class DefaultXformSerializer {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public DefaultXformSerializer(){
		
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.DataOutputStream,java.lang.Object)
	 */
	public void serialize(OutputStream os,Object data){
		try{
			DataOutputStream dos = new DataOutputStream(os);
			
			List<String> xforms = (List<String>)data; //This is always a list of strings.
			dos.writeByte(xforms.size()); //Write the size such that the party at the other end knows how many times to loop.
			for(String xml : xforms)
				dos.writeUTF(xml);
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.DataInputStream, java.lang.Object)
	 */
	public Object deSerialize(InputStream is,Object data){
		
		DataInputStream dis = new DataInputStream(is);
		
		List<String> forms = new ArrayList<String>();
		try{
			int len = dis.readByte();
			for(int i=0; i<len; i++)
				forms.add(dis.readUTF());
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}
		
		return forms;
	}
}
