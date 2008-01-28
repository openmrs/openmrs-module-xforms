package org.openmrs.module.xforms.sms;



import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fcitmuk.communication.sms.SMSServer;
import org.fcitmuk.communication.sms.SMSServerListener;
import org.openmrs.module.xforms.XformsServer;

/**
 * Serves xforms services to SMS connections.
 * 
 * @author Daniel
 *
 */
public class XformsSMSServer  implements SMSServerListener{

	private Log log = LogFactory.getLog(this.getClass());
	
	private SMSServer smsServer;
	private XformsServer xformsServer;
	 	
	public XformsSMSServer(String id,String comPort, int msgDstPort, int msgSrcPort, int baudRate, String manufacturer, String model, String serverIP){
		smsServer = new SMSServer(id,comPort,msgDstPort,msgSrcPort,baudRate,manufacturer,model,this);
		xformsServer = new XformsServer(serverIP);
	}
	
	public void start(){
		smsServer.start();
	}
	
	public void stop(){
		if(smsServer != null)
			smsServer.stop();
	}
	
	public void processMessage(DataInputStream dis, DataOutputStream dos){
		xformsServer.processConnection(dis, dos);
	}
	
	public void errorOccured(String errorMessage, Exception e){
		log.error(errorMessage, e);
	}
}
