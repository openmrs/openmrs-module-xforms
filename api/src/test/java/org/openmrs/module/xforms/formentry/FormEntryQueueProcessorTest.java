/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.xforms.formentry;

import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class FormEntryQueueProcessorTest extends BaseModuleContextSensitiveTest {
	
	private static final String TEST_XFORM_FILE = "test_xform_with_complex_obs.xml";
	
	/**
	 * @see {@link FormEntryQueueProcessor#transformFormEntryQueue(FormEntryQueue,null)}
	 */
	@Test
	@Verifies(value = "should transform xml data with a serialized complex obs", method = "transformFormEntryQueue(FormEntryQueue,null)")
	public void transformFormEntryQueue_shouldTransformXmlDataWithASerializedComplexObs() throws Exception {
		String handlerName = "NeighborHandler";
		
		Context.getObsService().registerHandler(handlerName, new NeighborObsHandler());
		try {
			
			String xml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(TEST_XFORM_FILE));
			FormEntryQueue formEntryQueue = new FormEntryQueue();
			formEntryQueue.setCreator(Context.getAuthenticatedUser());
			formEntryQueue.setDateCreated(new Date());
			formEntryQueue.setFormData(xml);
			formEntryQueue.setFileSystemUrl("/path");
			
			HL7InQueue hl7inQueue = new FormEntryQueueProcessor().transformFormEntryQueue(formEntryQueue, true);
			Assert.assertEquals(2, StringUtils.countMatches(hl7inQueue.getHL7Data(), "OBX"));
			Assert.assertTrue(hl7inQueue.getHL7Data().indexOf("{\"firstname\":\"Horatio\", \"lastname\":\"Hornblower\"}") > 0);
			Assert.assertTrue(hl7inQueue.getHL7Data().indexOf("{\"firstname\":\"John\", \"lastname\":\"Doe\"}") > 0);
		}
		finally {
			Context.getObsService().removeHandler(handlerName);
		}
	}
	
	public class NeighborObsHandler implements ComplexObsHandler {
		
		public Obs saveObs(Obs obs) throws APIException {
			return null;
		}
		
		public Obs getObs(Obs obs, String view) {
			return null;
		}
		
		public boolean purgeComplexData(Obs obs) {
			return false;
		}
		
		//@Override
		public String getSchema(String format) {
			return null;
		}
		
		public String serializeFormData(String data) {
			//This is a test implementation that converts the incoming data to json
			String firstname = StringUtils.substringBetween(data, "<firstname>", "</firstname>");
			String lastname = StringUtils.substringBetween(data, "<lastname>", "</lastname>");
			
			return "{\"firstname\":\"" + firstname + "\", \"lastname\":\"" + lastname + "\"}";
		}
	}
}