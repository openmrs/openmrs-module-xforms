package org.openmrs.module.xforms.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.junit.Test;
import org.kxml2.kdom.Document;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformObsEdit;
import org.openmrs.reporting.export.DataExportUtil.VelocityExceptionHandler;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * 
 * @author daniel
 *
 */
public class XformsObsEditTest extends BaseModuleContextSensitiveTest{

	public Boolean useInMemoryDatabase() {
		return false;
	}

	public void testThis(){
		String xformXml = null, layoutXml = null, localeXml = null, javaScriptSrc = null;

		/*int pos = xml.indexOf(PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
		if(pos > 0){
			xformXml = xml.substring(0,pos);
			layoutXml = FormUtil.formatXml(xml.substring(pos+PurcConstants.PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), xml.length()));
		}
		else
			xformXml = xml;*/
		
		final String PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR = "LAYOUT";
		final String PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR = "LOCALE";
		final String PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR = "JAVASCRIPT";
		
		String xml = "11245LAYOUT678JAVASCRIPT13-404";
		int pos = xml.indexOf(PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
		int pos2 = xml.indexOf(PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR);
		if(pos > 0){
			xformXml = xml.substring(0,pos);
			layoutXml = xml.substring(pos+PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), pos2 > 0 ? pos2 : xml.length());
			
			if(pos2 > 0)
				javaScriptSrc = xml.substring(pos2+PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(), xml.length());
		}

		//String xml = "11245LAYOUT678LOCALE910234JAVASCRIPT13-404";
		//String xml = "11245LAYOUT678LOCALE910234";
		//String xml = "11245LOCALE910234";
		/*String xml = "11245LOCALE910234JAVASCRIPT13-404";
		
		int pos = xml.indexOf(PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR);
		int pos2 = xml.indexOf(PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR);
		int pos3 = xml.indexOf(PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR);
		if(pos > 0){
			xformXml = xml.substring(0,pos);
			layoutXml = xml.substring(pos+PURCFORMS_FORMDEF_LAYOUT_XML_SEPARATOR.length(), (pos2 > 0 ? pos2 : (pos3 > 0 ? pos3 : xml.length())));

			if(pos2 > 0)
				localeXml = xml.substring(pos2+PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR.length(), pos3 > 0 ? pos3 : xml.length());
		
			if(pos3 > 0)
				javaScriptSrc = xml.substring(pos3+PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(), xml.length());
		}
		else if(pos2 > 0){
			xformXml = xml.substring(0,pos2);
			localeXml = xml.substring(pos2+PURCFORMS_FORMDEF_LOCALE_XML_SEPARATOR.length(), pos3 > 0 ? pos3 : xml.length());
			
			if(pos3 > 0)
				javaScriptSrc = xml.substring(pos3+PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(), xml.length());
		}
		else if(pos3 > 0){
			xformXml = xml.substring(0,pos3);
			javaScriptSrc = xml.substring(pos3+PURCFORMS_FORMDEF_JAVASCRIPT_SRC_SEPARATOR.length(), xml.length());
		}
		else
			xformXml = xml;*/
		
		System.out.println(xformXml);
		System.out.println(layoutXml);
		System.out.println(localeXml);
		System.out.println(javaScriptSrc);
	}
	
	/*public void testGetEditedEncounter() throws Exception {

		initializeInMemoryDatabase();
        authenticate();
        executeDataSet("org/openmrs/module/xforms/test/include/XformsObsEditTest.xml");
	}*/

	public void tVelocityEvaluation() throws Exception{
		
		/*select name,default_value from form_field ff inner join field f
		where ff.field_id=f.field_id
		and ff.form_id=15
		and length(default_value) > 0*/
		Calendar.getInstance().getTimeInMillis();
		int k = Calendar.getInstance().getTime().getYear();
		//Math.round(a)

		k = 0;
		
		authenticate();
		
		Locale locale = Context.getLocale();
		//System.out.println(locale.getLanguage());
		//System.out.println(locale.getISO3Language());
		//for (Locale locale : locales)
		//	;
		
		//Calendar.getInstance().add(field, amount)
		//Calendar.getInstance().
		//d.getYear();
		
		
		String xml = getDoc();
		xml = "$!{patient.getFamilyName()} with id $!{patient.getPatientIdentifier(4).getIdentifier()}";
		xml = "$!{patient.getFamilyName()} in village $!{patient.getPersonAddress().getCityVillage()}";
		//${patient.attributeMap.Cell}
		//${patient.attributeMap.HeadOfHousehold}
		//${patient.attributeMap.Sector}

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.CommonsLogLogChute" );
		ve.setProperty(CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME, "xforms_velocity");

		try {
			ve.init();

			VelocityContext velocityContext = new VelocityContext();
			Context.authenticate("guyzb", "daniel123");
			Patient patient = Context.getPatientService().getPatient(13);
			velocityContext.put("patient", patient);

			// add the error handler
			EventCartridge ec = new EventCartridge();
			ec.addEventHandler(new VelocityExceptionHandler());
			velocityContext.attachEventCartridge(ec);

			StringWriter w = new StringWriter();
			ve.evaluate(velocityContext, w, XformObsEdit.class.getName(), xml);
			xml = w.toString();

			System.out.println(xml);

		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	@Test
	public void testGetEditedEncounter() throws Exception{
		authenticate();

		String xml = getDoc();
		Document doc = XformBuilder.getDocument(xml);

		Set<Obs> obs2Void = new HashSet<Obs>();
		Encounter encounter = XformObsEdit.getEditedEncounter(null,XformBuilder.getElement(doc.getRootElement(), "form"),obs2Void);

		Context.getEncounterService().saveEncounter(encounter);

		ObsService obsService = Context.getObsService();
		for(Obs obs : obs2Void)
			obsService.voidObs(obs, "xformsmodule");
	}

	private String getDoc() throws Exception {
		BufferedReader r = new BufferedReader(new FileReader("TestXform.xml"));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			sb.append(line).append("\n");
		}

		return sb.toString();
	}
}
