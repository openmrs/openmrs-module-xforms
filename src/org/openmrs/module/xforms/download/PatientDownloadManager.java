package org.openmrs.module.xforms.download;


import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.PatientData;
import org.openmrs.module.xforms.PatientTableField;
import org.openmrs.module.xforms.PatientTableFieldBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.module.xforms.SerializableData;


/**
 * Manages patient downloads.
 * 
 * @author Daniel
 *
 */
public class PatientDownloadManager {

	public static void downloadPatients(String cohortId, OutputStream os) throws Exception{
		
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);

		String className = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_SERIALIZER);
		if(className == null || className.length() == 0)
			className = XformConstants.DEFAULT_PATIENT_SERIALIZER;
		SerializableData sr = (SerializableData)OpenmrsClassLoader.getInstance().loadClass(className).newInstance();
		
		if(cohortId == null)
			cohortId = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_DOWNLOAD_COHORT);
		
		sr.serialize(new DataOutputStream(os),getPatientData(cohortId,xformsService));
	}
	
	private static PatientData getPatientData(String cohortId,XformsService xformsService){
		Context.openSession(); //This prevents the bluetooth server from failing with the form field lazy load exception.
		PatientData patientData  = new PatientData();
		Cohort cohort = Context.getCohortService().getCohort(Integer.parseInt(cohortId));
		Set<Integer> patientIds = cohort.getMemberIds();
		if(patientIds != null && patientIds.size() > 0){
			patientData.setPatients(getPantients(patientIds));
			List<PatientTableField> fields = PatientTableFieldBuilder.getPatientTableFields(xformsService);
			if(fields != null && fields.size() > 0){
				patientData.setFields(fields);
				patientData.setFieldValues(PatientTableFieldBuilder.getPatientTableFieldValues(new ArrayList(patientIds), fields, xformsService));
			}
		}
		
		return patientData;
	}
	
	private static List<Patient> getPantients(Collection<Integer> patientIds){
		List<Patient> patients = new ArrayList<Patient>();
		
		PatientService patientService = Context.getPatientService();
		for(Integer patientId : patientIds)
			patients.add(patientService.getPatient(patientId));
		
		return patients;
	}
}
