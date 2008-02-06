package org.openmrs.module.xforms.sms;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
		try{
			xformsServer.processConnection(dis, dos);
		}catch(Exception e){
			log.error(e);
			try{//TODO We need a smart way of sending error SMSs
				dos.writeByte(XformsServer.STATUS_FAILURE);
				//dos.writeUTF(e.getMessage());
			}catch(IOException ex){
				log.error(ex);
			}
		}
	}
	
	public void errorOccured(String errorMessage, Exception e){
		log.error(errorMessage, e);
	}
}
