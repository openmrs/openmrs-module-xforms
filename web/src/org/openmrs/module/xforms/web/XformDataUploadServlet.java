package org.openmrs.module.xforms.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.download.XformDataUploadManager;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;


/**
 * Provides XForm data upload services.
 * 
 * @author Daniel
 *
 */
public class XformDataUploadServlet extends HttpServlet{

public static final long serialVersionUID = 1234278783771156L;
    
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * This just delegates to the doGet()
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        byte status = -1;
        
        try{
            //try to authenticate users who logon inline (with the request).
            XformsUtil.authenticateInlineUser(request);
            
            // check if user is authenticated
            if (XformsUtil.isAuthenticated(request,response,"/moduleServlet/xforms/xformDataUpload")){
                response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
                
                //check if external client sending multiple filled forms.
                if(XformConstants.TRUE_TEXT_VALUE.equalsIgnoreCase(request.getParameter(XformConstants.REQUEST_PARAM_BATCH_ENTRY))){                           
                    try{
                		DataInputStream dis = new DataInputStream(request.getInputStream());
                		String name = dis.readUTF();
                		String password = dis.readUTF();
                		String serializer = dis.readUTF();
                		byte action = dis.readByte();
                		
                        XformDataUploadManager.submitXforms(dis,request.getSession().getId());
                        status = XformsServer.STATUS_SUCCESS;
                      }
                    catch(Exception e){
                        log.error( e.getMessage(),e);
                        status = XformsServer.STATUS_FAILURE; 
                    }                
                }
                else{ //else single form filled from browser.
                    XformDataUploadManager.processXform(IOUtils.toString(request.getInputStream()),request.getSession().getId(),XformsUtil.getEnterer());
                    setSingleEntryResponse(request, response);
                }
            } 
            
            if(status != -1){ //if non web client (who will either have faulure (0) or success (1)) 
                //GZIPOutputStream gzip = new GZIPOutputStream(response.getOutputStream());
                ZOutputStream gzip = new ZOutputStream(response.getOutputStream(),JZlib.Z_BEST_COMPRESSION);
                DataOutputStream dos = new DataOutputStream(gzip);
                dos.writeByte(status);
                dos.flush();
                gzip.finish();
            }
        }
        catch(Exception e){
            log.error(e.getMessage(),e);
        }
    }
    
    /**
     * Write the response after processing an xform submitted from the browser.
     * 
     * @param request - the request.
     * @param response - the response.
     */
    private void setSingleEntryResponse(HttpServletRequest request, HttpServletResponse response){
        
        String searchNew = Context.getAdministrationService().getGlobalProperty("xforms.searchNewPatientAfterFormSubmission");
        String url = "/findPatient.htm";
        if(XformConstants.FALSE_TEXT_VALUE.equalsIgnoreCase(searchNew)){
            String patientId = request.getParameter(XformConstants.REQUEST_PARAM_PATIENT_ID);
            url = "/patientDashboard.form?patientId="+patientId;
        }
        url = request.getContextPath() + url;
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=utf-8");
        
        try{
            //We are using an iframe to display the xform within the page.
            //So this response just tells the iframe parent document to go to either the
            //patient dashboard, or to the search patient screen, depending on the user's settings.
            response.getOutputStream().println("<html>" + "<head>"
                    +"<script type='text/javascript'> window.onload=function() {self.parent.location.href='" + url + "';}; </script>"
                    +"</head>" + "</html>");
        }
        catch(IOException e){
            log.error(e.getMessage(),e);
        }
    }
}
