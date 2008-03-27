package org.openmrs.module.xforms.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformUser;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.db.XformsDAO;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements XForms services.
 * 
 * @author Daniel
 *
 */
public class XformsServiceImpl implements XformsService{
	
	private XformsDAO dao;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public XformsServiceImpl() {	}
	
	private XformsDAO getXformsDAO() {
		return dao;
	}
	
	public void setXformsDAO(XformsDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXform(org.openmrs.Form)
	 */
	public void deleteXform(Form form) {
	    deleteXform(form.getFormId());
    }
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXform(java.lang.Integer)
	 */
	public void deleteXform(Integer formId){
		getXformsDAO().deleteXform(formId);
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXslt(java.lang.Integer)
	 */
	public void deleteXslt(Integer formId){
		getXformsDAO().deleteXslt(formId);
	}

	/**
     * @see org.openmrs.module.xforms.XformsService#saveXform(org.openmrs.module.xforms.Xform)
     */
    public void saveXform(Xform xform) {
    	xform.setCreator(Context.getAuthenticatedUser());
    	xform.setDateCreated(new Date());
		getXformsDAO().saveXform(xform);
    }

	/**
     * @see org.openmrs.module.xforms.XformsService#getXform(org.openmrs.Form)
     */
    public Xform getXform(Form form) {
	    return getXform(form.getFormId());
    }
    
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXforms()
	 */
    public List<Xform> getXforms(){
    	return getXformsDAO().getXforms();
    }
    
    /**
     * @see org.openmrs.module.xforms.XformsService#getXform(java.lang.Integer)
     */
    public Xform getXform(Integer formId) {
	    return getXformsDAO().getXform(formId);
    }
    
	/**
	 * @see org.openmrs.module.xforms.XformsService#getPatientValue(java.lang.Integer,java.lang.String,java.lang.String,java.lang.String)
	 */
    public Object getPatientValue(Integer patientId, String tableName, String columnName, String filterValue){
    	return getXformsDAO().getPatientValue(patientId, tableName, columnName,filterValue);
    }
    
	/**
	 * @see org.openmrs.module.xforms.XformsService#getUsers()
	 */
    public List<XformUser> getUsers(){
    	return getXformsDAO().getUsers();
    }
    
    /**
	 * @see org.openmrs.module.xforms.XformsService#getXformFormIds()
	 */
    public List<Integer> getXformFormIds(){
    	return getXformsDAO().getXformFormIds();
    }
    
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXform(java.lang.Integer)
	 */
	public boolean hasXform(Integer formId){
	   	return getXformsDAO().hasXform(formId);
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXslt(java.lang.Integer)
	 */
	public boolean hasXslt(Integer formId){
		return getXformsDAO().hasXslt(formId);
	}
	
	 /**
     * @see org.openmrs.module.xforms.XformsService#getXslt(java.lang.Integer)
     */
	@Transactional(readOnly=true)
	public String getXslt(Integer formId){
		return getXformsDAO().getXslt(formId);
	}
	
	 /**
     * @see org.openmrs.module.xforms.XformsService#saveXslt(java.lang.Integer,java.lang.String)
     */
	public void saveXslt(Integer formId, String xslt){
		getXformsDAO().saveXslt(formId, xslt);
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getFieldDefaultValue(java.lang.Integer,java.lang.String)
	 */
	public Object getFieldDefaultValue(Integer formId, String fieldName){
		return getXformsDAO().getFieldDefaultValue(formId, fieldName);
	}
	
    /**
     * @see org.openmrs.module.xforms.XformsService#getXform(java.lang.Integer,java.lang.boolean)
     */
    public Xform getXform(Integer formId, boolean createNewIfNonExistant) {
	    Xform xform = getXformsDAO().getXform(formId);
	    
	    if(xform == null && createNewIfNonExistant)
	    	xform = getNewXform(formId);
	    
	    return xform;
    }
    
    /**
     * @see org.openmrs.module.xforms.XformsService#getNewXform(java.lang.Integer)
     */
    public Xform getNewXform(Integer formId) {
    	FormService formService = (FormService)Context.getService(FormService.class);
    	Form form = formService.getForm(formId);
		String schemaXml = XformsUtil.getSchema(form);
		String templateXml = FormEntryWrapper.getFormTemplate(form); //new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		
		//TODO This localhost and port(8080) should not be hardcoded.
		String actionUrl = "http://" + "localhost" + ":8080/openmrs" + XformConstants.XFORM_DATA_UPLOAD_RELATIVE_URL;
		return new Xform(formId,XformBuilder.getXform4mStrings(schemaXml, templateXml,actionUrl));
   }
}
