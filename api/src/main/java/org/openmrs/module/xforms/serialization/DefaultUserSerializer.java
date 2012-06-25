package org.openmrs.module.xforms.serialization;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.xforms.model.XformUser;


//TODO This class may need to be refactored out of the XForms module. Not very sure for now.

/**
 * Provides default serialization and deserialization of users to clients.
 * This class is used when sending a list of users to clients like
 * mobile devices that may want to collect patient data in for instance offline mode.
 * This list of users helps clients protect sensitive data on these devices
 * by requiring the same openmrs login info.
 * 
 * For those who want a different serialization format for users,
 * just implement the SerializableData interface and specify the class
 * using the openmrs global property {xforms.userSerializer}. 
 * The jar containing this class can then be
 * put under the webapps/openmrs/web-inf/lib folder.
 * One of the reasons one could want a different serialization format
 * is for performance by doing a more optimized and compact format.
 * Onother reason i can foresee is for non java clients who may say
 * want the users in xml or any other format, because the default
 * implementation assumes java clients.
 * 
 * @author Daniel
 *
 */
public class DefaultUserSerializer {

	private Log log = LogFactory.getLog(this.getClass());
	
	public DefaultUserSerializer(){
		
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.DataOutputStream,java.lang.Object)
	 */
	public void serialize(OutputStream os,Object data){
		try{
			DataOutputStream dos = new DataOutputStream(os);
			
			if(data == null){
        		dos.writeInt(0);
        		return;
        	}
			
			List<XformUser> users = (List<XformUser>)data; //This will always be a list of XFormUser

			dos.writeInt(users.size());
			for(XformUser user : users)
				serialize(user,dos);
			
		}catch(IOException e){
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * Serializes a user to the stream.
	 * 
	 * @param user - the user to serialize.
	 * @param dos  - the stream to write to.
	 */
	protected void serialize(XformUser user, DataOutputStream dos){
		try{
			dos.writeInt(user.getUserId());
			dos.writeUTF(user.getName());
			dos.writeUTF(user.getPassword());
			dos.writeUTF(user.getSalt());
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.DataInputStream, java.lang.Object)
	 */
	public Object deSerialize(InputStream is,Object data){
		return null; //not necessary for now because we do not expect users to come back from the client.
	}
}
