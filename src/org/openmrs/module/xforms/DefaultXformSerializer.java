package org.openmrs.module.xforms;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

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
public class DefaultXformSerializer implements SerializableData{
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public DefaultXformSerializer(){
		
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.DataOutputStream,java.lang.Object)
	 */
	public void serialize(DataOutputStream dos,Object data){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dosLocal = new DataOutputStream(baos);

			List<String> xforms = (List<String>)data; //This is always a list of strings.
			dosLocal.writeByte(xforms.size()); //Write the size such that the party at the other end knows how many times to loop.
			for(String xml : xforms)
				dosLocal.writeUTF(xml);
			
			GZIPOutputStream gzip = new GZIPOutputStream(dos);
			gzip.write(baos.toByteArray());
			gzip.finish();
		}
		catch(Exception e){
			log.error(e);
		}
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.DataInputStream, java.lang.Object)
	 */
	public Object deSerialize(DataInputStream dis,Object data){
		List<String> forms = new ArrayList<String>();
		try{
			int len = dis.readByte();
			for(int i=0; i<len; i++)
				forms.add(dis.readUTF());
		}
		catch(Exception e){
			log.error(e);
		}
		
		return forms;
	}
}
