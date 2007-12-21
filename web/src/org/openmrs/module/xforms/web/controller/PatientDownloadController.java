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
				
		//check if user is authenticated
		if (!XformsUtil.isAuthenticated(request,response,"/module/xforms/patientDownload.form"))
			return null;
		
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);

		String cohortId = request.getParameter(XformConstants.REQUEST_PARAM_COHORT_ID);
		if(cohortId == null)
			cohortId = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_DOWNLOAD_COHORT);
		
		if(cohortId != null && cohortId.length() > 0){
			if(request.getParameter(XformConstants.REQUEST_PARAM_DOWNLOAD_PATIENTS) != null){
				response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + getCohortName(Integer.parseInt(cohortId))+XformConstants.XML_FILE_EXTENSION);
				
				String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_SERIALIZER);
				if(className == null || className.length() == 0)
					className = XformConstants.DEFAULT_PATIENT_SERIALIZER;
				//SerializableData sr = (SerializableData)Class.forName(className).newInstance();
				SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
				
				response.setCharacterEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
				sr.serialize(new DataOutputStream(response.getOutputStream()),getPatientData(cohortId,xformsService));
			}
			else if(request.getParameter(XformConstants.REQUEST_PARAM_SET_COHORT) != null){
				Context.getAdministrationService().setGlobalProperty("xforms.patientDownloadCohort", cohortId);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.setPatientDownloadCohortSuccess");
				return new ModelAndView(new RedirectView(getSuccessView()));
			}
		}
		
		if(request.getParameter(XformConstants.REQUEST_PARAM_UPLOAD_PATIENT_XFORM) != null){//TODO This needs to be refactored with the XformUploadController
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			MultipartFile xformFile = multipartRequest.getFile(XformConstants.REQUEST_PARAM_PATIENT_XFORM_FILE);
			if (xformFile != null && !xformFile.isEmpty()) {
				String xml = IOUtils.toString(xformFile.getInputStream());
				Xform xform = new Xform();
				xform.setFormId(XformConstants.PATIENT_XFORM_FORM_ID);
				xform.setXformData(xml);
				xformsService.saveXform(xform);
				
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "xforms.patientXformUploadSuccess");
			}
			return new ModelAndView(new RedirectView(getSuccessView()));
		}
		else if(request.getParameter(XformConstants.REQUEST_PARAM_DOWNLOAD_PATIENT_XFORM) != null){
			String filename = XformConstants.PATIENT_XFORM_FORM_ID+XformConstants.XML_FILE_EXTENSION;
			response.setHeader(XformConstants.HTTP_HEADER_CONTENT_DISPOSITION, XformConstants.HTTP_HEADER_CONTENT_DISPOSITION_VALUE + filename);
			response.getOutputStream().print(XformBuilder.getNewPatientXform("testing"));
		}
		
		return null;
    }
	
	private PatientData getPatientData(String cohortId,XformsService xformsService){
		PatientData patientData  = new PatientData();
		
		Cohort cohort = Context.getCohortService().getCohort(Integer.parseInt(cohortId));
		Set<Integer> patientIds = cohort.getMemberIds();
		patientData.setPatients(getPantients(patientIds));
		
		List<PatientTableField> fields = PatientTableFieldBuilder.getPatientTableFields(xformsService);
		if(fields != null && fields.size() > 0){
			patientData.setFields(fields);
			patientData.setFieldValues(PatientTableFieldBuilder.getPatientTableFieldValues(new ArrayList(patientIds), fields, xformsService));
		}
		
		return patientData;
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
