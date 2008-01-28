package org.openmrs.module.xforms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.bluetooth.XformsBluetoothTask;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.scheduler.Schedulable;


/**
 * This class implements the task which processes forms in the xforms queue.
 * 
 * @author Daniel Kayiwa
 * @version 1.0
 */
public class ProcessXformsQueueTask implements Schedulable{

//	 Logger
	private static Log log = LogFactory.getLog(ProcessXformsQueueTask.class);

	// Task configuration 
	private TaskConfig taskConfig;
	
	//Instance of xforms processor.
	private XformsQueueProcessor processor = null;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 * 
	 */
	public ProcessXformsQueueTask() {
		if (processor == null)
			processor = new XformsQueueProcessor();
	}

	/**
	 * Process the next xform in the queue and then remove the xform
	 * from the queue.
	 */
	public void run() {
		Context.openSession();
		log.debug("Running xforms queue task... ");
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			
			processor.processXformsQueue();
			
		} catch (APIException e) {
			log.error("Error running xforms queue task", e);
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
