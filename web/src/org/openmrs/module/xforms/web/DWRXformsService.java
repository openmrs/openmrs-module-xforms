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
import org.openmrs.module.xforms.MedicalHistoryField;
import org.openmrs.module.xforms.Xform;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.web.WebUtil;
import org.openmrs.web.dwr.FieldListItem;
import org.openmrs.web.dwr.FormFieldListItem;


public class DWRXformsService {

	protected final Log log = LogFactory.getLog(getClass());

	public String getXform(String formId){
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

	/*public List<FormListItem> findForms(String text, boolean includeUnpublished) {
		List<FormListItem> forms = new Vector<FormListItem>();

		for(Form form : Context.getFormService().findForms(text, includeUnpublished, false)) {
			forms.add(new FormListItem(form));
		}

		return forms;
	}

	public List<FormListItem> getForms(boolean includeUnpublished) {
		List<FormListItem> forms = new Vector<FormListItem>();

		for(Form form : Context.getFormService().getForms(!includeUnpublished)) {
			forms.add(new FormListItem(form));
		}

		return forms;
	}*/

	public Field getField(Integer fieldId) {
		Field f = new Field();
		//FormService fs = Context.getFormService();
		//f = fs.getField(fieldId);
		return f;
	}

	public FormFieldListItem getFormField(Integer formFieldId) {
		FormField f = new FormField();
		//FormService fs = Context.getFormService();
		//f = fs.getFormField(formFieldId);
		return new FormFieldListItem(f, Context.getLocale());
	}

	public List<FormFieldListItem> getFormFields(Integer formId) {
		List<FormFieldListItem> formFields = new Vector<FormFieldListItem>();
		/*Form form = Context.getFormService().getForm(formId);
		for (FormField ff : form.getFormFields())
			formFields.add(new FormFieldListItem(ff, Context.getLocale()));*/
		return formFields;
	}

	public List<FieldListItem> findFields(String txt) {
		List<FieldListItem> fields = new Vector<FieldListItem>();

		//for(Field field : Context.getFormService().findFields(txt))
		//	fields.add(new FieldListItem(field, Context.getLocale()));

		return fields;
	}

	public List<Object> findFieldsAndConcepts(String txt) {
		Locale locale = Context.getLocale();

		// return list will contain ConceptListItems and FieldListItems.
		List<Object> objects = new Vector<Object>();

		/*Concept concept = null;
		try {
			Integer i = Integer.valueOf(txt);
			concept = Context.getConceptService().getConcept(i);
		}
		catch (NumberFormatException e) {}

		Map<Integer, Boolean> fieldForConceptAdded = new HashMap<Integer, Boolean>();

		if (concept != null) {
			for (Field field : Context.getFormService().findFields(concept)) {
				FieldListItem fli = new FieldListItem(field, locale); 
				if (!objects.contains(fli))
					objects.add(fli);
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			if (!fieldForConceptAdded.containsKey((concept.getConceptId()))) {
				objects.add(new ConceptListItem(concept, locale));
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}

		}

		for(Field field : Context.getFormService().findFields(txt)) {
			FieldListItem fi = new FieldListItem(field, locale);
			if (!objects.contains(fi)) {
				objects.add(fi);
				concept = field.getConcept();
				if (concept != null)
					fieldForConceptAdded.put(concept.getConceptId(), true);
			}

		}

		List<ConceptWord> conceptWords = Context.getConceptService().findConcepts(txt, locale, false);
		for (ConceptWord word : conceptWords) {
			concept = word.getConcept();
			for (Field field : Context.getFormService().findFields(concept)) {
				FieldListItem fli = new FieldListItem(field, locale);
				if (!objects.contains(fli))
					objects.add(fli);
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			if (!fieldForConceptAdded.containsKey((concept.getConceptId()))) {
				objects.add(new ConceptListItem(word));
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
		}

		Collections.sort(objects, new FieldConceptSort<Object>(locale));*/

		return objects;
	}

	/*public String getJSTree() {
		Form form = Context.getFormService().getForm(15);
		//TreeMap<Integer, TreeSet<FormField>> formFields = FormUtil.getFormStructure(form);

		return generateJSTree(form.getFormFields(), Context.getLocale());
	}*/


	public void saveFormField(Integer fieldId, String name, Integer tabIndex, boolean isNew) {
		
		((XformsService)Context.getService(XformsService.class)).saveMedicalHistoryField(new MedicalHistoryField(fieldId,name,tabIndex,isNew));

		/*if (formFieldId != null && formFieldId != 0)
			ff = fs.getFormField(formFieldId);
		else
			ff = new FormField(formFieldId);

		ff.setForm(fs.getForm(formId));
		if (parent == null)
			ff.setParent(null);
		else if (!parent.equals(ff.getFormFieldId()))
			ff.setParent(fs.getFormField(parent));
		ff.setFieldNumber(number);
		ff.setFieldPart(part);
		ff.setPageNumber(page);
		ff.setMinOccurs(min);
		ff.setMaxOccurs(max);
		ff.setRequired(required);
		ff.setSortWeight(sortWeight);

		log.debug("fieldId: " + fieldId);
		log.debug("formFieldId: " + formFieldId);
		log.debug("parentId: "+ parent);
		log.debug("parent: " + ff.getParent());

		if (fieldId != null && fieldId != 0)
			field = fs.getField(fieldId);
		else
			field = new Field(fieldId);

		if (field == null) {
			log.error("Field is null. Field Id: " + fieldId);
		}

		field.setName(name);
		field.setDescription(fieldDesc);
		field.setFieldType(fs.getFieldType(fieldTypeId));
		if (conceptId != null && conceptId != 0)
			field.setConcept(cs.getConcept(conceptId));
		else
			field.setConcept(null);
		field.setTableName(table);
		field.setAttributeName(attr);
		field.setDefaultValue(defaultValue);
		field.setSelectMultiple(multiple);

		ff.setField(field);
		fs.updateFormField(ff);

		fieldId = ff.getField().getFieldId();
		formFieldId = ff.getFormFieldId();

		Integer[] arr = {fieldId, 0};

		return arr;*/
	}

	public void deleteFormField(Integer fieldId) {
		if (Context.isAuthenticated())
			((XformsService)Context.getService(XformsService.class)).deleteMedicalHistoryField(fieldId);
			//Context.getFormService().deleteFormField(Context.getFormService().getFormField(id));
	}

	/* private String generateJSTree(Set<FormField> formFields, Locale locale) {
		String s = "";

		//if (formFields.containsKey(current)) {
		//	TreeSet<FormField> set = formFields.get(current);
		FormField parent = new FormField();
		Field field = new Field();
		field.setName("Drag and drop fields here");
		field.setFieldId(0);
		parent.setField(field);
		parent.setSortWeight(0f);
		parent.setFormFieldId(0);

		s += generateFormFieldJavascript(parent, locale);

			for (FormField ff : formFields) {
				ff.setParent(parent);
				s += generateFormFieldJavascript(ff, locale);
				if (formFields.containsKey(ff.getFormFieldId())) {
					s += generateJSTree(formFields,/* ff.getFormFieldId(),*//* locale);
				}
			}
		//}
		System.out.println(s);
		return s;
	}

    private String generateFormFieldJavascript(FormField ff, Locale locale) {

    	String parent = "''";
		if (ff.getParent() != null)
			parent = ff.getParent().getFormFieldId().toString();

		Field field = ff.getField();

    	return "addNode(tree, {parent: " + parent + ", " + 
    					"fieldId: " + field.getFieldId() + ", " + 
    					"fieldName: \"" + WebUtil.escapeQuotesAndNewlines(field.getName()) + "\", " + 
    					"tabIndex: " + ff.getSortWeight() + "});";
    }*/

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
		/*Form form = Context.getFormService().getForm(15);
		TreeMap<Integer, TreeSet<FormField>> formFields = getFormStructure(form);
		return generateJSTree(formFields, 0, Context.getLocale());*/
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
		/*if (ff.getParent() != null)
			parent = "-1"; //ff.getParent().getFormFieldId().toString();

		Field field = ff.getField();
		Concept concept = new Concept();
		ConceptName conceptName = new ConceptName();
		Boolean isSet = false;
		if (field.getConcept() != null) {
			concept = field.getConcept();
			conceptName = concept.getName(locale);
			isSet = concept.isSet();
		}

		if (log.isDebugEnabled())
			log.debug("ff.getFormFieldId: " + ff.getFormFieldId());*/

		return "addNode(tree, {fieldId: " + field.getFieldId() + ", " + 
		"parent: " + parent + ", " + 
		"fieldName: \"" + WebUtil.escapeQuotesAndNewlines(field.getName()) + "\", " + 
		"isNew: " + field.isNew() + ", " + 
		"tabIndex: " + field.getTabIndex() + "});";
	}
	
