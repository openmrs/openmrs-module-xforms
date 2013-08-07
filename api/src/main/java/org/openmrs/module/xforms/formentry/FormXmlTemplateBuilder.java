package org.openmrs.module.xforms.formentry;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.FormConstants;
import org.openmrs.util.FormUtil;
import org.openmrs.util.VelocityExceptionHandler;
import org.springframework.util.StringUtils;

/**
 * XML template builder for OpenMRS forms.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormXmlTemplateBuilder {

	protected final Log log = LogFactory.getLog(getClass());
	
	private VelocityEngine ve;

	Form form;
	String url;

	/**
	 * Construct an XML template builder for generating patient-based templates
	 * for a given OpenMRS form.
	 * 
	 * @param context
	 *            active OpenMRS context
	 * @param form
	 *            OpenMRS form for which template(s) will be made
	 * @param url
	 *            absolute (full, including "http://" and ending with ".xsn")
	 *            url location of InfoPath form (.xsn file)
	 */
	public FormXmlTemplateBuilder(Form form, String url) {
		this.form = form;
		this.url = url;
	}
	
	public synchronized String getXmlTemplate(Patient patient) {
		initializeVelocity();
		
		VelocityContext velocityContext = new VelocityContext();

		if (patient != null) {
			velocityContext.put("form", form);
			velocityContext.put("url", url);
			User user = Context.getAuthenticatedUser();
			String enterer;
			if (user != null)
				enterer = user.getUserId() + "^" + user.getGivenName() + " "
						+ user.getFamilyName();
			else
				enterer = "";

			velocityContext.put("enterer", enterer);
			velocityContext.put("patient", patient);
			velocityContext.put("timestamp", new SimpleDateFormat(
					"yyyyMMdd'T'HH:mm:ss.SSSZ"));
			velocityContext.put("date", new SimpleDateFormat("yyyyMMdd"));
			velocityContext.put("time", new SimpleDateFormat("HH:mm:ss"));
			
			List<Encounter> encounters = Context.getEncounterService().getEncountersByPatientId(patient.getPatientId(), false);
			velocityContext.put("patientEncounters", encounters);
			
			List<Relationship> relationships = Context.getPersonService().getRelationshipsByPerson(patient);
			velocityContext.put("relationships", relationships);
		}
		
		// adding the error handler for velocity
		EventCartridge ec = new EventCartridge();
		ec.addEventHandler(new VelocityExceptionHandler());
		velocityContext.attachEventCartridge(ec);

		String template = null;
		try {
			StringWriter w = new StringWriter();
			Velocity.evaluate(velocityContext, w, this.getClass().getName(),
					form.getTemplate());
			template = w.toString();
		} catch (Exception e) {
			log.error("Error evaluating default values for form "
					+ form.getName() + "[" + form.getFormId() + "]", e);
		}
		return template;
	}
	
	/**
	 * A utility method to initialize Velocity. This could be
	 * called in the constructor, but putting it in a separate
	 * method like this allows for late-initialization only
	 * when someone actually uses this servlet.
	 */
	private void initializeVelocity() {
		if (ve == null) {
			ve = new VelocityEngine();

			ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.CommonsLogLogChute" );
			ve.setProperty(CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME, 
					"xforms_velocity");
			try {
				ve.init();
			} catch (Exception e) {
				log.error("velocity init failed", e);
			}		
		}
	}

	/**
	 * Returns the XML template for a form
	 * 
	 * @param includeDefaultScripts
	 *            if true, field defaults are inserted into the template
	 * @return XML template for a form
	 */
	public synchronized String getXmlTemplate(boolean includeDefaultScripts) {
		StringBuffer xml = new StringBuffer();

		xml.append(FormXmlTemplateFragment.header(form, url));
		xml.append(FormXmlTemplateFragment.openForm(form, FormEntryWrapper
				.getFormSchemaNamespace(form), includeDefaultScripts));

		TreeMap<Integer, TreeSet<FormField>> formStructure = FormUtil
				.getFormStructure(form);

		renderStructure(xml, formStructure, includeDefaultScripts, 0, 2);

		xml.append(FormXmlTemplateFragment.closeForm());

		return xml.toString();
	}

	/**
	 * Recursively creates the xml structure for the given formStructure
	 * 
	 * @param xml
	 * @param formStructure
	 * @param includeDefaultScripts
	 * @param sectionId
	 * @param indent
	 */
	public void renderStructure(StringBuffer xml,
			TreeMap<Integer, TreeSet<FormField>> formStructure,
			boolean includeDefaultScripts, Integer sectionId, int indent) {
		
		// if this sectionId is invalid, quit
		if (!formStructure.containsKey(sectionId))
			return;
		
		TreeSet<FormField> section = formStructure.get(sectionId);
		if (section == null || section.size() < 1)
			return;
		
		Vector<String> tagList = new Vector<String>();
		char[] indentation = new char[indent];
		
		// loop over each field in this form
		for (FormField formField : section) {
			Field field = formField.getField();
			Integer fieldTypeId = field.getFieldType().getFieldTypeId();
			String xmlTag = FormUtil.getNewTag(field.getName(), tagList);
			Integer subSectionId = formField.getFormFieldId();
			for (int i = 0; i < indent; i++)
				indentation[i] = ' ';
			xml.append(indentation);
			
			// if this is a repeating element and they have defined a default value
			// for the field, then we want to repeat this element
			boolean repeatingElement = false;
			if (formField.getMaxOccurs() != null && formField.getMaxOccurs().equals(-1) &&
					StringUtils.hasLength(field.getDefaultValue()) &&
					includeDefaultScripts) {
				xml.append("#{foreach}($listItem in " + field.getDefaultValue() + ")");
				repeatingElement = true;
			}
			
			// write out the element based on its type
			xml.append("<" + xmlTag);
			if (fieldTypeId.equals(FormConstants.FIELD_TYPE_DATABASE)) {
				xml.append(" openmrs_table=\"");
				xml.append(field.getTableName());
				xml.append("\" openmrs_attribute=\"");
				xml.append(field.getAttributeName());
                //include UUID field attribute
                xml.append("\" openmrs_field_uuid=\"");
                xml.append(field.getUuid());
				if (formStructure.containsKey(subSectionId)) {
					xml.append("\">\n");
					renderStructure(xml, formStructure, includeDefaultScripts,
							subSectionId, indent
									+ FormConstants.INDENT_SIZE);
					xml.append(indentation);
				} else {
					if (field.getDefaultValue() != null) {
						xml.append("\">");
						if (includeDefaultScripts)
							xml.append(field.getDefaultValue());
					} else {
						if (!formField.isRequired())
							xml.append("\" xsi:nil=\"true");
						xml.append("\">");
					}
				}
				xml.append("</");
				xml.append(xmlTag);
				xml.append(">\n");
			} else if (fieldTypeId
					.equals(FormConstants.FIELD_TYPE_CONCEPT)) {
				Concept concept = field.getConcept();
				String hl7Abbr = concept.getDatatype().getHl7Abbreviation();
                xml.append(" openmrs_field_uuid=\"");
                xml.append(field.getUuid());
                xml.append("\" openmrs_concept=\"");
				xml.append(StringEscapeUtils.escapeXml(FormUtil.conceptToString(concept, Context
						.getLocale())));
				xml.append("\" openmrs_datatype=\"");
				xml.append(hl7Abbr);
				xml.append("\"");
				if (formStructure.containsKey(subSectionId)) {
					xml.append(">\n");
					renderStructure(xml, formStructure, includeDefaultScripts,
							subSectionId, indent + FormConstants.INDENT_SIZE);
					xml.append(indentation);
					xml.append("</");
					xml.append(xmlTag);
					xml.append(">\n");
				} else {
					if (hl7Abbr.equals(FormConstants.HL7_CODED)
							|| hl7Abbr.equals(
									FormConstants.HL7_CODED_WITH_EXCEPTIONS)) {
						xml.append(" multiple=\"");
						xml.append(field.getSelectMultiple() ? "1" : "0");
						xml.append("\"");
					}
					xml.append(">\n");
					xml.append(indentation);
					xml.append(indentation);
					xml.append("<date xsi:nil=\"true\"></date>\n");
					xml.append(indentation);
					xml.append(indentation);
					xml.append("<time xsi:nil=\"true\"></time>\n");
					if ((hl7Abbr.equals(FormConstants.HL7_CODED) || 
						 hl7Abbr.equals(FormConstants.HL7_CODED_WITH_EXCEPTIONS))
							&& field.getSelectMultiple()) {
						for (ConceptAnswer answer : concept
								.getSortedAnswers(Context.getLocale())) {
							xml.append(indentation);
							xml.append(indentation);
							xml.append("<");
							String answerConceptName = answer
									.getAnswerConcept().getName(
											Context.getLocale()).getName();
							Drug answerDrug = answer.getAnswerDrug();
							if (answerDrug == null) {
								String answerTag = FormUtil.getXmlToken(
										answerConceptName);
								xml.append(answerTag);
								xml.append(" openmrs_concept=\"");
								xml.append(StringEscapeUtils.escapeXml(FormUtil.conceptToString(answer
										.getAnswerConcept(), Context
										.getLocale())));
								xml.append("\">false</");
								xml.append(answerTag);
								xml.append(">\n");
							} else {
								String answerDrugName = answerDrug.getName();
								String answerTag = FormUtil.getXmlToken(
										answerDrugName);
								xml.append(answerTag);
								xml.append(" openmrs_concept=\"");
								xml.append(StringEscapeUtils.escapeXml(FormUtil.conceptToString(answer
										.getAnswerConcept(), Context
										.getLocale())));
								xml.append("^");
								xml.append(FormUtil.drugToString(answerDrug));
								xml.append("\">false</");
								xml.append(answerTag);
								xml.append(">\n");
							}
						}
					} else {
						xml.append(indentation);
						xml.append(indentation);
						xml.append("<value");
						if (hl7Abbr.equals(FormConstants.HL7_BOOLEAN))
							xml.append(" infopath_boolean_hack=\"1\"");
						xml.append(" xsi:nil=\"true\"></value>\n");
					}
					xml.append(indentation);
					xml.append("</");
					xml.append(xmlTag);
					xml.append(">\n");
				}
			} else {
				// if the type isn't db or concept, just do something generic
				xml.append(">\n");
				renderStructure(xml, formStructure, includeDefaultScripts,
						subSectionId, indent + FormConstants.INDENT_SIZE);
				xml.append(indentation);
				xml.append("</");
				xml.append(xmlTag);
				xml.append(">\n");
			}
			
			// if this element is declared as 0-n and there is a default value on it
			// then we need to repeat the whole element
			if (repeatingElement) {
				xml.append("#{end}");
			}
			
		}
	}
}
