package org.openmrs.module.xforms.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fcitmuk.communication.bluetooth.BluetoothServer;
import org.fcitmuk.communication.bluetooth.BluetoothServerListener;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.download.*;


/**
 * Serves xforms services to bluetooth connections.
 * 
 * @author Daniel
 *
 */
public class XformsBluetoothServer  implements BluetoothServerListener  {

	/** Action to get a list of form definitions. */
	public static final byte ACTION_DOWNLOAD_FORMS = 3;
	
	/** Action to save a list of form data. */
	public static final byte ACTION_UPLOAD_FORMS = 5;
	
	/** Action to download a list of patients from the server. */
	public static final byte ACTION_DOWNLOAD_PATIENTS = 6;
	
	/** Action to download a list of patients from the server. */
	public static final byte ACTION_DOWNLOAD_USERS = 7;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/** The IP address of the server. */
	private String serverIP;
	
	
	/** Constructs an xforms bluetooth server instance. */
	public XformsBluetoothServer(String name, String serverUUID,String serverIP){
		this.serverIP = serverIP;
		new BluetoothServer(name,serverUUID,this);
	}
	
	/**
	 * Called when a new connection has been received.
	 * 
	 * @param dis - the stream to read from.
	 * @param dos - the stream to write to.
	 */
	public void processConnection(DataInputStream dis, DataOutputStream dos){
		try{		 			
			 String name = dis.readUTF();
			 String pw = dis.readUTF();
			 Context.authenticate(name, pw);
			 
			 byte action = dis.readByte();
			 
			 if(action == ACTION_DOWNLOAD_PATIENTS)
				 downloadPatients(dis,dos);
			 else if(action == ACTION_DOWNLOAD_FORMS)
				 downloadForms(dis,dos);
			 else if(action == ACTION_UPLOAD_FORMS)
				 uploadForms(dis,dos);
			 else if(action == ACTION_DOWNLOAD_USERS)
				 downloadUsers(dis,dos);
		}
		catch(Exception e){
			log.error(e);
		}
	}
	
	private String getActionUrl(){
		//HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		//HttpSession session = WebContextFactory.get().getSession();
		return "http://" + serverIP + ":8080/openmrs/module/xforms/xformDataUpload.form";
	}
	
	private void downloadUsers(DataInputStream dis, DataOutputStream dos) throws Exception{
		//String locationUrl = "http://localhost:8080/openmrs/moduleServlet/xforms/userDownload?uname=guyzb&pw=daniel123";
		//this.processConnection(clientDis, clientDos, comnParam, locationUrl,"application/x-www-form-urlencoded");
		UserDownloadManager.downloadUsers(dos);
		
	}
	
	private void downloadPatients(DataInputStream dis, DataOutputStream dos) throws Exception{
		//String locationUrl = "http://localhost:8080/openmrs/module/xforms/patientDownload.form?downloadPatients=true&uname=guyzb&pw=daniel123";
		//this.processConnection(clientDis, clientDos, comnParam, locationUrl,"application/x-www-form-urlencoded");
		PatientDownloadManager.downloadPatients(null, dos);
	}
	
	private void downloadForms(DataInputStream dis, DataOutputStream dos) throws Exception{
		//72.249.82.103
		//String locationUrl = "http://localhost:8080/openmrs/moduleServlet/xforms/xformDownload?target=xforms&createNew=true&uname=guyzb&pw=daniel123";
		//this.processConnection(clientDis, clientDos, comnParam, locationUrl,"application/x-www-form-urlencoded");
		XformDownloadManager.downloadXforms(getActionUrl(), dos);
	}
	
	private void uploadForms(DataInputStream dis, DataOutputStream dos) throws Exception{
		//String locationUrl = "http://localhost:8080/openmrs/module/xforms/xformDataUpload.form?batchEntry=true&uname=guyzb&pw=daniel123";
		//this.processConnection(clientDis, clientDos, comnParam, locationUrl, "application/octet-stream");
		XformDownloadManager.downloadXforms(getActionUrl(), dos);
	}
	
	/**
	 * Called when an error occurs during processing.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e){
		log.error(errorMessage, e);
	}
}