	/**
     * Sorts loosely on:
     *   FieldListItems first, then concepts
     *   FieldListItems with higher number of forms first, then lower
     *   Concepts with shorter names before longer names
     * @author bwolfe
     *
     * @param <Obj>
     */
    
    /*private class FieldConceptSort<Obj extends Object> implements Comparator<Object> {
		Locale locale;
		FieldConceptSort(Locale locale) {
			this.locale = locale;
		}
		public int compare(Object o1, Object o2) {
			if (o1 instanceof FieldListItem && o2 instanceof FieldListItem) {
				FieldListItem f1 = (FieldListItem)o1;
				FieldListItem f2 = (FieldListItem)o2;
				Integer numForms1 = f1.getNumForms();
				Integer numForms2 = f2.getNumForms();
				return numForms2.compareTo(numForms1);
			}
			else if (o1 instanceof FieldListItem && o2 instanceof ConceptListItem) {
				return -1;
			}
			else if (o1 instanceof ConceptListItem && o2 instanceof FieldListItem) {
				return 1;
			}
			else if (o1 instanceof ConceptListItem && o2 instanceof ConceptListItem) {
				ConceptListItem c1 = (ConceptListItem)o1;
				ConceptListItem c2 = (ConceptListItem)o2;
				int length1 = c1.getName().length();
				int length2 = c2.getName().length();
				return new Integer(length1).compareTo(new Integer(length2));
			}
			else
				return 0;
		}
    }*/
}
