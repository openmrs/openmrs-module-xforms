package org.openmrs.module.xforms.test;

import java.io.File;
import java.io.FileWriter;

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
}
