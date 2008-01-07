package org.openmrs.module.xforms.sms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.scheduler.Schedulable;


/**
 * This class implements the task which handles SMS connection requests for xforms
 * together with the corresponding user and patient downloads.
 * 
 * @author Daniel
 * @version 1.0
 */
public class XformsSMSTask implements Schedulable{

//	 Logger
	private static Log log = LogFactory.getLog(XformsSMSTask.class);

	// Task configuration 
	private TaskConfig taskConfig;
	
	private static final String PROPERTY_MODEM_PORT = "ModemPort";
	private static final String PROPERTY_SERVER_PORT = "ServerPort";
	
//	 Instance of sms server.
	private XformsSMSServer server = null;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 * 
	 */
	public XformsSMSTask() {
		/*if (server == null)
			server = new SMSServer();*/
	}

	/**
	 * Process incoming SMSs.
	 */
	public void run() {
		Context.openSession();
		log.debug("Running xforms SMS task... ");
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			
			String serverPort = taskConfig.getProperty(PROPERTY_SERVER_PORT);
			String modemPort = taskConfig.getProperty(PROPERTY_MODEM_PORT);
			
			if(serverPort == null){
				serverPort = "1234";
				taskConfig.setProperty(PROPERTY_SERVER_PORT, serverPort);
				log.error("Property "+PROPERTY_SERVER_PORT+" was null. Set to "+serverPort);
			}
			
			if(modemPort == null){
				modemPort = "COM1";
				taskConfig.setProperty(PROPERTY_MODEM_PORT, modemPort);
				log.error("Property "+PROPERTY_MODEM_PORT+" was null. Set to "+modemPort);
			}
				
			System.out.println("ServerPort Value="+serverPort);
			System.out.println("ModemPort Value="+modemPort);
			
			server = new XformsSMSServer(modemPort);
			server.start();

		} catch (APIException e) {
			log.error("Error running xforms SMS task", e);
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
		System.out.println("Stopping SMS Server Task");
		server.stop();
	}
	
	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
		System.out.println("called init");
		taskConfig.getProperty("ModemPort"); //Just to ensure we dont get exceptons on accessing this object else where.
		//System.out.println("after init");
		//System.out.println("ServerPort Value="+taskConfig.getProperty("ServerPort"));
		//System.out.println("ModemPort Value="+taskConfig.getProperty("ModemPort"));
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
}
