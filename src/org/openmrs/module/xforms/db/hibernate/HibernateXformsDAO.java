package org.openmrs.module.xforms.db.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformUser;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.db.*;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;


/**
 * Provides the hibernate data access services for the Xforms module.
 * 
 * @author Daniel
 *
 */
public class HibernateXformsDAO implements XformsDAO{
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factories
	 */
	private SessionFactory sessionFactory;
	
	public HibernateXformsDAO(){
		
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
	public Xform getXform(Integer formId){
		Query query = sessionFactory.getCurrentSession().createQuery("from Xform where formId = :formId");
		query.setParameter("formId", formId);
    	 
    	return (Xform)query.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXforms()
	 */
	@SuppressWarnings("unchecked")
	public List<Xform> getXforms(){
		return sessionFactory.getCurrentSession().createQuery("from Xform").list();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#saveXform(org.openmrs.module.xforms.Xform)
	 */
	public void saveXform(Xform xform){
		//sessionFactory.getCurrentSession().saveOrUpdate(xform);
		deleteXform(xform.getFormId());
		sessionFactory.getCurrentSession().save(xform);
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXform(java.lang.Integer)
	 */
	public void deleteXform(Integer formId){
		Query query = sessionFactory.getCurrentSession().createQuery("delete from Xform where formId = :formId");
		query.setParameter("formId", formId);
    	 
    	query.executeUpdate();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getPatientValue(java.lang.Integer,java.lang.String,java.lang.String,java.lang.String)
	 */
	public Object getPatientValue(Integer patientId, String tableName, String columnName, String filterValue){
		try{
			String sql = "select " + columnName + " from " + tableName + " where " + (tableName.indexOf("person") != -1 ? "person_id" : "patient_id") + "=" + patientId;
			if(filterValue != null && tableName.equalsIgnoreCase("PATIENT_IDENTIFIER") && columnName.equalsIgnoreCase("IDENTIFIER"))
				sql += " and identifier_type = " + filterValue;
			return sessionFactory.getCurrentSession().createSQLQuery(sql).uniqueResult();
		}catch(Exception e){
			log.error("Could not get value for field:["+columnName+"] table:["+tableName+"]");
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getUsers()
	 */
	public List<XformUser> getUsers(){
		List<XformUser> users = new ArrayList<XformUser>();
		
		String sql = "select user_id,username,password,salt from users";
	
		try{
			PreparedStatement st = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next())
				users.add(new XformUser(res.getInt("user_id"),res.getString("username"),
						res.getString("password"),res.getString("salt")));
			
			return users;
		}
		catch(SQLException e){
			log.error(e);
		}
		
		return null;
	}
	
	public List<Integer> getXformFormIds(){
		List<Integer> formIds = new ArrayList<Integer>();
		
		String sql = "select form_id from xform where form_id<>"+XformConstants.PATIENT_XFORM_FORM_ID;
	
		try{
			PreparedStatement st = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			String varname;
			while(res.next())
				formIds.add(res.getInt("form_id"));
			
			return formIds;
		}
		catch(SQLException e){
			log.error(e);
		}
		
		return null;

	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXform(java.lang.Integer)
	 */
	public boolean hasXform(Integer formId){
		String sql = "select 1 from xform where form_id="+formId;
		return sessionFactory.getCurrentSession().createSQLQuery(sql).uniqueResult() != null;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXslt(java.lang.Integer)
	 */
	public boolean hasXslt(Integer formId){
		String sql = "select 1 from xform where xslt is not null and form_id="+formId;
		return sessionFactory.getCurrentSession().createSQLQuery(sql).uniqueResult() != null;		
	}
	
	 /**
     * @see org.openmrs.module.xforms.XformsService#getXslt(java.lang.Integer)
     */
	public String getXslt(Integer formId){
		String sql = "select xslt from xform where form_id="+formId;
		try{
			PreparedStatement st = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			ResultSet res = st.executeQuery();
			if(res.next())
				return res.getString("xslt");
		}
		catch(SQLException e){
			log.error(e);
		}
		
		return null;
	}
	
	 /**
     * @see org.openmrs.module.xforms.XformsService#saveXslt(java.lang.Integer,java.lang.String)
     */
	public void saveXslt(Integer formId, String xslt){
		Query query = sessionFactory.getCurrentSession().createSQLQuery("update xform set xslt = :xslt where form_id = :formId");
		query.setParameter("xslt", xslt);
		query.setParameter("formId", formId);
    	 
    	query.executeUpdate();
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getFieldDefaultValue(java.lang.Integer,java.lang.String)
	 */
	public Object getFieldDefaultValue(Integer formId, String fieldName){
		String sql = "select default_value from field f inner join form_field ff on f.field_id=ff.field_id where form_id="+formId+" and name='" + fieldName + "'";
		
		try{
			PreparedStatement st = sessionFactory.getCurrentSession().connection().prepareStatement(sql);
			ResultSet res = st.executeQuery();
			if(res.next())
				return res.getString("default_value");
		}
		catch(SQLException e){
			log.error(e);
		}
		
		return null;
	}
}
