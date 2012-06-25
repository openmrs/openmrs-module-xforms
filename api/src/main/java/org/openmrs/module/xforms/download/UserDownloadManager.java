package org.openmrs.module.xforms.download;

import java.io.OutputStream;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.model.XformUser;
import org.openmrs.module.xforms.util.XformsUtil;


/**
 * Manages user downloads.
 * 
 * @author Daniel
 *
 */
public class UserDownloadManager {
	
	public static void downloadUsers(OutputStream os, String serializerKey) throws Exception{

		if(serializerKey == null)
			serializerKey = XformConstants.GLOBAL_PROP_KEY_USER_SERIALIZER;
		
        XformsUtil.invokeSerializationMethod("serialize",os,serializerKey , XformConstants.DEFAULT_USER_SERIALIZER, getUsers());
		
        /*String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_USER_SERIALIZER);
		if(className == null || className.length() == 0)
			className = XformConstants.DEFAULT_USER_SERIALIZER;
	
        Object obj = OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
        Method method = obj.getClass().getMethod("serialize", new Class[]{DataOutputStream.class,Object.class});
        method.invoke(obj, new Object[]{new DataOutputStream(os), getUsers()});*/

		//SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		//sr.serialize(new DataOutputStream(os), getUsers());
	}
	
	public static List<XformUser> getUsers(){
		return ((XformsService)Context.getService(XformsService.class)).getUsers();
	}
}
