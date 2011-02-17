package org.openmrs.module.xforms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;


/**
 * Builds the basic form.
 */
public class BasicFormBuilder {
	
	private static final int FIELD_TYPE_SECTION = 5;
	private static final int FIELD_TYPE_DATABASE_ELEMENT = 2;
	
	private static final String SECTION_NAME_PATIENT = "PATIENT";
	private static final String SECTION_NAME_ENCOUNTER = "ENCOUNTER";
	private static final String SECTION_NAME_OBS = "OBS";
	
	private static final String TABLE_NAME_PATIENT_NAME = "patient_name";
	private static final String TABLE_NAME_PATIENT = "patient";
	private static final String TABLE_NAME_PATIENT_IDENTIFIER = "patient_identifier";
	private static final String TABLE_NAME_ENCOUNTER = "encounter";
	
	private static final String FIELD_FAMILY_NAME = "PATIENT.FAMILY_NAME";
	private static final String FIELD_GIVEN_NAME = "PATIENT.GIVEN_NAME";
	private static final String FIELD_MIDDLE_NAME = "PATIENT.MIDDLE_NAME";
	private static final String FIELD_MEDICAL_RECORD_NUMBER = "PATIENT.MEDICAL_RECORD_NUMBER";
	private static final String FIELD_PATIENT_ID = "PATIENT.PATIENT_ID";
	private static final String FIELD_SEX = "PATIENT.SEX";
	private static final String FIELD_BIRTHDATE = "PATIENT.BIRTHDATE";
	private static final String FIELD_ENCOUNTER_DATETIME = "ENCOUNTER.ENCOUNTER_DATETIME";
	private static final String FIELD_LOCATION_ID = "ENCOUNTER.LOCATION_ID";
	private static final String FIELD_PROVIDER_ID = "ENCOUNTER.PROVIDER_ID";
	
	
	public static void addDefaultFields(Form form){
		form.setXslt(getFormXslt());
		
		FormService formService = Context.getFormService();
		
		//Add patient section.
		Field patientSection = getPatientSection(formService);
		FormField patientFormField = getNewFormField(); patientSection.toString();
		patientFormField.setField(patientSection);
		patientFormField.setFieldNumber(1);
		form.addFormField(patientFormField);

		//Add encounter section.
		Field encounterSection = getEncounterSection(formService);
		FormField encounterFormField = getNewFormField();
		encounterFormField.setField(encounterSection);
		encounterFormField.setFieldNumber(2);
		form.addFormField(encounterFormField);
		
		//Add obs section.
		Field obsSection = getObsSection(formService);
		FormField obsFormField = getNewFormField();
		obsFormField.setField(obsSection);
		obsFormField.setFieldNumber(3);
		form.addFormField(obsFormField);
		
		//Add patient section fields.
		addDatabaseElementField(formService, form, FIELD_FAMILY_NAME, TABLE_NAME_PATIENT_NAME, "family_name", "$!{patient.getFamilyName()}", patientFormField);
		addDatabaseElementField(formService, form, FIELD_GIVEN_NAME, TABLE_NAME_PATIENT_NAME, "given_name", "$!{patient.getGivenName()}", patientFormField);
		addDatabaseElementField(formService, form, FIELD_MIDDLE_NAME, TABLE_NAME_PATIENT_NAME, "middle_name", "$!{patient.getMiddleName()}", patientFormField);
		addDatabaseElementField(formService, form, FIELD_BIRTHDATE, TABLE_NAME_PATIENT, "birthdate", "$!{date.format($patient.getBirthdate())}", patientFormField);
		addDatabaseElementField(formService, form, FIELD_SEX, TABLE_NAME_PATIENT, "gender", "$!{patient.getGender()}", patientFormField);
		addDatabaseElementField(formService, form, FIELD_PATIENT_ID, TABLE_NAME_PATIENT, "patient_id", "$!{patient.getPatientId()}", patientFormField);
		addDatabaseElementField(formService, form, FIELD_MEDICAL_RECORD_NUMBER, TABLE_NAME_PATIENT_IDENTIFIER, "identifier", "$!{patient.getPatientIdentifier(1).getIdentifier()}", patientFormField);
		
		//Add encounter section fields.
		addDatabaseElementField(formService, form, FIELD_ENCOUNTER_DATETIME, TABLE_NAME_ENCOUNTER, "encounter_datetime", null, encounterFormField);
		addDatabaseElementField(formService, form, FIELD_LOCATION_ID, TABLE_NAME_ENCOUNTER, "location_id", null, encounterFormField);
		addDatabaseElementField(formService, form, FIELD_PROVIDER_ID, TABLE_NAME_ENCOUNTER, "provider_id", null, encounterFormField);
		
		
		//Finally save everything.
		formService.saveForm(form);
	}
	
