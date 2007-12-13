package org.openmrs.module.xforms.web.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.openmrs.api.PatientService;
//import org.openmrs.api.PersonService;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.openmrs.Cohort;
import org.openmrs.Patient;
//import org.openmrs.Person;
import org.openmrs.module.xforms.*;

import java.io.*;

//TODO This class may need to be refactored out of the XForms module.

/**
 * Privides patient download services.
 * 
 * @author Daniel
 *
 */
public class PatientDownloadController extends SimpleFormController{
	
	/** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    private List<Cohort> cohorts;
        	    
    @Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

		Map<Object, Object> data = new HashMap<Object, Object>();
		cohorts = Context.getCohortService().getCohorts();
		data.put("cohorts", cohorts);

		return data;
	}


	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {						
		
		//try to authenticate users who logon inline (with the request).
		XformsUtil.authenticateInlineUser(request);
		
		//check for authenticated users
		if (!Context.isAuthenticated()) {
			request.getSession().setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR,
				request.getContextPath() + "/module/xforms/xformUpload.form");
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String cohortId = request.getParameter("cohortId");
		if(cohortId == null)
			cohortId = Context.getAdministrationService().getGlobalProperty("xforms.patientDownloadCohort");
		
		if(cohortId != null && cohortId.length() > 0){
			if(request.getParameter("downloadPatients") != null){
				response.setHeader("Content-Disposition", "attachment; filename=" + getCohortName(Integer.parseInt(cohortId))+".xml");
				
				String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_SERIALIZER);
				if(className == null || className.length() == 0)
					className = XformConstants.DEFAULT_PATIENT_SERIALIZER;
				//SerializableData sr = (SerializableData)Class.forName(className).newInstance();
				SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
				
				Cohort cohort = Context.getCohortService().getCohort(Integer.parseInt(cohortId));
				Set<Integer> patientIds = cohort.getMemberIds();
				response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
				sr.serialize(new DataOutputStream(response.getOutputStream()),getPantients(patientIds));
			}
			else if(request.getParameter("setCohort") != null){
				Context.getAdministrationService().setGlobalProperty("xforms.patientDownloadCohort", cohortId);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.setPatientDownloadCohortSuccess");
				return new ModelAndView(new RedirectView(getSuccessView()));
			}
		}
		
		if(request.getParameter("uploadPatientXform") != null){//TODO This needs to be refactored with the XformUploadController
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			MultipartFile xformFile = multipartRequest.getFile("patientXformFile");
			if (xformFile != null && !xformFile.isEmpty()) {
				XformsService xformsService = (XformsService)Context.getService(XformsService.class);
				String xml = IOUtils.toString(xformFile.getInputStream());
				Xform xform = new Xform();
				xform.setFormId(XformConstants.PATIENT_XFORM_FORM_ID);
				xform.setXformData(xml);
				xformsService.saveXform(xform);
				
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.patientXformUploadSuccess");
			}
			return new ModelAndView(new RedirectView(getSuccessView()));
		}
		else if(request.getParameter("downloadPatientXform") != null){
			String filename = XformConstants.PATIENT_XFORM_FORM_ID+".xml";
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.getOutputStream().print(XformBuilder.getNewPatientXform("testing"));
		}
		
		return null;
    }
	
	private List<Patient> getPantients(Collection<Integer> patientIds){
		List<Patient> patients = new ArrayList<Patient>();
		
		PatientService patientService = Context.getPatientService();
		for(Integer patientId : patientIds)
			patients.add(patientService.getPatient(patientId));
		
		return patients;
	}
	
    @Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception { 
    	return "";
    }
    
    private String getCohortName(int id){
    	String name = Integer.toString(id);
    	if(cohorts == null)
    		cohorts = Context.getCohortService().getCohorts();
    	
		for(Cohort cohort : cohorts){
			if(cohort.getCohortId() == id)
				name = cohort.getName();
		}

		return name.replace(" ", "_");
    }
}
