package org.openmrs.module.xforms.download;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformUser;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.module.xforms.SerializableData;


/**
 * Manages user downloads.
 * 
 * @author Daniel
 *
 */
public class UserDownloadManager {
	
	public static void downloadUsers(OutputStream os) throws Exception{

		String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_SERIALIZER);
		if(className == null || className.length() == 0)
			className = XformConstants.DEFAULT_USER_SERIALIZER;
	
		SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		sr.serialize(new DataOutputStream(os), getUsers());
	}
	
	public static List<XformUser> getUsers(){
		return ((XformsService)Context.getService(XformsService.class)).getUsers();
	}
}
