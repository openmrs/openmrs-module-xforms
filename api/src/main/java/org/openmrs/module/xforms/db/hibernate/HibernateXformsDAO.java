/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms.db.hibernate;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.GlobalProperty;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.xforms.MedicalHistoryField;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.db.XformsDAO;
import org.openmrs.module.xforms.formentry.XformsFormEntryError;
import org.openmrs.module.xforms.model.MedicalHistoryFieldData;
import org.openmrs.module.xforms.model.MedicalHistoryValue;
import org.openmrs.module.xforms.model.PatientMedicalHistory;
import org.openmrs.module.xforms.model.PersonRepeatAttribute;
import org.openmrs.module.xforms.model.XformUser;
import org.openmrs.module.xforms.util.XformsUtil;
import org.springframework.util.StringUtils;

/**
 * Provides the hibernate data access services for the Xforms module.
 * 
 * @author Daniel
 * 
 */
public class HibernateXformsDAO implements XformsDAO {
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factories
	 */
	private SessionFactory sessionFactory;
	
	public HibernateXformsDAO() {
		
	}
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXform(java.lang.Integer)
	 */
	public Xform getXform(Integer formId) {		
		Query query = getCurrentSession().createQuery(
		"from Xform where formId = :formId");
		query.setParameter("formId", formId);
		
		return (Xform) query.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXforms()
	 */
	@SuppressWarnings("unchecked")
	public List<Xform> getXforms() {
		return getCurrentSession().createQuery("from Xform").list();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#saveXform(org.openmrs.module.xforms.Xform)
	 */
	public void saveXform(Xform xform) {
		// getCurrentSession().saveOrUpdate(xform);
		//deleteXform(xform.getFormId());
		//Context.evictFromSession(xform);
		getCurrentSession().save(xform);
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXform(java.lang.Integer)
	 */
	public void deleteXform(Integer formId) {
		Query query = getCurrentSession().createQuery(
		"delete from Xform where formId = :formId");
		query.setParameter("formId", formId);
		
		query.executeUpdate();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXslt(java.lang.Integer)
	 */
	public void deleteXslt(Integer formId) {
		Query query = getCurrentSession().createQuery(
		"update Xform set xslt = null where formId = :formId");
		query.setParameter("formId", formId);
		
		query.executeUpdate();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getPatientValue(java.lang.Integer,java.lang.String,java.lang.String,java.lang.String)
	 */
	public Object getPatientValue(Integer patientId, String tableName,
	                              String columnName, String filterValue) {
		
		if(tableName.equals("patient")){
			if(columnName.equals("birthdate")||columnName.equals("birthdate_estimated")||columnName.equals("gender"))
				tableName = "person";
		}
		else if(tableName.equals("patient_address")){
			if(columnName.equals("address1")||columnName.equals("address2"))
				tableName = "person_address";
		}
		else if(tableName.equals("patient_name")){
			if(columnName.equals("family_name")||columnName.equals("given_name")||columnName.equals("middle_name"))
				tableName = "person_name";
		}
		
		String sql = "";
		try {
			sql = "select "
				+ columnName
				+ " from "
				+ tableName
				+ " where "
				+ (tableName.indexOf("person") != -1 ? "person_id"
						: "patient_id") + "=" + patientId;
			if (filterValue != null
					&& tableName.equalsIgnoreCase("PATIENT_IDENTIFIER")
					&& columnName.equalsIgnoreCase("IDENTIFIER"))
				sql += " and identifier_type = " + filterValue;
			else if(tableName.equalsIgnoreCase("PATIENT_IDENTIFIER"))
				sql += " and preferred=1";
			
			return getCurrentSession().createSQLQuery(sql)
			.uniqueResult();
		} catch (Exception e) {
			log.error("Could not get value for field:[" + columnName
				+ "] table:[" + tableName + "] SQL=" + sql
				+ " ErrorDetails=" + e.getMessage(), /*e*/null);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getUsers()
	 */
	public List<XformUser> getUsers() {
		List<XformUser> users = new ArrayList<XformUser>();
		
		String sql = "select user_id,system_id,username,password,salt from users " +
		"where password is not null and salt is not null and " +
		"not (username is null and system_id is null)";
		
		List<Object[]> results = (List<Object[]>) getCurrentSession()
				.createSQLQuery(sql).list();
		
		for (Object[] row : results) {
			users.add(new XformUser((Integer) row[0], (String) row[1],
					(String) row[2], (String) row[3], (String) row[4]));
		}

		return users;
	}
	
	public List<Integer> getXformFormIds() {
		List<Integer> formIds = new ArrayList<Integer>();
		
		String sql = "select form_id from xforms_xform where form_id in (select form_id from form) and form_id<>"
			+ XformConstants.PATIENT_XFORM_FORM_ID;
		
		List<Object[]> results = (List<Object[]>) getCurrentSession()
				.createSQLQuery(sql).list();

		for (Object[] row : results) {
			formIds.add((Integer) row[0]);
		}

		return formIds;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXform(java.lang.Integer)
	 */
	public boolean hasXform(Integer formId) {
		String sql = "select 1 from xforms_xform where form_id=" + formId;
		return getCurrentSession().createSQLQuery(sql)
		.uniqueResult() != null;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXslt(java.lang.Integer)
	 */
	public boolean hasXslt(Integer formId) {
		String sql = "select 1 from xforms_xform where xslt is not null and form_id="
			+ formId;
		return getCurrentSession().createSQLQuery(sql)
		.uniqueResult() != null;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXslt(java.lang.Integer)
	 */
	public String getXslt(Integer formId) {
		String sql = "select xslt from xforms_xform where form_id=" + formId;
		Object xslt = getCurrentSession().createSQLQuery(sql).uniqueResult();
		if (xslt != null) {
			return (String) xslt;
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#saveXslt(java.lang.Integer,java.lang.String)
	 */
	public void saveXslt(Integer formId, String xslt) {
		Query query = getCurrentSession().createSQLQuery(
		"update xforms_xform set xslt = :xslt where form_id = :formId");
		query.setParameter("xslt", xslt);
		query.setParameter("formId", formId);
		
		query.executeUpdate();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getFieldDefaultValue(java.lang.Integer,java.lang.String)
	 */
	/*public Object getFieldDefaultValue(Integer formId, String fieldName) {
		String sql = "select default_value from field f inner join form_field ff on f.field_id=ff.field_id where form_id="
			+ formId + " and name='" + fieldName + "'";

		try {
			PreparedStatement st = getCurrentSession()
			.connection().prepareStatement(sql);
			ResultSet res = st.executeQuery();
			if (res.next())
				return res.getString("default_value");
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
		}

		return null;
	}*/
	
	public List<PersonRepeatAttribute> getPersonRepeatAttributes(Integer personId, Integer personAttributeId){
		Query query = getCurrentSession().createQuery(
			"from PersonRepeatAttributes where personId=:personId "+
		"and attributeTypeId=:attributeTypeId");
		
		query.setParameter("personId", personId);
		query.setParameter("attributeTypeId", personAttributeId);
		
		return query.list();
	}
	
	public void savePersonRepeatAttribute(PersonRepeatAttribute personRepeatAttribute){
		getCurrentSession().save(personRepeatAttribute);
	}
	
	public void deletePersonRepeatAttribute(Integer personRepeatAttributeId){
		getCurrentSession().delete(personRepeatAttributeId);
	}
	
	public List<Object[]> getList(String sql, String displayField, String valueField){
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		
		if(displayField != null && displayField.trim().length() > 0){
			if(XformsUtil.isOnePointNineAndAbove())
				query.addScalar(displayField/*, Hibernate.STRING*/);
			else
				query.addScalar(displayField, StandardBasicTypes.STRING);
		}
		
		if(valueField != null && valueField.trim().length() > 0){
			if(XformsUtil.isOnePointNineAndAbove())
				query.addScalar(valueField/*, Hibernate.STRING*/);
			else
				query.addScalar(valueField, StandardBasicTypes.STRING);
		}
		
		return query.list();
	}
	
	public List<MedicalHistoryField> getMedicalHistoryFields() {
		return getCurrentSession().createQuery("from MedicalHistoryField").list();
	}
	
	public void saveMedicalHistoryField(MedicalHistoryField field){
		if(field.isNew())
			getCurrentSession().save(field);
		else
			getCurrentSession().update(field);
	}
	
	public void deleteMedicalHistoryField(MedicalHistoryField field){
		getCurrentSession().delete(field);
	}
	
	public void deleteMedicalHistoryField(Integer fieldId){
		Query query = getCurrentSession().createQuery("delete from MedicalHistoryField where fieldId = ?");
		query.setParameter(0, fieldId);
		query.executeUpdate();
	}
	
	/*public PatientMedicalHistory getPatientMedicalHistory(Integer patientId){
		String sql = "select * from (select mhf.tabIndex,mhf.name, " +
			"cast(case when value_group_id is not null then value_group_id " +
			"when value_boolean is not null then value_boolean " +
			"when value_drug is not null then value_drug " +
			"when value_datetime is not null then value_datetime " +
			"when value_numeric is not null then value_numeric " +
			"when value_modifier is not null then value_modifier " +
			"when value_text is not null then value_text " +
			"end as char) as value,e.encounter_datetime " +
			"from encounter e " +
			"inner join obs o on o.encounter_id = e.encounter_id " +
			"inner join field f on f.concept_id=o.concept_id " +
			"inner join xforms_medical_history_field mhf on mhf.field_id=f.field_id " +
			"and o.person_id = e.patient_id " +
			"where e.patient_id = " + patientId + " " +
			"and value_coded is null " +
			"UNION " +
			"select mhf.tabIndex,mhf.name,cn.name,e.encounter_datetime " +
			"from encounter e " +
			"inner join obs o on o.encounter_id = e.encounter_id " +
			"inner join concept_name cn on cn.concept_id=o.value_coded " +
			"inner join field f on f.concept_id=o.concept_id " +
			"inner join xforms_medical_history_field mhf on mhf.field_id=f.field_id " +
			"and o.person_id = e.patient_id " +
			"where e.patient_id = " + patientId + " " +
			"and value_coded is not null) as t where value is not null " +
			"order by tabIndex,name,encounter_datetime";

		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		query.addScalar("name", Hibernate.STRING);
		query.addScalar("value", Hibernate.STRING);
		query.addScalar("encounter_datetime", Hibernate.DATE);
		
		List<Object[]> list = query.list();
		if(list == null || list.size() == 0)
			return null;
		
		PatientMedicalHistory history = new PatientMedicalHistory();
		MedicalHistoryFieldData field = null;
		String prevName = null;
		for(Object[] item : list){
			String name = (String)item[0];
			if(!name.equals(prevName)){
				field = new MedicalHistoryFieldData();
				field.setFieldName(name);
				history.addHistory(field);
				prevName = name;
			}
			field.addValue(new MedicalHistoryValue((String)item[1],(Date)item[2]));
		}
		
		history.setPatientId(patientId);
		
		return history;
	}*/
	
	
	public PatientMedicalHistory getPatientMedicalHistory(Integer patientId){
		
		String sql = "select * from (select mhf.tab_index,mhf.name, " +
		"value_group_id, " +
		"value_boolean, " +
		"value_drug, " +
		"value_datetime, " +
		"value_numeric, " +
		"value_text, " +
		"e.encounter_datetime " +
		"from encounter e " +
		"inner join obs o on o.encounter_id = e.encounter_id " +
		"inner join xforms_medical_history_field mhf on mhf.field_id=o.concept_id " +
		"and o.person_id = e.patient_id " +
		"where e.patient_id = " + patientId + " " +
		        "and value_coded is null and o.voided = false " +
		"UNION " +
		        "select mhf.tab_index, mhf.name, null, null, null, null, null, cn.name, e.encounter_datetime " +
		"from encounter e " +
		"inner join obs o on o.encounter_id = e.encounter_id " +
		"inner join concept_name cn on cn.concept_id=o.value_coded " +
		"inner join xforms_medical_history_field mhf on mhf.field_id=o.concept_id " +
		"and o.person_id = e.patient_id " +
		"where e.patient_id = " + patientId + " " +
		        "and value_coded is not null and o.voided = false ) as t " +
		        "order by tab_index,name,encounter_datetime";
		
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		
		if(XformsUtil.isOnePointNineAndAbove())
			query.addScalar("name"/*, Hibernate.STRING*/);
		else
			query.addScalar("name", StandardBasicTypes.STRING);
		
		//query.addScalar("value", Hibernate.STRING);
		
		if(XformsUtil.isOnePointNineAndAbove()){
			query.addScalar("value_group_id"/*, Hibernate.INTEGER*/);
			query.addScalar("value_boolean"/*, Hibernate.INTEGER*/);
			query.addScalar("value_drug"/*, Hibernate.INTEGER*/);
			query.addScalar("value_datetime"/*, Hibernate.DATE*/);
			query.addScalar("value_numeric"/*, Hibernate.FLOAT*/);
			//query.addScalar("value_modifier"/*, Hibernate.OBJECT*/);
			query.addScalar("value_text"/*, Hibernate.STRING*/);
			
			query.addScalar("encounter_datetime"/*, Hibernate.DATE*/);
		}
		else{
			query.addScalar("value_group_id", StandardBasicTypes.INTEGER);
			query.addScalar("value_boolean", StandardBasicTypes.INTEGER);
			query.addScalar("value_drug", StandardBasicTypes.INTEGER);
			query.addScalar("value_datetime", StandardBasicTypes.DATE);
			query.addScalar("value_numeric", StandardBasicTypes.FLOAT);
			//query.addScalar("value_modifier", Hibernate.OBJECT);
			query.addScalar("value_text", StandardBasicTypes.STRING);
			
			query.addScalar("encounter_datetime", StandardBasicTypes.DATE);
		}
		
		List<Object[]> list = query.list();
		if(list == null || list.size() == 0)
			return null;
		
		PatientMedicalHistory history = new PatientMedicalHistory();
		MedicalHistoryFieldData field = null;
		String prevName = null;
		for(Object[] item : list){
			String name = (String)item[0];
			if(!name.equals(prevName)){
				field = new MedicalHistoryFieldData();
				field.setFieldName(name);
				history.addHistory(field);
				prevName = name;
			}
			
			MedicalHistoryValue mhv = new MedicalHistoryValue();
			
			if(item[1] != null){
				mhv.setType(MedicalHistoryValue.TYPE_INT);
				mhv.setValue(item[1]);
			}
			else if(item[2] != null){
				mhv.setType(MedicalHistoryValue.TYPE_INT);
				mhv.setValue(item[2]);
			}
			else if(item[3] != null){
				mhv.setType(MedicalHistoryValue.TYPE_INT);
				mhv.setValue(item[3]);
			}
			else if(item[4] != null){
				mhv.setType(MedicalHistoryValue.TYPE_DATE);
				mhv.setValue(item[4]);
			}
			else if(item[5] != null){
				mhv.setType(MedicalHistoryValue.TYPE_FLOAT);
				mhv.setValue(item[5]);
			}
			else if(item[6] != null){
				mhv.setType(MedicalHistoryValue.TYPE_STRING);
				mhv.setValue(item[6]);
			}
			else
				continue;
			
			mhv.setValueDate((Date)item[7]);
			
			field.addValue(mhv /*new MedicalHistoryValue((String)item[1],(Date)item[2])*/);
		}
		
		history.setPatientId(patientId);
		
		return history;
	}
	
	
	public String getFieldDefaultValue(Integer formId, String fieldName){
		String sql = "select distinct default_value from form_field ff inner join field f " +
		"where ff.field_id=f.field_id " +
		"and ff.form_id=" + formId + " and name = '" + fieldName + "'";
		
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		
		if(XformsUtil.isOnePointNineAndAbove())
			query.addScalar("default_value"/*, Hibernate.STRING*/);
		else
			query.addScalar("default_value", StandardBasicTypes.STRING);
		
		return (String)query.uniqueResult();
	}
	
	public void createFormEntryError(XformsFormEntryError formEntryError) throws DAOException {
		getCurrentSession().save(formEntryError);
	}
	
	public List<GlobalProperty> getXFormsGlobalProperties() {
		String sql = "select * from global_property gp where gp.property like 'xforms%'";
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		
		return query.list();
	}
	
	public List<Object[]> getXformsList(){
		String sql = "select f.form_id, f.name from xforms_xform xf inner join form f " +
		"on xf.form_id=f.form_id where f.retired=0 ";
		
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		
		if(XformsUtil.isOnePointNineAndAbove()){
			query.addScalar("form_id"/*, Hibernate.INTEGER*/);
			query.addScalar("name"/*, Hibernate.STRING*/);
		}
		else{
			query.addScalar("form_id", StandardBasicTypes.INTEGER);
			query.addScalar("name", StandardBasicTypes.STRING);
		}
		
		return query.list();
	}
	
	public String getLocationName(Integer locationId){
		String sql = "select name from location where retired = false and location_id=" + locationId;
		return (String)getCurrentSession().createSQLQuery(sql).uniqueResult(); 
	}
	
	public String getPersonName(Integer personId){

		String sql = "select given_name, middle_name, family_name, preferred from person_name where voided = false and person_id="
				+ personId;

		List<Object[]> results = (List<Object[]>) getCurrentSession()
				.createSQLQuery(sql).list();

		String name = null;

		for (Object[] row : results) {

			// If we already have a name, overwrite it only with a preferred
			// one.
			if (name != null) {
				if (((Boolean) row[3]).booleanValue() != true)
					continue;
				else
					name = null;
			}

			String givenName = (String) row[0];
			String middleName = (String) row[1];
			String familyName = (String) row[2];

			if (StringUtils.hasText(givenName))
				name = givenName;

			if (StringUtils.hasText(middleName)) {
				if (name == null)
					name = givenName;
				else
					name += " " + givenName;
			}

			if (StringUtils.hasText(familyName)) {
				if (name == null)
					name = familyName;
				else
					name += " " + familyName;
			}

			if (((Boolean) row[3]).booleanValue() != true)
				break;
		}

		return name;
	}
	
	public String getConceptName(Integer conceptId, String localeKey){
		String sql = "select name from concept_name where concept_id=" + conceptId + " and locale='" + localeKey
		        + "' and voided = false and locale_preferred=1";
		return (String)getCurrentSession().createSQLQuery(sql).uniqueResult();
	}
	
	/**
	 * Gets the current hibernate session while taking care of the hibernate 3 and 4 differences.
	 * 
	 * @return the current hibernate session.
	 */
	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session)method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}
}
