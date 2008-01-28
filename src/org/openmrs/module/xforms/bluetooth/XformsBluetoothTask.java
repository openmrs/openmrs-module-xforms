package org.openmrs.module.xforms.bluetooth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.module.xforms.bluetooth.XformsBluetoothServer;


/**
 * This class implements the task which handles bluetooth connection requests for xforms
 * together with the corresponding user and patient downloads.
 * 
 * @author Daniel
 * @version 1.0
 */
public class XformsBluetoothTask implements Schedulable{

//	 Logger
	private static Log log = LogFactory.getLog(XformsBluetoothTask.class);

	// Task configuration 
	private TaskConfig taskConfig;
	
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
	public void run() {
		Context.openSession();
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			
			String serviceName = taskConfig.getProperty(PROPERTY_SERVICE_NAME); //"OpenMRS XForms Bluetooth Server"
			String serverUUID = taskConfig.getProperty(PROPERTY_SERVER_UUID); //"F0E0D0C0B0A000908070605040301111"
			String serverIP= taskConfig.getProperty(PROPERTY_SERVER_IP); //"localhost"
			
			if(serviceName == null){
				serviceName = "OpenMRS XForms Bluetooth Server";
				taskConfig.setProperty(PROPERTY_SERVICE_NAME, serviceName);
				log.error("Property "+PROPERTY_SERVICE_NAME+" was null. Set to "+serviceName);
			}
			
			if(serverUUID == null){
				serverUUID = "F0E0D0C0B0A000908070605040301111";
				taskConfig.setProperty(PROPERTY_SERVER_UUID, serverUUID);
				log.error("Property "+PROPERTY_SERVER_UUID+" was null. Set to "+serverUUID);
			}
			
			if(serverIP == null){
				serverIP = "localhost";
				taskConfig.setProperty(PROPERTY_SERVER_IP, serverIP);
				log.error("Property "+PROPERTY_SERVER_IP+" was null. Set to "+serverIP);
			}
						
			server = new XformsBluetoothServer(serviceName,serverUUID,serverIP);
			
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
		System.out.println("shutdown called.");
		server.stop();
		System.out.println("bluetoth stopped.");
	}
	
	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
		taskConfig.getProperty("name"); //Just to ensure we dont get exceptons on accessing this object else where.
	}
	
	private void authenticate() {
		try {
			AdministrationService adminService = Context.getAdministrationService();
			Context.authenticate(adminService.getGlobalProperty("scheduler.username"),
				adminService.getGlobalProperty("scheduler.password"));
			
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
	}
	
	protected void finalize(){
		
	}
}
