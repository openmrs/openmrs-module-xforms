package org.openmrs.module.xforms.bluetooth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;


/**
 * This class implements the task which handles bluetooth connection requests for xforms
 * together with the corresponding user and patient downloads.
 * 
 * @author Daniel
 * @version 1.0
 */
public class XformsBluetoothTask extends AbstractTask{

//	 Logger
	private static Log log = LogFactory.getLog(XformsBluetoothTask.class);
	
	private static final String PROPERTY_SERVICE_NAME = "ServiceName";
	private static final String PROPERTY_SERVER_UUID = "ServerUUID";
	private static final String PROPERTY_SERVER_IP = "ServerIP";
	
	// Instance of xforms bluetooth server.
	private XformsBluetoothServer server = null;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 * 
	 */
	public XformsBluetoothTask() {
		/*if (server == null)
			server = new BluetoothServer();*/
	}

	/**
	 * Process incoming connection.
	 */
	public void execute() {
		Context.openSession();
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			
			String serviceName = taskDefinition.getProperty(PROPERTY_SERVICE_NAME); //"OpenMRS XForms Bluetooth Server"
			String serverUUID = taskDefinition.getProperty(PROPERTY_SERVER_UUID); //"F0E0D0C0B0A000908070605040301111"
			String serverIP= taskDefinition.getProperty(PROPERTY_SERVER_IP); //"localhost"
			
			if(serviceName == null){
				serviceName = "OpenMRS XForms Bluetooth Server";
				taskDefinition.setProperty(PROPERTY_SERVICE_NAME, serviceName);
				log.error("Property "+PROPERTY_SERVICE_NAME+" was null. Set to "+serviceName);
			}
			
			if(serverUUID == null){
				serverUUID = "F0E0D0C0B0A000908070605040301111";
				taskDefinition.setProperty(PROPERTY_SERVER_UUID, serverUUID);
				log.error("Property "+PROPERTY_SERVER_UUID+" was null. Set to "+serverUUID);
			}
			
			if(serverIP == null){
				serverIP = "localhost";
				taskDefinition.setProperty(PROPERTY_SERVER_IP, serverIP);
				log.error("Property "+PROPERTY_SERVER_IP+" was null. Set to "+serverIP);
			}
						
			server = new XformsBluetoothServer(serviceName,serverUUID);
			server.start();
		} catch (APIException e) {
			log.error("Error running xforms bluetooth task", e);
			throw e;
		} finally {
			Context.closeSession();
		}
	}
	
	/**
	 * Clean up any resources here
	 *
	 */
	public void shutdown() {
		if(server != null)
			server.stop();
		server = null;
		super.shutdown();
	}
}