	private static Field getPatientSection(FormService formService){
		Field field = getField(formService, SECTION_NAME_PATIENT, FIELD_TYPE_SECTION);
		if(field == null){
			field = getNewField();
			field.setName(SECTION_NAME_PATIENT);
			field.setFieldType(formService.getFieldType(FIELD_TYPE_SECTION));
			field.setDescription("Patient section of form");
		}
		
		return field;
	}
	
	private static Field getEncounterSection(FormService formService){
		Field field = getField(formService, SECTION_NAME_ENCOUNTER, FIELD_TYPE_SECTION);
		if(field == null){
			field = getNewField();
			field.setName(SECTION_NAME_ENCOUNTER);
			field.setFieldType(formService.getFieldType(FIELD_TYPE_SECTION));
			field.setDescription("Encounter section of form");
		}
		
		return field;
	}
	
	private static Field getObsSection(FormService formService){
		Field field = getField(formService, SECTION_NAME_OBS, FIELD_TYPE_SECTION);
		if(field == null)
			field = getField(formService, SECTION_NAME_OBS, 1); //concept
		
		if(field == null){
			field = getNewField();
			field.setName(SECTION_NAME_OBS);
			field.setFieldType(formService.getFieldType(FIELD_TYPE_SECTION));
			field.setDescription("Obs section of form");
		}
		
		return field;
	}
	
	private static Field getField(FormService formService, String name, int type){
		List<Field> fields = formService.getFields(name);
		if(fields != null){
			for(Field field : fields){
				if(field.getFieldType() != null && 
						field.getFieldType().getFieldTypeId() == type && 
						field.getName().equalsIgnoreCase(name)){
					return field;
				}
			}
		}
		
		return null;
	}
	
	private static void addDatabaseElementField(FormService formService, Form form, String name, String tableName, String attributeName, String defaultValue, FormField parentFormField){
		Field field = getField(formService, name, FIELD_TYPE_DATABASE_ELEMENT);
		if(field == null){
			field = getNewField();
			field.setName(name);
			field.setFieldType(formService.getFieldType(FIELD_TYPE_DATABASE_ELEMENT));
			field.setTableName(tableName);
			field.setAttributeName(attributeName);
			field.setDefaultValue(defaultValue);
		}
		
		FormField formField = getNewFormField();
		formField.setField(field);
		formField.setParent(parentFormField);
		form.addFormField(formField);
	}
	
	private static Field getNewField(){
		Field field = new Field();
		field.setUuid(UUID.randomUUID().toString());
		field.setCreator(Context.getAuthenticatedUser());
		field.setDateCreated(new Date());
		
		return field;
	}
	
	private static FormField getNewFormField(){
		FormField formField = new FormField();
		formField.setUuid(UUID.randomUUID().toString());
		formField.setCreator(Context.getAuthenticatedUser());
		formField.setSortWeight(1.0f);
		formField.setDateCreated(new Date());
		
		return formField;
	}
	
	private static String getFormXslt(){
		
		try{
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(BasicFormBuilder.class.getResourceAsStream("form_xslt.xml"), XformConstants.DEFAULT_CHARACTER_ENCODING));
			
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
}