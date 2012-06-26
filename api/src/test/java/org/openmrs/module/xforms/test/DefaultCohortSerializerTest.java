package org.openmrs.module.xforms.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.module.xforms.download.PatientDownloadManager;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class DefaultCohortSerializerTest extends BaseModuleContextSensitiveTest {
	
	/**
	 * Test patient cohort serialization.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSerialize() throws Exception {
		executeDataSet("DefaultCohortSerializerTest.xml");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		PatientDownloadManager.downloadCohorts(dos, null);
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		int size = dis.readInt();
		Assert.assertTrue("There should be two cohorts", size == 2);
		
		int id = dis.readInt();
		Assert.assertTrue("The first cohort_id should be 1", id == 1);
		
		String name = dis.readUTF();
		Assert.assertTrue("The first cohort name should be: The First Unit Test Cohort",
		    name.equals("The First Unit Test Cohort"));
		
		id = dis.readInt();
		Assert.assertTrue("The second cohort_id should be 2", id == 2);
		
		name = dis.readUTF();
		Assert.assertTrue("The second cohort name should be: The Second Unit Test Cohort",
		    name.equals("The Second Unit Test Cohort"));
	}
}
