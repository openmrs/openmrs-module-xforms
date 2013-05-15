package org.openmrs.module.xforms.db;

import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.xforms.MedicalHistoryField;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.formentry.XformsFormEntryError;
import org.openmrs.module.xforms.model.PatientMedicalHistory;
import org.openmrs.module.xforms.model.PersonRepeatAttribute;
import org.openmrs.module.xforms.model.XformUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides data access services to the Xforms module.
 * 
 * @author Daniel
 *
 */
public interface XformsDAO {
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXform(java.lang.Integer)
	 */
	public Xform getXform(Integer formId);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXforms()
	 */
	public List<Xform> getXforms();
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#saveXform(org.openmrs.module.xforms.Xform)
	 */
	public void saveXform(Xform xform);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXform(java.lang.Integer)
	 */
	public void deleteXform(Integer formId);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXslt(java.lang.Integer)
	 */
	public void deleteXslt(Integer formId);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getPatientValue(java.lang.Integer,java.lang.String,java.lang.String,jave.lang.String)
	 */
	public Object getPatientValue(Integer patientId, String tableName, String columnName, String filterValue);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getUsers()
	 */
	public List<XformUser> getUsers();
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXformFormIds()
	 */
	public List<Integer> getXformFormIds();
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXform(java.lang.Integer)
	 */
	public boolean hasXform(Integer formId);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXslt(java.lang.Integer)
	 */
	public boolean hasXslt(Integer formId);
	
	 /**
     * @see org.openmrs.module.xforms.XformsService#getXslt(java.lang.Integer)
     */
	public String getXslt(Integer formId);
	
	 /**
     * @see org.openmrs.module.xforms.XformsService#saveXslt(java.lang.Integer,java.lang.String)
     */
	public void saveXslt(Integer formId, String xslt);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getFieldDefaultValue(java.lang.Integer,java.lang.String)
	 */
	public String getFieldDefaultValue(Integer formId, String fieldName);
	
	
	public List<PersonRepeatAttribute> getPersonRepeatAttributes(Integer personId, Integer personAttributeId);
	public void savePersonRepeatAttribute(PersonRepeatAttribute personRepeatAttribute);
	public void deletePersonRepeatAttribute(Integer personRepeatAttributeId);
	
	public List<Object[]> getList(String sql, String displayField, String valueField);
	
	public PatientMedicalHistory getPatientMedicalHistory(Integer patientId);
	
	public List<MedicalHistoryField> getMedicalHistoryFields();
	public void saveMedicalHistoryField(MedicalHistoryField field);
	public void deleteMedicalHistoryField(MedicalHistoryField field);
	public void deleteMedicalHistoryField(Integer fieldId);
	
	public void createFormEntryError(XformsFormEntryError formEntryError);

	public List<GlobalProperty> getXFormsGlobalProperties();
	
	public List<Object[]> getXformsList();
	
	public String getLocationName(Integer locationId);
	public String getPersonName(Integer personId);
	public String getConceptName(Integer conceptId, String localeKey);
}
