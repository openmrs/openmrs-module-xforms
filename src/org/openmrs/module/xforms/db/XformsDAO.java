package org.openmrs.module.xforms.db;

import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformUser;

import java.util.*;

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
	 * @see org.openmrs.module.xforms.XformsService#getPatientValue(java.lang.Integer,java.lang.String,java.lang.String)
	 */
	public Object getPatientValue(Integer patientId, String tableName, String columnName);
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getUsers()
	 */
	public List<XformUser> getUsers();
}
