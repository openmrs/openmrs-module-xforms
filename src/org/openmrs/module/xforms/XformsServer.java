package org.openmrs.module.xforms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.download.PatientDownloadManager;
import org.openmrs.module.xforms.download.UserDownloadManager;
import org.openmrs.module.xforms.download.XformDataUploadManager;
import org.openmrs.module.xforms.download.XformDownloadManager;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

/**
 * Serves xform services to non HTTP connections. Examples of such connections
 * can be SMS, Bluetooth, Data cable, etc.
 * 
 * @author Daniel
 * 
 */
public class XformsServer {

    /** Value representing a not yet set status. */
    public static final byte STATUS_NULL = -1;
    
    /** Value representing success of an action. */
    public static final byte STATUS_SUCCESS = 1;

    /** Value representing failure of an action. */
    public static final byte STATUS_FAILURE = 0;

    /** Action to get a list of form definitions. */
    public static final byte ACTION_DOWNLOAD_FORMS = 3;

    /** Action to save a list of form data. */
    public static final byte ACTION_UPLOAD_FORMS = 5;

    /** Action to download a list of patients from the server. */
    public static final byte ACTION_DOWNLOAD_PATIENTS = 6;

    /** Action to download a list of patients from the server. */
    public static final byte ACTION_DOWNLOAD_USERS = 7;
    
    /** Action to download a list of users and forms from the server. */
    public static final byte ACTION_DOWNLOAD_COHORTS = 8;

    /** Action to download a list of users and forms from the server. */
    public static final byte ACTION_DOWNLOAD_USERS_AND_FORMS = 11;

    /** The IP address of the server. */
    private String serverIP;

    private Log log = LogFactory.getLog(this.getClass());

    public XformsServer(String serverIP) {
        this.serverIP = serverIP;
    }

    /**
     * Called when a new connection has been received. Failures are not handled
     * in this class as different servers (BT,SMS, etc) may want to handle them
     * differently.
     * 
     * @param dis - the stream to read from.
     * @param dos - the stream to write to.
     */
    public void processConnection(DataInputStream dis, DataOutputStream dosParam)
            throws IOException, ContextAuthenticationException, Exception {
        String name = dis.readUTF();
        String pw = dis.readUTF();
        Context.authenticate(name, pw);

        String serializer = dis.readUTF();
 
        byte action = dis.readByte();

        //System.out.println("processConnection called with action="+action);
        
        //GZIPOutputStream gzip = new GZIPOutputStream(dosParam);
        ZOutputStream gzip = new ZOutputStream(dosParam,JZlib.Z_BEST_COMPRESSION);
        DataOutputStream dos = new DataOutputStream(gzip);

        if (action == ACTION_DOWNLOAD_PATIENTS)
            PatientDownloadManager.downloadPatients(String.valueOf(dis.readInt()), dos);
        else if(action == ACTION_DOWNLOAD_COHORTS)
            PatientDownloadManager.downloadCohorts(dos);
        else if (action == ACTION_DOWNLOAD_FORMS)
            XformDownloadManager.downloadXforms(dos);
        else if (action == ACTION_UPLOAD_FORMS)
            submitXforms(dis, dos);
        else if (action == ACTION_DOWNLOAD_USERS)
            UserDownloadManager.downloadUsers(dos);
        else if (action == ACTION_DOWNLOAD_USERS_AND_FORMS)
            downloadUsersAndForms(dos);

        dos.flush();
        gzip.finish();
        
        //System.out.println("finished processConnection with action="+action);
    }

    // TODO I guess this needs to be done in a smarter way.
    private String getActionUrl() {
        // HttpServletRequest request =
        // WebContextFactory.get().getHttpServletRequest();
        // HttpSession session = WebContextFactory.get().getSession();
        return "http://" + serverIP + ":8080/openmrs"
                + XformConstants.XFORM_DATA_UPLOAD_RELATIVE_URL;
    }

    /**
     * Saves xforms xml models.
     * 
     * @param dis - the stream to read from.
     * @param dos - the stream to write to.
     */
    private void submitXforms(DataInputStream dis, DataOutputStream dos) throws Exception {
        XformDataUploadManager.submitXforms(dis, new java.util.Date().toString());
        try {
            dos.writeByte(STATUS_SUCCESS);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            dos.writeByte(STATUS_FAILURE);
        }
    }

    /**
     * Downloads a list of users and xforms.
     * 
     * @param dos - the stream to write to.
     * @throws Exception
     */
    private void downloadUsersAndForms(DataOutputStream dos) throws Exception {
        UserDownloadManager.downloadUsers(dos);
        XformDownloadManager.downloadXforms(dos);
    }
}
