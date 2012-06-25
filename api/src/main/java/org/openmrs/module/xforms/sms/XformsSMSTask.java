package org.openmrs.module.xforms.sms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;


/**
 * This class implements the task which handles SMS connection requests for xforms
 * together with the corresponding user and patient downloads.
 * 
 * @author Daniel
 * @version 1.0
 */
public class XformsSMSTask extends AbstractTask{

//	 Logger
	private static Log log = LogFactory.getLog(XformsSMSTask.class);
	
	private static final String PROPERTY_MODEM_PORT = "ModemPort";
	private static final String PROPERTY_SERVER_IP = "ServerIP";
	private static final String PROPERTY_MSG_DEST_PORT = "MsgSrcPort";
	private static final String PROPERTY_MSG_SRC_PORT = "MsgDestPort";
	private static final String PROPERTY_MODEM_BAUDRATE = "ModemBaudRate";
	private static final String PROPERTY_MODEM_MANUFACTURER = "ModemManufacturer";
	private static final String PROPERTY_MODEM_MODEL = "ModemModel";
	private static final String PROPERTY_MODEM_ID = "ModemId";
	
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
	public void execute() {
		Context.openSession();
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			
			String serverIP = taskDefinition.getProperty(PROPERTY_SERVER_IP);
			String msgSrcPort = taskDefinition.getProperty(PROPERTY_MSG_SRC_PORT);
			String msgDestPort = taskDefinition.getProperty(PROPERTY_MSG_DEST_PORT);
			String modemPort = taskDefinition.getProperty(PROPERTY_MODEM_PORT);
			String modemBaudRate= taskDefinition.getProperty(PROPERTY_MODEM_BAUDRATE);
			String modemManufacturer = taskDefinition.getProperty(PROPERTY_MODEM_MANUFACTURER);
			String modemModel = taskDefinition.getProperty(PROPERTY_MODEM_MODEL);
			String modemId = taskDefinition.getProperty(PROPERTY_MODEM_ID);
			
			if(serverIP == null){
				serverIP = "localhost";
				taskDefinition.setProperty(PROPERTY_SERVER_IP, serverIP);
				log.error("Property "+PROPERTY_SERVER_IP+" was null. Set to "+serverIP);
			}
			
			if(msgSrcPort == null){
				msgSrcPort = "1234";
				taskDefinition.setProperty(PROPERTY_MSG_SRC_PORT, msgSrcPort);
				log.error("Property "+PROPERTY_MSG_SRC_PORT+" was null. Set to "+msgSrcPort);
			}
			
			if(msgDestPort == null){
				msgDestPort = "1234";
				taskDefinition.setProperty(PROPERTY_MSG_DEST_PORT, msgDestPort);
				log.error("Property "+PROPERTY_MSG_DEST_PORT+" was null. Set to "+msgDestPort);
			}
			
			if(modemBaudRate == null){
				modemBaudRate = "9600";
				taskDefinition.setProperty(PROPERTY_MODEM_BAUDRATE, modemBaudRate);
				log.error("Property "+PROPERTY_MODEM_BAUDRATE+" was null. Set to "+modemBaudRate);
			}
			
			if(modemPort == null){
				modemPort = "COM1";
				taskDefinition.setProperty(PROPERTY_MODEM_PORT, modemPort);
				log.error("Property "+PROPERTY_MODEM_PORT+" was null. Set to "+modemPort);
			}
			
			if(modemManufacturer == null){
				modemManufacturer = "Nokia";
				taskDefinition.setProperty(PROPERTY_MODEM_MANUFACTURER, modemManufacturer);
				log.error("Property "+PROPERTY_MODEM_MANUFACTURER+" was null. Set to "+modemManufacturer);
			}
			
			if(modemModel == null){
				modemModel = "6020";
				taskDefinition.setProperty(PROPERTY_MODEM_MODEL, modemModel);
				log.error("Property "+PROPERTY_MODEM_MODEL+" was null. Set to "+modemModel);
			}
			
			if(modemId == null){
				modemId = "OpenMRS SMS Gateway Modem";
				taskDefinition.setProperty(PROPERTY_MODEM_ID, modemId);
				log.error("Property "+PROPERTY_MODEM_ID+" was null. Set to "+modemId);
			}
				
			System.out.println("ServerIP Value="+serverIP);
			System.out.println("MsgSrcPort Value="+msgSrcPort);
			System.out.println("MsgDestPort Value="+msgDestPort);
			System.out.println("ModemBaudRate Value="+modemBaudRate);
			System.out.println("ModemPort Value="+modemPort);
			System.out.println("ModemManufacturer Value="+modemManufacturer);
			System.out.println("ModemModel Value="+modemModel);
			System.out.println("ModemId Value="+modemId);
			
			server = new XformsSMSServer(modemId, modemPort,Integer.valueOf(msgDestPort),Integer.valueOf(msgSrcPort), Integer.valueOf(modemBaudRate), modemManufacturer, modemModel);
			server.start();

		} catch (APIException e) {
			log.error("Error running XForms SMS task", e);
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
