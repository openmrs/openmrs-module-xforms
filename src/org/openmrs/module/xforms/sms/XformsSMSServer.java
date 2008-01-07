package org.openmrs.module.xforms.sms;


import dk.daimi.jones.services.sms.NMIListener;
import dk.daimi.jones.services.sms.SMSMessage;
import org.fcitmuk.communication.sms.SMSServer;

/**
 * Serves xforms services to SMS connections.
 * 
 * @author Daniel
 *
 */
public class XformsSMSServer  implements NMIListener{

	private SMSServer server;
	
	public XformsSMSServer(String port){
		server = new SMSServer(port,this);
	}
	
	public void start(){
		server.start();
	}
	
	public void stop(){
		server.stop();
	}
	
	public void messageReceived(SMSMessage sms){
		processMessage(sms);
	}
	
	private void processMessage(SMSMessage sms){
		System.out.println("Received SMS from '" + sms.getAddress() + "'");
		System.out.println("Text: '" + sms.getData() + "'");
	}
}
