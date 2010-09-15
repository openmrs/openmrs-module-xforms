package org.openmrs.module.xforms.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.openmrs.module.xforms.XformConstants;


public class BuilderTest extends TestCase {

	public void test(){
		try{
			//String schemaXml = getFileAsString(new File("/Users/danielkayiwa/Downloads/FormEntry-2.xsd"));
			//String templateXml = getFileAsString(new File("/Users/danielkayiwa/Downloads/template.xml"));
			
			String schemaXml = getFileAsString(new File("/Users/danielkayiwa/Downloads/FormEntry(3).xsd"));
			String templateXml = getFileAsString(new File("/Users/danielkayiwa/Downloads/template(3).xml"));
			
			String xform = Builder2.getXform4mStrings(schemaXml, templateXml);
			System.out.println(xform);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static String getFileAsString(File file) {
		try{
			StringBuffer fileData = new StringBuffer(1000);
			//BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),XformConstants.DEFAULT_CHARACTER_ENCODING));

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
