package org.openmrs.module.xforms.download;


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.PatientData;
import org.openmrs.module.xforms.PatientTableField;
import org.openmrs.module.xforms.PatientTableFieldBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;


/**
 * Manages patient downloads.
 * 
 * @author Daniel
 *
 */
public class PatientDownloadManager {

	private static Log log = LogFactory.getLog(PatientDownloadManager.class);

	
	public static void downloadPatients(String cohortId, OutputStream os) throws Exception{
		
        if(cohortId == null)
            cohortId = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_DOWNLOAD_COHORT);

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);

        XformsUtil.invokeSerializationMethod(os, XformConstants.GLOBAL_PROP_KEY_PATIENT_SERIALIZER, XformConstants.DEFAULT_PATIENT_SERIALIZER, getPatientData(cohortId,xformsService));
 	}
	
	private static PatientData getPatientData(String sCohortId,XformsService xformsService){
		Context.openSession(); //This prevents the bluetooth server from failing with the form field lazy load exception.
		PatientData patientData  = new PatientData();
		
		Integer cohortId = getCohortId(sCohortId);
		if(cohortId != null){
			Cohort cohort = Context.getCohortService().getCohort(cohortId);
			Set<Integer> patientIds = cohort.getMemberIds();
			if(patientIds != null && patientIds.size() > 0){
				patientData.setPatients(getPantients(patientIds));
				List<PatientTableField> fields = PatientTableFieldBuilder.getPatientTableFields(xformsService);
				if(fields != null && fields.size() > 0){
					patientData.setFields(fields);
					patientData.setFieldValues(PatientTableFieldBuilder.getPatientTableFieldValues(new ArrayList(patientIds), fields, xformsService));
				}
			}
		}
		
		return patientData;
	}
	
	private static Integer getCohortId(String cohortId){
		if(cohortId == null || cohortId.trim().length() == 0)
			return null;
		try{
			return Integer.parseInt(cohortId);
		}catch(Exception e){
            log.error(e.getMessage(),e);
		}
		
		return null;
	}
	
	private static List<Patient> getPantients(Collection<Integer> patientIds){
		List<Patient> patients = new ArrayList<Patient>();
		
		PatientService patientService = Context.getPatientService();
		for(Integer patientId : patientIds)
			patients.add(patientService.getPatient(patientId));
		
		return patients;
	}
    
	public static void downloadCohorts(OutputStream os) throws Exception{
        XformsUtil.invokeSerializationMethod(os, XformConstants.GLOBAL_PROP_KEY_COHORT_SERIALIZER, XformConstants.DEFAULT_COHORT_SERIALIZER, Context.getCohortService().getCohorts());
     }
}
