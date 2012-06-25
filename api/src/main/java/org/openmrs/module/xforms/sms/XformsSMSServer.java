package org.openmrs.module.xforms.sms;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fcitmuk.communication.sms.SMSServer;
import org.fcitmuk.communication.sms.SMSServerListener;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.download.XformDataUploadManager;


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
	private FormSmsParser formsSmsParser;
	
	
	/** Flag to determine whether the sms sender wants success reports. */
	private boolean smsSendSuccessReports = true;
	
	/** Flag to determine if the sms sender wants failure reports. */
	private boolean smsSendFailureReports = true;
	
	 	
	public XformsSMSServer(String id,String comPort, int msgDstPort, int msgSrcPort, int baudRate, String manufacturer, String model){
		smsServer = new SMSServer(id,comPort,msgDstPort,msgSrcPort,baudRate,manufacturer,model,this);
		xformsServer = new XformsServer();
		formsSmsParser = new FormSmsParser();
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
            log.error(e.getMessage(),e);
			try{//TODO We need a smart way of sending error SMSs
				dos.writeByte(XformsServer.STATUS_FAILURE);
				//dos.writeUTF(e.getMessage());
			}catch(IOException ex){
				log.error(ex);
			}
		}
	}
	
	public String processMessage(String sender, String text){
		String reply = null;

		try{
			Context.openSession();

			loadSettings();
			
			XformDataUploadManager.queueForm(formsSmsParser.sms2FormXml(sender, text),true,null);

			/*FormSmsArchive formSmsArchive = new FormSmsArchive(new FormDataArchive(formData));
			formSmsArchive.setSender(sender);
			formSmsArchive.setData(text);
			formSmsArchive.setArchiveCreator(formData.getCreator());
			formSmsArchive.setArchiveDateCreated(formData.getDateCreated());

			Context.getFormDownloadService().saveFormSmsArchive(formSmsArchive);*/

			if(smsSendSuccessReports)
				reply = "Message received and processed sucessfully.";
		}
		catch(Exception ex){
			ex.printStackTrace();

			if(smsSendFailureReports){
				reply = ex.getMessage();
				if(reply == null)
					reply = "Errors occured while processing message on the server. Please report this error to the administrator.";
			}

			try{
				;//Context.getFormDownloadService().saveFormSmsError(new FormSmsError(sender,text,new Date(),null,ex.getMessage()));
			}
			catch(Exception e){
				e.printStackTrace();
				
				if(smsSendFailureReports)
					reply += " " +  e.getMessage();
			}
		}
		finally{
			Context.closeSession();
		}

		return reply;
	}
	
	public void errorOccured(String errorMessage, Exception e){
		log.error(errorMessage, e);
	}
	
	private void loadSettings(){
		String val = Context.getAdministrationService().getGlobalProperty("xforms.smsSendSuccessReports");
		if("false".equalsIgnoreCase(val))
			smsSendSuccessReports = false;
		
		val = Context.getAdministrationService().getGlobalProperty("xforms.smsSendFailureReports");
		if("false".equalsIgnoreCase(val))
			smsSendFailureReports = false;
	}
}
