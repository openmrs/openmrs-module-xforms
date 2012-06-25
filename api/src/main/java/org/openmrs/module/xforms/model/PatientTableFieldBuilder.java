package org.openmrs.module.xforms.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsService;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;


/**
 * Builds patient table fields and their corresponding values.
 * 
 * @author Daniel
 *
 */
public class PatientTableFieldBuilder {

    private static Log log = LogFactory.getLog(PatientTableFieldBuilder.class);
    
	public static List<PatientTableField> getPatientTableFields(XformsService xformsService){
		FormService formService = (FormService)Context.getService(FormService.class);

		List<PatientTableField> fields = new ArrayList<PatientTableField>();
		
		List<Integer> formIds = xformsService.getXformFormIds();
		if(formIds == null || formIds.size() == 0)
			return null;
		for(Integer formId : formIds)
			addFormTableFields(formId,fields,formService);
		return fields;
	}
	
	private static void addFormTableFields(Integer formId,List<PatientTableField> fields, FormService formService){
		try{
            Form form = formService.getForm(formId);
    		String templateXml = FormEntryWrapper.getFormTemplate(form); //new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
    		Document doc = XformBuilder.getDocument(templateXml);
    		addTableFields(doc.getRootElement(),fields);
        }
        catch(Exception e){
            log.error(e.getMessage(), e);
        }
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
				//TODO Need to use velocity variable replacement here
				val = xformsService.getPatientValue(patientId, pfld.getTableName(), pfld.getColumnName(),null);
				if(val != null)
					fieldValues.add(new PatientTableFieldValue(pfld.getId(),patientId,val));
			}
		}
		return fieldValues;
	}
}
