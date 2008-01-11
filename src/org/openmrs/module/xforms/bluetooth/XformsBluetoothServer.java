package org.openmrs.module.xforms.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPOutputStream;

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
	
	/** Action to download a list of users and forms from the server. */
	public static final byte ACTION_DOWNLOAD_USERS_AND_FORMS = 11;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/** The IP address of the server. */
	private String serverIP;
	
	private BluetoothServer server;
	
	/** Constructs an xforms bluetooth server instance. */
	public XformsBluetoothServer(String name, String serverUUID,String serverIP){
		this.serverIP = serverIP;
		server = new BluetoothServer(name,serverUUID,this);
	}
	
	/** Stop this server from running. */
	public void stop(){
		if(server != null)
			server.destroy();
	}
	
	/**
	 * Called when a new connection has been received.
	 * 
	 * @param dis - the stream to read from.
	 * @param dos - the stream to write to.
	 */
	public void processConnection(DataInputStream dis, DataOutputStream dosParam){
		try{		 			
			 String name = dis.readUTF();
			 String pw = dis.readUTF();
			 Context.authenticate(name, pw);
			 
			 byte action = dis.readByte();
			 
			 GZIPOutputStream gzip = new GZIPOutputStream(dosParam);
			 DataOutputStream dos = new DataOutputStream(gzip);
				
			 if(action == ACTION_DOWNLOAD_PATIENTS)
				 PatientDownloadManager.downloadPatients(null, dos);
			 else if(action == ACTION_DOWNLOAD_FORMS)
				 XformDownloadManager.downloadXforms(getActionUrl(), dos);
			 else if(action == ACTION_UPLOAD_FORMS)
				 XformDownloadManager.downloadXforms(getActionUrl(), dos);
			 else if(action == ACTION_DOWNLOAD_USERS)
				 UserDownloadManager.downloadUsers(dos);
			 else if(action == ACTION_DOWNLOAD_USERS_AND_FORMS)
				 downloadUsersAndForms(dos);
			 
			 dos.flush();
			 gzip.finish();
		}
		catch(Exception e){
			log.error(e);
			e.printStackTrace();
		}
	}
	
	//TODO I guess this needs to be done in a smarter way.
	private String getActionUrl(){
		//HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		//HttpSession session = WebContextFactory.get().getSession();
		return "http://" + serverIP + ":8080/openmrs/module/xforms/xformDataUpload.form";
	}
			
	/**
	 * Downloads a list of users and xforms.
	 * 
	 * @param dos - the stream to write to.
	 * @throws Exception
	 */
	private void downloadUsersAndForms(DataOutputStream dos) throws Exception{
		UserDownloadManager.downloadUsers(dos);
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
