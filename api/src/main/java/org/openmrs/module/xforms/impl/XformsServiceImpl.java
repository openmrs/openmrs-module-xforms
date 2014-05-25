package org.openmrs.module.xforms.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.MedicalHistoryField;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformBuilderEx;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.db.XformsDAO;
import org.openmrs.module.xforms.formentry.XformsFormEntryError;
import org.openmrs.module.xforms.model.PatientMedicalHistory;
import org.openmrs.module.xforms.model.PersonRepeatAttribute;
import org.openmrs.module.xforms.model.XformUser;
import org.openmrs.module.xforms.util.XformsUtil;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Implements XForms services.
 * 
 * @author Daniel
 * 
 */
public class XformsServiceImpl implements XformsService {

	private XformsDAO dao;

	private Log log = LogFactory.getLog(this.getClass());
    
    private Class<?> formResourceClass;

	public XformsServiceImpl() {
	}

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
	public void deleteXform(Integer formId) {
		getXformsDAO().deleteXform(formId);
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#deleteXslt(java.lang.Integer)
	 */
	public void deleteXslt(Integer formId) {
		getXformsDAO().deleteXslt(formId);
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#saveXform(org.openmrs.module.xforms.Xform)
	 */
	public void saveXform(Xform xform) {
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
	public List<Xform> getXforms() {
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
	public Object getPatientValue(Integer patientId, String tableName,
			String columnName, String filterValue) {
		return getXformsDAO().getPatientValue(patientId, tableName, columnName,
				filterValue);
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#getUsers()
	 */
	public List<XformUser> getUsers() {
		return getXformsDAO().getUsers();
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#getXformFormIds()
	 */
	public List<Integer> getXformFormIds() {
		return getXformsDAO().getXformFormIds();
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#hasXform(java.lang.Integer)
	 */
	public boolean hasXform(Integer formId) {
		return getXformsDAO().hasXform(formId);
	}

    /**
	 * @see org.openmrs.module.xforms.XformsService#hasXslt(java.lang.Integer)
	 */
	public boolean hasXslt(Integer formId) {
		if (XformsUtil.isOnePointNineAndAbove())
			return StringUtils.isNotBlank(getXslt(formId));
		
		return getXformsDAO().hasXslt(formId);
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#getXslt(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public String getXslt(Integer formId) {
		if (XformsUtil.isOnePointNineAndAbove()) {
			Form form = Context.getFormService().getForm(formId);
			if (form != null) {
				Method getFormResourceMethod = ClassUtils.getMethodIfAvailable(FormService.class, "getFormResource",
				    new Class<?>[] { Form.class, String.class });
				if (getFormResourceMethod != null) {
					Object formResource = ReflectionUtils.invokeMethod(getFormResourceMethod, Context.getFormService(),
					    new Object[] { form, form.getName() + XformConstants.XFORM_XSLT_FORM_RESOURCE_NAME_SUFFIX });
					if (formResource != null) {
						Method valueMethod = ClassUtils.getMethodIfAvailable(formResource.getClass(), "getValue", null);
						if (valueMethod != null){
							try{
								return (String) ReflectionUtils.invokeMethod(valueMethod, formResource);
							}catch(APIException e){
								if("org.openmrs.customdatatype.NotYetPersistedException".equals(e.getClass().getName()))
									return null;//ignore
								
								throw e;
							}
						}
					}
				}
			}
			
			return null;
		} else {
			return getXformsDAO().getXslt(formId);
		}
	}
	
	/**
	 * @see org.openmrs.module.xforms.XformsService#saveXslt(java.lang.Integer,java.lang.String)
	 */
	public void saveXslt(Integer formId, String xslt) {
		if (XformsUtil.isOnePointNineAndAbove()) {
			Form form = Context.getFormService().getForm(formId);
			if (form != null) {
				if (formResourceClass == null) {
					try {
						formResourceClass = Context.loadClass("org.openmrs.FormResource");
					}
					catch (ClassNotFoundException e) {
						log.error("Failed to load class: org.openmrs.FormResource");
					}
				}
				
				if (formResourceClass != null) {
					Method saveFormResourceMethod = ClassUtils.getMethodIfAvailable(FormService.class, "saveFormResource",
					    new Class<?>[] { formResourceClass });
					if (saveFormResourceMethod != null) {
						Object formResource;
						try {
							formResource = formResourceClass.newInstance();
							BeanUtils.setProperty(formResource, "form", form);
							BeanUtils.setProperty(formResource, "name", form.getName()
							        + XformConstants.XFORM_XSLT_FORM_RESOURCE_NAME_SUFFIX);
							
							Method setValueReferenceMethod = ClassUtils.getMethodIfAvailable(FormService.class, "setValueReferenceInternal",
							    new Class<?>[] { String.class });
							if(setValueReferenceMethod != null){
								ReflectionUtils.invokeMethod(setValueReferenceMethod, formResource, new Object[] { xslt });
							}
							
							ReflectionUtils.invokeMethod(saveFormResourceMethod, Context.getFormService(),
							    new Object[] { formResource });
						}
						catch (InstantiationException e) {
							log.error("Error:", e);
						}
						catch (IllegalAccessException e) {
							log.error("Error:", e);
						}
						catch (InvocationTargetException e) {
							log.error("Error:", e);
						}
					}
				}
			}
		} else {
			getXformsDAO().saveXslt(formId, xslt);
		}
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#getFieldDefaultValue(java.lang.Integer,java.lang.String)
	 */
	public String getFieldDefaultValue(Integer formId, String fieldName) {
		return getXformsDAO().getFieldDefaultValue(formId, fieldName);
	}

	/**
	 * @see 
	 *      org.openmrs.module.xforms.XformsService#getXform(java.lang.Integer,java
	 *      .lang.boolean)
	 */
	public Xform getXform(Integer formId, boolean createNewIfNonExistant)
			throws Exception {
		Xform xform = getXformsDAO().getXform(formId);

		if (xform == null && createNewIfNonExistant)
			xform = getNewXform(formId);

		return xform;
	}

	/**
	 * @see org.openmrs.module.xforms.XformsService#getNewXform(java.lang.Integer)
	 */
	public Xform getNewXform(Integer formId) throws Exception {
		FormService formService = (FormService) Context
				.getService(FormService.class);
		Form form = formService.getForm(formId);
		// String schemaXml = XformsUtil.getSchema(form);
		// String templateXml = FormEntryWrapper.getFormTemplate(form);
		return new Xform(formId, XformBuilderEx.buildXform(form)/*
																 * XformBuilder.
																 * getXform4mStrings
																 * (schemaXml,
																 * templateXml)
																 */);
	}

	public List<PersonRepeatAttribute> getPersonRepeatAttributes(
			Integer personId, Integer personAttributeId) {
		return getXformsDAO().getPersonRepeatAttributes(personId,
				personAttributeId);
	}

	public void savePersonRepeatAttribute(
			PersonRepeatAttribute personRepeatAttribute) {
		getXformsDAO().savePersonRepeatAttribute(personRepeatAttribute);
	}

	public void deletePersonRepeatAttribute(Integer personRepeatAttributeId) {
		getXformsDAO().deletePersonRepeatAttribute(personRepeatAttributeId);
	}

	public List<Object[]> getList(String sql, String displayField,
			String valueField) {
		return getXformsDAO().getList(sql, displayField, valueField);
	}

	public PatientMedicalHistory getPatientMedicalHistory(Integer patientId) {
		return getXformsDAO().getPatientMedicalHistory(patientId);
	}

	public List<MedicalHistoryField> getMedicalHistoryFields() {
		return getXformsDAO().getMedicalHistoryFields();
	}

	public void saveMedicalHistoryField(MedicalHistoryField field) {
		getXformsDAO().saveMedicalHistoryField(field);
	}

	public void deleteMedicalHistoryField(MedicalHistoryField field) {
		getXformsDAO().deleteMedicalHistoryField(field);
	}

	public void deleteMedicalHistoryField(Integer fieldId) {
		getXformsDAO().deleteMedicalHistoryField(fieldId);
	}

	public void createFormEntryError(XformsFormEntryError formEntryError) {
		getXformsDAO().createFormEntryError(formEntryError);
	}

	public List<GlobalProperty> getXFormsGlobalProperties() {
		return getXformsDAO().getXFormsGlobalProperties();
		// return null;
	}

	public List<Object[]> getXformsList() {
		return getXformsDAO().getXformsList();
	}

	public String getLocationName(Integer locationId) {
		return getXformsDAO().getLocationName(locationId);
	}

	public String getPersonName(Integer personId) {
		return getXformsDAO().getPersonName(personId);
	}

	public String getConceptName(Integer conceptId, String localeKey) {
		return getXformsDAO().getConceptName(conceptId, localeKey);
	}

	@Override
	@Transactional(readOnly = true)
	public void sendStacktraceToAdminByEmail(String subject, Throwable exception) {
		String email = Context.getAdministrationService().getGlobalProperty(
				XformConstants.GLOBAL_PROP_KEY_ADMIN_EMAIL);
		if (email == null || email.isEmpty()) {
			return;
		}

		String emailConfig = Context.getAdministrationService()
				.getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_EMAIL_SERVER_CONFIG);

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		if (emailConfig != null && !emailConfig.isEmpty()) {
			Properties javaMailProperties = new Properties();
			
			String[] properties = emailConfig.split(";");
			for (String property : properties) {
				String[] keyAndValue = property.split("=");
				javaMailProperties.put(keyAndValue[0], keyAndValue[1]);
			}
			
			mailSender.setJavaMailProperties(javaMailProperties);
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject(subject);
		message.setText(ExceptionUtils.getFullStackTrace(exception));
		//try {
			mailSender.send(message);
		//} catch (MailException e) {
		//	log.error("Failed to send an e-mail to " + email, e);
		//}
	}

}
