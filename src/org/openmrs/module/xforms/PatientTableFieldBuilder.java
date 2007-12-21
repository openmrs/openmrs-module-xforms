package org.openmrs.module.xforms;


import java.util.List;
import java.util.ArrayList;
import org.kxml2.kdom.*;

import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormXmlTemplateBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.Form;


/**
 * Builds patient table field and their corresponding values.
 * 
 * @author Daniel
 *
 */
public class PatientTableFieldBuilder {

	public static List<PatientTableField> getPatientTableFields(XformsService xformsService){
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);

		List<PatientTableField> fields = new ArrayList<PatientTableField>();
		
		List<Integer> formIds = xformsService.getXformFormIds();
		if(formIds == null || formIds.size() == 0)
			return null;
		for(Integer formId : formIds)
			addFormTableFields(formId,fields,formEntryService);
		return fields;
	}
	
	private static void addFormTableFields(Integer formId,List<PatientTableField> fields, FormEntryService formEntryService){
		Form form = formEntryService.getForm(formId);
		String templateXml = new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		Document doc = XformBuilder.getDocument(templateXml);
		addTableFields(doc.getRootElement(),fields);
	}
	
	private static void addTableFields(Element node,List<PatientTableField> fields){
		int numOfEntries = node.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getType(i) != Element.ELEMENT)
				continue; //Ignore all text.
			
			Element child = node.getElement(i);
			String columnName = child.getAttributeValue(null, XformBuilder.ATTRIBUTE_OPENMRS_ATTRIBUTE);
			String tableName = child.getAttributeValue(null, XformBuilder.ATTRIBUTE_OPENMRS_TABLE);
			if(!(columnName == null || tableName == null)){
				String name = child.getName();
				if(XformBuilder.isUserDefinedNode(name) && !tableFieldExists(name,fields))
					fields.add(new PatientTableField(fields.size()+1,XformBuilder.getNodePath(child),tableName,columnName));
			}
			addTableFields(child,fields);
		}
	}
	
	private static boolean tableFieldExists(String name,List<PatientTableField> fields){
		for(PatientTableField field : fields){
			if(field.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	public static List<PatientTableFieldValue> getPatientTableFieldValues(List<Integer> patientids,List<PatientTableField> fields, XformsService xformsService){
		List<PatientTableFieldValue> fieldValues = new ArrayList<PatientTableFieldValue>();
		Object val;
		for(PatientTableField pfld : fields){
			for(Integer patientId : patientids){
				val = xformsService.getPatientValue(patientId, pfld.getTableName(), pfld.getColumnName());
				if(val != null)
					fieldValues.add(new PatientTableFieldValue(pfld.getId(),patientId,val));
			}
		}
		return fieldValues;
	}
}
