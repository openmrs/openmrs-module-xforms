package org.openmrs.module.xforms.web.controller;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.Document;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.RelationshipSubmission;
import org.openmrs.module.xforms.RelativeSubmission;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformObsEdit;
import org.openmrs.module.xforms.XformPatientEdit;
import org.openmrs.module.xforms.XformsServer;
import org.openmrs.module.xforms.download.XformDataUploadManager;
import org.openmrs.module.xforms.util.XformsUtil;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

//TODO This class is to be deleted as it functionality is now done by XformDataUploadServlet

/**
 * Provides XForm data upload services from the web interface. Encounter form filling or editing of
 * form obs data from the web interface submit data through this controller.
 * 
 * @author Daniel
 */
public class XformDataUploadController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		return new HashMap<String, Object>();
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object,
	                                BindException exceptions) throws Exception {
		
		byte status = XformsServer.STATUS_NULL;
		
		//try to authenticate users who logon inline (with the request).
		XformsUtil.authenticateInlineUser(request);
		
		PrintWriter writer = response.getWriter();
		
		// check if user is authenticated
		if (XformsUtil.isAuthenticated(request, response, "/module/xforms/xformDataUpload.form")) {
			response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
			
			//check if external client sending multiple filled forms.
			//These are normally mobile clients.
			if (XformConstants.TRUE_TEXT_VALUE.equalsIgnoreCase(request
			        .getParameter(XformConstants.REQUEST_PARAM_BATCH_ENTRY))) {
				try {
					String serializerKey = request.getParameter("serializer");
					if (serializerKey == null || serializerKey.trim().length() == 0)
						serializerKey = XformConstants.GLOBAL_PROP_KEY_XFORM_SERIALIZER;
					
					XformDataUploadManager.submitXforms(request.getInputStream(), request.getSession().getId(),
					    serializerKey);
					status = XformsServer.STATUS_SUCCESS;
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
					status = XformsServer.STATUS_FAILURE;
				}
			} else { //else single form filled from browser.
				String xml = IOUtils.toString(request.getInputStream(), XformConstants.DEFAULT_CHARACTER_ENCODING);
				
				try {
					request.setAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_ERROR_MESSAGE, null);
					request.setAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_PATIENT_ID, null);
					
					if ("edit".equals(request.getParameter("mode")))
						processXformEdit(request, xml);
					else
						XformDataUploadManager.processXform(xml, request.getSession().getId(), XformsUtil.getEnterer(),
						    true, request);
					
					Object id = request.getAttribute(XformConstants.REQUEST_ATTRIBUTE_ID_PATIENT_ID);
					if (id != null)
						writer.print(id.toString());
					
					response.setStatus(HttpServletResponse.SC_OK);
				}
				catch (Exception ex) {//HttpServletRequest request
					XformsUtil.reportDataUploadError(ex, request, response, writer);
				}
			}
		}
		
		if (status != XformsServer.STATUS_NULL) {
			//send compressed response for mobile device.
			ZOutputStream gzip = new ZOutputStream(response.getOutputStream(), JZlib.Z_BEST_COMPRESSION);
			DataOutputStream dos = new DataOutputStream(gzip);
			dos.writeByte(status);
			dos.flush();
			gzip.finish();
		}
		
		return null;
	}
	
	private void processXformEdit(HttpServletRequest request, String xml) throws Exception {
		xml = XformsUtil.replaceConceptMaps(xml);
		Document doc = XformBuilder.getDocument(xml);
		if (XformPatientEdit.isPatientElement(doc.getRootElement())) {
			Patient patient = XformPatientEdit.getEditedPatient(request, doc.getRootElement());
			Context.getPatientService().savePatient(patient);
		} else {
			Set<Obs> obs2Void = new HashSet<Obs>();
			Encounter encounter = XformObsEdit.getEditedEncounter(request, doc.getRootElement(), obs2Void);
			
			//TODO These two below need to be put in a transaction
			Context.getEncounterService().saveEncounter(encounter);
			
			ObsService obsService = Context.getObsService();
			for (Obs obs : obs2Void)
				obsService.voidObs(obs, "xformsmodule");
			
			RelativeSubmission.submit(xml, encounter.getPatient());
		}
	}
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "";
	}
}
