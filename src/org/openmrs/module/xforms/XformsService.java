package org.openmrs.module.xforms;

import java.util.List;

import org.openmrs.Form;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.xforms.db.*;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service methods for the Xforms module
 */

@Transactional
public interface XformsService {
	
	/**
	 * Sets the xforms data access object.
	 * 
	 * @param dao - the data access object.
	 */
	public void setXformsDAO(XformsDAO dao);
	
	/**
	 * Saves an Xform in the database. If it already exists, it will be overwritten,
	 * else will create a new one.
	 * 
	 * @param xform
	 */
	public void saveXform(Xform xform);
	
	/**
	 * Deletes an XForm associated with the given form
	 * 
	 * @param form Form object 
	 */
	public void deleteXform(Form form);
	
	/**
	 * Deletes an XForm associated with the given form id
	 * 
	 * @param formId The id of the form
	 */
	public void deleteXform(Integer formId);
	
	/**
	 * Get the XForm for the given form
	 * 
	 * @param form - the form
	 * @return XForm associated with the form
	 */
	@Transactional(readOnly=true)
	public Xform getXform(Form form);
	
	/**
	 * Gets all XForms
	 * 
	 * @return List of XForms
	 */
	@Transactional(readOnly=true)
	public List<Xform> getXforms();
	
	/**
	 * Get the XForm for the given form
	 * 
	 * @param formId id of the form that owns the XForm to retrieve
	 * @return XForm associated with the form
	 */
	@Transactional(readOnly=true)
	public Xform getXform(Integer formId);
	
	/**
	 * Gets the value of a patient table field.
	 * 
	 * @param patientId - the id of the patient.
	 * @param tableName - the name of the database table.
	 * @param columnName - the name of the database column.
	 * @return
	 */
	@Transactional(readOnly=true)
	public Object getPatientValue(Integer patientId, String tableName, String columnName);
	
	/**
	 * Gets a list of users.
	 * 
	 * @return - the user list.
	 */
	@Transactional(readOnly=true)
	public List<XformUser> getUsers();

}
