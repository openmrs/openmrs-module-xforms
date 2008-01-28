package org.openmrs.module.xforms.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fcitmuk.communication.bluetooth.BluetoothServer;
import org.fcitmuk.communication.bluetooth.BluetoothServerListener;
import org.openmrs.module.xforms.XformsServer;


/**
 * Serves xforms services to bluetooth connections.
 * 
 * @author Daniel
 *
 */
public class XformsBluetoothServer  implements BluetoothServerListener  {
	
	private Log log = LogFactory.getLog(this.getClass());
		
	private BluetoothServer btServer;
	private XformsServer xformsServer;
	
	/** Constructs an xforms bluetooth server instance. */
	public XformsBluetoothServer(String name, String serverUUID,String serverIP){
		btServer = new BluetoothServer(name,serverUUID,this);
		xformsServer = new XformsServer(serverIP);
	}
	
	/** Stop this server from running. */
	public void stop(){
		if(btServer != null)
			btServer.destroy();
	}
	
	/**
	 * Called when a new connection has been received.
	 * 
	 * @param dis - the stream to read from.
	 * @param dos - the stream to write to.
	 */
	public void processConnection(DataInputStream dis, DataOutputStream dos){
		xformsServer.processConnection(dis, dos);
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
