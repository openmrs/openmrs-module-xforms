package org.openmrs.module.xforms.db.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformUser;
import org.openmrs.module.xforms.db.*;
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
	 * @see org.openmrs.module.xforms.XformsService#getPatientValue(java.lang.Integer,java.lang.String,java.lang.String)
	 */
	public Object getPatientValue(Integer patientId, String tableName, String columnName){
		String sql = "select " + columnName + " from " + tableName + " where " + (tableName.indexOf("person") != -1 ? "person_id" : "patient_id") + "=" + patientId;
		return sessionFactory.getCurrentSession().createSQLQuery(sql).uniqueResult();
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
			
			String varname;
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
}
