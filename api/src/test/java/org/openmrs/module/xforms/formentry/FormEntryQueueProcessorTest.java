/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		
		@Override
		public String[] getSupportedViews() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean supportsView(String view) {
			// TODO Auto-generated method stub
			return false;
		}

		public String serializeFormData(String data) {
			//This is a test implementation that converts the incoming data to json
			String firstname = StringUtils.substringBetween(data, "<firstname>", "</firstname>");
			String lastname = StringUtils.substringBetween(data, "<lastname>", "</lastname>");
			
			return "{\"firstname\":\"" + firstname + "\", \"lastname\":\"" + lastname + "\"}";
		}
	}
}
