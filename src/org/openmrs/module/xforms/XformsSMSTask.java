package org.openmrs.module.xforms;

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
	
//	 Instance of sms server.
	//private SMSServer server = null;
	
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
			
			System.out.println("ServerPort Value="+taskConfig.getProperty("ServerPort"));
			System.out.println("ModemPort Value="+taskConfig.getProperty("ModemPort"));

			//processor.processFormEntryQueue();
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
		
	}
	
	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
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
