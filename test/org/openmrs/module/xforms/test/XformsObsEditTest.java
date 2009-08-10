package org.openmrs.module.xforms.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.kxml2.kdom.Document;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformObsEdit;
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
	
	/*public void testGetEditedEncounter() throws Exception {
		
		initializeInMemoryDatabase();
        authenticate();
        executeDataSet("org/openmrs/module/xforms/test/include/XformsObsEditTest.xml");
	}*/
	
	@Test
	public void testGetEditedEncounter() throws Exception{
		authenticate();
	
		BufferedReader r = new BufferedReader(new FileReader("TestXform.xml"));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = r.readLine();
            if (line == null)
                break;
            sb.append(line).append("\n");
        }
        
		Document doc = XformBuilder.getDocument(sb.toString());
		
		Set<Obs> obs2Void = new HashSet<Obs>();
		Encounter encounter = XformObsEdit.getEditedEncounter(XformBuilder.getElement(doc.getRootElement(), "form"),obs2Void);
		
		Context.getEncounterService().saveEncounter(encounter);
		
		ObsService obsService = Context.getObsService();
		for(Obs obs : obs2Void)
			obsService.voidObs(obs, "xformsmodule");
	}
}
