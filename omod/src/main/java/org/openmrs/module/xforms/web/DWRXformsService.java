package org.openmrs.module.xforms.web;

import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.xforms.MedicalHistoryField;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.web.WebUtil;
import org.openmrs.web.dwr.FieldListItem;
import org.openmrs.web.dwr.FormFieldListItem;


public class DWRXformsService {

	protected final Log log = LogFactory.getLog(getClass());

	public String getXform(String formId) throws Exception {
		Xform xform = null;

		//only fill the objects if the user has authenticated properly
		if (Context.isAuthenticated()) {
			XformsService svc = (XformsService)Context.getService(XformsService.class);
			if(formId != null && formId.trim().length() > 0){
				xform = svc.getXform(Integer.parseInt(formId),true);
				return xform.getXformXml();
			}
		}

		return null;
	}

	public boolean saveXform(Xform xform){
		try{
			if (Context.isAuthenticated()) {
				XformsService svc = (XformsService)Context.getService(XformsService.class);
				Xform xf = svc.getXform(xform.getFormId(),true);
				xf.setXformXml(xform.getXformXml());
				svc.saveXform(xf);
				return true;
			}
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}

		return false;
	}

	public Field getField(Integer fieldId) {
		Field f = new Field();
		return f;
	}

	public FormFieldListItem getFormField(Integer formFieldId) {
		FormField f = new FormField();
		return new FormFieldListItem(f, Context.getLocale());
	}

	public List<FormFieldListItem> getFormFields(Integer formId) {
		List<FormFieldListItem> formFields = new Vector<FormFieldListItem>();
		return formFields;
	}

	public List<FieldListItem> findFields(String txt) {
		List<FieldListItem> fields = new Vector<FieldListItem>();
		return fields;
	}

	public List<Object> findFieldsAndConcepts(String txt) {
		Locale locale = Context.getLocale();

		// return list will contain ConceptListItems and FieldListItems.
		List<Object> objects = new Vector<Object>();
		return objects;
	}

	public void saveFormField(Integer fieldId, String name, Integer tabIndex, boolean isNew) {
		((XformsService)Context.getService(XformsService.class)).saveMedicalHistoryField(new MedicalHistoryField(fieldId,name,tabIndex,isNew));
	}

	public void deleteFormField(Integer fieldId) {
		if (Context.isAuthenticated())
			((XformsService)Context.getService(XformsService.class)).deleteMedicalHistoryField(fieldId);
	}

	public static TreeMap<Integer, TreeSet<FormField>> getFormStructure(Form form) {
		TreeMap<Integer, TreeSet<FormField>> formStructure = new TreeMap<Integer, TreeSet<FormField>>();
		Integer base = Integer.valueOf(0);
		formStructure.put(base, new TreeSet<FormField>());

		FormField parentField = new FormField();
		Field field = new Field();
		field.setName("DRAG AND DROP FIELDS HERE");
		field.setFieldId(-1);
		parentField.setField(field);
		parentField.setSortWeight(0f);
		parentField.setFormFieldId(-1);

		//form.addFormField(parent);
		formStructure.get(base).add(parentField);	

		for (FormField formField : form.getFormFields()) {
			FormField parent = formField.getParent();
			if (parent != null) 
				formStructure.get(0).add(formField);
		}

		return formStructure;
	}

	public static TreeMap<Integer, TreeSet<MedicalHistoryField>> getMedicalHistoryFieldsStructure(List<MedicalHistoryField> fields) {
		TreeMap<Integer, TreeSet<MedicalHistoryField>> formStructure = new TreeMap<Integer, TreeSet<MedicalHistoryField>>();
		Integer base = Integer.valueOf(0);
		formStructure.put(base, new TreeSet<MedicalHistoryField>());

		MedicalHistoryField parentField = new MedicalHistoryField();
		parentField.setName("DRAG AND DROP FIELDS HERE");
		parentField.setFieldId(-1);
		parentField.setTabIndex(0);
		parentField.setNew(false);

		formStructure.get(base).add(parentField);	

		if(fields != null && fields.size() > 0){
			for (MedicalHistoryField field : fields)
				formStructure.get(0).add(field);
		}

		return formStructure;
	}

	public String getJSTree() {
		XformsService xformsService = (XformsService)Context.getService(XformsService.class);
		List<MedicalHistoryField> fields = xformsService.getMedicalHistoryFields();
		String s = generateJSTree(getMedicalHistoryFieldsStructure(fields), 0, Context.getLocale());
		return s;
	}

	private String generateJSTree(TreeMap<Integer, TreeSet<MedicalHistoryField>> formFields, Integer current, Locale locale) {
		String s = "";

		if (formFields.containsKey(current)) {
			TreeSet<MedicalHistoryField> set = formFields.get(current);
			for (MedicalHistoryField ff : set) {
				s += generateFormFieldJavascript(ff, locale);
				if (formFields.containsKey(ff.getFieldId())) 
					s += generateJSTree(formFields, ff.getFieldId(), locale);
			}
		}

		return s;
	}

	private String generateFormFieldJavascript(MedicalHistoryField field, Locale locale) {

		String parent = "''";
		if(field.getFieldId() != -1)
			parent = "-1";

		return "addNode(tree, {fieldId: " + field.getFieldId() + ", " + 
		"parent: " + parent + ", " + 
		"fieldName: \"" + WebUtil.escapeQuotesAndNewlines(field.getName()) + "\", " + 
		"isNew: " + field.isNew() + ", " + 
		"tabIndex: " + field.getTabIndex() + "});";
	}
	
	public boolean isAuthenticated() {
        return Context.isAuthenticated();
    }
    
    public boolean authenticate(String user, String pass) {
        try {
            Context.authenticate(user, pass);
            return true;
        } catch (ContextAuthenticationException ex) {
            return false;
        }
    }
}
