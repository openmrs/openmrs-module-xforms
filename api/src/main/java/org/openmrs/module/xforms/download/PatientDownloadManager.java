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
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.model.PatientData;
import org.openmrs.module.xforms.model.PatientTableField;
import org.openmrs.module.xforms.model.PatientTableFieldBuilder;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.util.OpenmrsConstants;


/**
 * Manages patient downloads.
 * 
 * @author Daniel
 *
 */
@SuppressWarnings("deprecation")
public class PatientDownloadManager {

	private static Log log = LogFactory.getLog(PatientDownloadManager.class);


	public static void downloadPatients(String cohortId, OutputStream os, String serializerKey, boolean isSavedSearch) throws Exception{
		if(cohortId == null)
			cohortId = Context.getAdministrationService().getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_PATIENT_DOWNLOAD_COHORT);

		if(serializerKey == null)
			serializerKey = XformConstants.GLOBAL_PROP_KEY_PATIENT_SERIALIZER;

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		XformsUtil.invokeSerializationMethod("serialize",os,serializerKey , XformConstants.DEFAULT_PATIENT_SERIALIZER, getPatientData(cohortId,xformsService,isSavedSearch));
	}

	public static void downloadPatients(String name, String identifier, OutputStream os,String serializerKey) throws Exception{
		if(serializerKey == null)
			serializerKey = XformConstants.GLOBAL_PROP_KEY_PATIENT_SERIALIZER;

		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		XformsUtil.invokeSerializationMethod("serialize",os, serializerKey, XformConstants.DEFAULT_PATIENT_SERIALIZER, getPatientData(name,identifier,xformsService));
	}

	private static PatientData getPatientData(String sCohortId,XformsService xformsService, boolean isSavedSearch){
		//Context.openSession(); //This prevents the bluetooth server from failing with the form field lazy load exception.
		PatientData patientData  = new PatientData();
		Cohort cohort = null;;
		Integer cohortId = getCohortId(sCohortId);
		if(cohortId != null){
			if (!isSavedSearch)
				cohort = Context.getCohortService().getCohort(cohortId);
			else {
				CohortSearchHistory history= new CohortSearchHistory();
				PatientSearchReportObject patientSearchReportObject = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(cohortId);
				if (patientSearchReportObject != null){
					history.addSearchItem(PatientSearch.createSavedSearchReference(cohortId));
					cohort = history.getPatientSet(0, null);
				}
			}
			
			if(cohort != null){
				Set<Integer> patientIds = cohort.getMemberIds();
				if(patientIds != null && patientIds.size() > 0)
					patientData.setPatients(getPatients(patientIds));
					
					//TODO We need to make this optional because it makes patient download too too slow.
					/*List<PatientTableField> fields = PatientTableFieldBuilder.getPatientTableFields(xformsService);
					if(fields != null && fields.size() > 0){
						patientData.setFields(fields);
						patientData.setFieldValues(PatientTableFieldBuilder.getPatientTableFieldValues(new ArrayList(patientIds), fields, xformsService));
					}*/
			}

			List<Patient> patients = patientData.getPatients();
			if(patients != null && patients.size() > 0){
				for(Patient patient : patients)
					patientData.addMedicalHistory(xformsService.getPatientMedicalHistory(patient.getPatientId()));
			}
		}

		return patientData;
	}
	
	private static PatientData getPatientData(String name, String identifier,XformsService xformsService){
		//Context.openSession(); //This prevents the bluetooth server from failing with the form field lazy load exception.
		PatientData patientData  = new PatientData();

		if(name != null && name.trim().length() == 0)
			name = null;
		if(identifier != null && identifier.trim().length() == 0)
			identifier = null;

		List<Patient> patients = Context.getPatientService().getPatients(name, identifier, null,false);
		patientData.setPatients(patients);
		if(patients != null){
			for(Patient patient : patients){
				List<PatientTableField> fields = PatientTableFieldBuilder.getPatientTableFields(xformsService);
				if(fields != null && fields.size() > 0){
					patientData.setFields(fields);
					patientData.setFieldValues(PatientTableFieldBuilder.getPatientTableFieldValues(getPatientIds(patients), fields, xformsService));
					patientData.addMedicalHistory(xformsService.getPatientMedicalHistory(patient.getPatientId()));
				}
			}
		}

		return patientData;
	}

	private static List<Integer> getPatientIds(List<Patient> patients){
		List<Integer> patientIds = new ArrayList<Integer>();
		for(Patient patient : patients)
			patientIds.add(patient.getPatientId());

		return patientIds;
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

	private static List<Patient> getPatients(Collection<Integer> patientIds){
		List<Patient> patients = new ArrayList<Patient>();

		PatientService patientService = Context.getPatientService();
		for(Integer patientId : patientIds)
			patients.add(patientService.getPatient(patientId));

		return patients;
	}

	public static void downloadCohorts(OutputStream os, String serializerKey) throws Exception{
		if(serializerKey == null)
				serializerKey = XformConstants.GLOBAL_PROP_KEY_COHORT_SERIALIZER;

		XformsUtil.invokeSerializationMethod("serialize",os, serializerKey, XformConstants.DEFAULT_COHORT_SERIALIZER, Context.getCohortService().getCohorts());
	}
	
	public static void downloadSavesSearches(OutputStream os, String serializerKey) throws Exception{
		if(serializerKey == null)
				serializerKey =  XformConstants.GLOBAL_PROP_KEY_SAVED_SEARCH_SERIALIZER;

		XformsUtil.invokeSerializationMethod("serialize",os, serializerKey, XformConstants.DEFAULT_SAVED_SEARCH_SERIALIZER, Context.getReportObjectService().getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH));
	}
}
