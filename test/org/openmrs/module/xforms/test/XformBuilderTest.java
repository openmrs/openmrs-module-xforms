package org.openmrs.module.xforms.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformsUtil;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class XformBuilderTest extends BaseModuleContextSensitiveTest{

	public Boolean useInMemoryDatabase() {
		return false;
	}
	
	public void testSplit(){
		String result = "Baganda|1$Bacholi|2$Bagisu|3$Basoga|4$Banyankole|5";
		//split(result,0);
		
		final char FIELD_SEPARATOR = '|';
		final char RECORD_SEPARATOR = '$';
		
		String text = result; int beginIndex = 0;
		String displayField = null, valueField = null;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		while( pos > 0){
			displayField = text.substring(beginIndex, pos);
			
			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				//split(text,pos+1);
				beginIndex = pos+1;
				pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
			}
			else{
				valueField = text.substring(beginIndex);
			}
		}
	}

	public void testBuildXform() throws Exception {
		authenticate();

		//Load OpenMRS form
		Form form = Context.getFormService().getForm(15);
		System.out.println("form = " + form.getName());

		String schemaXml = XformsUtil.getSchema(form);
		String templateXml = FormEntryWrapper.getFormTemplate(form);//new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		String xform = XformBuilder.getXform4mStrings(schemaXml, templateXml);

		System.out.println("XForm: \n" + xform);
		File outFile = new File("c:\\xformbuildertest.xml");
		FileWriter out = new FileWriter(outFile);
		out.write(xform);
		out.close();
	}

	public void testBuildXformFromFixedFiles() throws Exception {
		String templateXml = getFileAsString(new File("template.xml"));
		String schemaXml = getFileAsString(new File("FormEntry.xml"));

		try{
			String xform = XformBuilder.getXform4mStrings(schemaXml, templateXml);
			System.out.println(xform);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static String getFileAsString(File file) {
		try{
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader(file));
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
	
	private void split(String text, int beginIndex){
		final char FIELD_SEPARATOR = '|';
		final char RECORD_SEPARATOR = '$';
		
		String displayField = null, valueField = null;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		if(pos > 0){
			displayField = text.substring(beginIndex, pos);
			
			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				split(text,pos+1);
			}
			else{
				valueField = text.substring(beginIndex);
			}
		}
	}
}
